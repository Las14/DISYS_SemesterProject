package com.example.stationdatacollector;

import com.example.stationdatacollector.reveivedModels.CustomerStationData;
import com.example.stationdatacollector.reveivedModels.StationEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;


@SpringBootApplication
public class StationDataCollectorApplication {

    public final static String QUEUE_CUSTOMER_STATIONS_DATA = "customer_stations_data"; //green
    public final static String QUEUE_CUSTOMER_CHARGE_DATA = "customer_charge_data"; //blue

    public static void main(String[] args) {
        SpringApplication.run(StationDataCollectorApplication.class, args);
    }

    //Run this code when application has started
    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            try {
                ConnectionFactory factory = new ConnectionFactory(); //create factory here to be able to test it
                receiveInvoiceData(factory); //receive green message
            }
            catch(Exception e) {
                System.out.println(" [*] An exception occurred...");
            }
        };
    }

    public static void receiveInvoiceData(ConnectionFactory factory) throws IOException, TimeoutException {
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_CUSTOMER_STATIONS_DATA, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                CustomerStationData data = objectMapper.readValue(delivery.getBody(), CustomerStationData.class);
                System.out.println("Customer ID: " + data.getCustomerId());
                getCustomerChargeFromDBs(data);
            } catch (IOException e) {
                e.printStackTrace();
            }

        };
        channel.basicConsume(QUEUE_CUSTOMER_STATIONS_DATA, true, deliverCallback, consumerTag -> { });
    }

    public static void getCustomerChargeFromDBs(CustomerStationData data) {
        var stations = data.getStations();
        try {
            for(int i = 0; i < stations.size(); i++) {
                getCustomerChargeFromDB(data.getCustomerId(), stations.get(i));
            }
        } catch(Exception e){
            System.out.println(" [*] An exception occurred...");
        }

    }

    public static void getCustomerChargeFromDB(String customerId, StationEntity station) throws SQLException {
       String dbUrl = String.format("jdbc:postgresql://%s/stationdb?user=postgres&password=postgres", station.getDbUrl());

        double totalCharge = 0.0;
        try (java.sql.Connection connection = DriverManager.getConnection(dbUrl)) {
            String sql = "SELECT SUM(kwh) AS total_kwh FROM charge WHERE customer_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, Integer.parseInt(customerId));
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        totalCharge = resultSet.getDouble("total_kwh");
                    }
                }
            }
        }

        //send blue message
        ConnectionFactory factory = new ConnectionFactory(); //create factory here to be able to test it
        sendCharge(customerId, totalCharge, factory);
    }

    public static void sendCharge(String customerId, double totalCharge, ConnectionFactory factory) {
        factory.setHost("localhost");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_CUSTOMER_CHARGE_DATA, false, false, false, null);

            String message = customerId + ";" + totalCharge;

            channel.basicPublish("", QUEUE_CUSTOMER_CHARGE_DATA, null, message.getBytes());
            System.out.println(" [x] Sent customer charge data " + message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

}
