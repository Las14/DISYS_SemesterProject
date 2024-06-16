package com.example.datacollectionreceiver;

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

import java.util.concurrent.TimeoutException;

@SpringBootApplication
public class DataCollectionReceiverApplication {
    private final static String QUEUE_INVOICE_GENERATION_STARTED = "invoice_generation_started"; //purple
    private final static String QUEUE_CUSTOMER_CHARGE_DATA = "customer_charge_data"; //blue
    private final static String QUEUE_CUSTOMER_TOTAL_CHARGE = "customer_total_charge"; //orange
    private static int  totalCount = 0;
    private static int count = 0;
    private static double  totalCharge = 0;

    public static void main(String[] args) {
        SpringApplication.run(DataCollectionReceiverApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            try {
                receiveNumberOfDatabasesForCustomer(); //receive purple message

            }
            catch(Exception e) {
                System.out.println("[*] An exception occurred...");
            }
        };
    }

    private static void receiveNumberOfDatabasesForCustomer() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_INVOICE_GENERATION_STARTED, false, false, false, null);
        System.out.println("[*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            try {
                System.out.println("---------------------------- Begin --------------------------------");
                String data = new String(delivery.getBody(), "UTF-8");
                System.out.println("data: "+ data);
                String[] idNumberOfStations = data.split(";");
                totalCount = Integer.parseInt(idNumberOfStations[1]);
                System.out.println("[*] Received customer total databases: "+ totalCount);
                receiveInvoiceData();
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
        };
        channel.basicConsume(QUEUE_INVOICE_GENERATION_STARTED, true, deliverCallback, consumerTag -> { });
    }

    private static void receiveInvoiceData() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_CUSTOMER_CHARGE_DATA, false, false, false, null);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            try {
                String data = new String(delivery.getBody(), "UTF-8");
                String[] idTotalCharge = data.split(";");
                String id = idTotalCharge[0];
                System.out.println("[x] Received customer Id: " + id);
                System.out.println("[x] Received customer charge: " + idTotalCharge[1]);

                totalCharge = totalCharge + Double.parseDouble(idTotalCharge[1]);
                count++;

                if(count == totalCount) {
                    count = 0; //reset counter for the next customer
                    sendTotalCharge(id); //send orange message
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        channel.basicConsume(QUEUE_CUSTOMER_CHARGE_DATA, true, deliverCallback, consumerTag -> { });
    }

    public static void sendTotalCharge(String customerId) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_CUSTOMER_TOTAL_CHARGE, false, false, false, null);

            String message = customerId + ";" + totalCharge;
            channel.basicPublish("", QUEUE_CUSTOMER_TOTAL_CHARGE, null, message.getBytes());
            System.out.println("[x] Sent total charge for customer  '" + message + "'");
            totalCharge = 0; //reset total charge
            System.out.println("count: " + count);
            System.out.println("totalCharge: " + totalCharge);
            System.out.println("---------------------------- End --------------------------------");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }






}
