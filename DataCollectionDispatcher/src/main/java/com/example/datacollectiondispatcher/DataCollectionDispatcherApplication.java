package com.example.datacollectiondispatcher;

import com.example.datacollectiondispatcher.entity.StationEntity;
import com.rabbitmq.client.DeliverCallback;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
public class DataCollectionDispatcherApplication {

    public final static String QUEUE_CUSTOMER_ID = "customer_id"; //red message
    public final static String QUEUE_CUSTOMER_STATIONS_DATA = "customer_stations_data"; //green
    public final static String QUEUE_INVOICE_GENERATION_STARTED = "invoice_generation_started"; //purple

    private static DispatcherService dispatcherService;

    public static void main(String[] args) {
        SpringApplication.run(DataCollectionDispatcherApplication.class, args);
    }

    //Run this code when application has started
    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            try {
                // Inject to use the DispatcherService
                dispatcherService = ctx.getBean(DispatcherService.class);
                ConnectionFactory factory = new ConnectionFactory();
                receiveCustomerNumber(factory); //receive red message
            }
            catch(Exception e) {
                System.out.println(" [*] An exception occurred...");
            }
        };
    }

    public static void receiveCustomerNumber(ConnectionFactory factory) throws IOException, TimeoutException {
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_CUSTOMER_ID, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String customerId = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received customer Id '" + customerId + "'");
            List<StationEntity> stations = dispatcherService.fetchStations();
            CustomerStationData data = new CustomerStationData(customerId, stations);

            sendStationData(data, factory); //send green message
            sendInvoiceGenerationInfos(data, factory); //send purple message
        };
          channel.basicConsume(QUEUE_CUSTOMER_ID, true, deliverCallback, consumerTag -> { });
    }

    public static void sendStationData(CustomerStationData data, ConnectionFactory factory) {
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_CUSTOMER_STATIONS_DATA, false, false, false, null);

            // convert stationData to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String message = objectMapper.writeValueAsString(data);

            channel.basicPublish("", QUEUE_CUSTOMER_STATIONS_DATA, null, message.getBytes());
            System.out.println(" [x] Sent stations data to stat invoice request for customer '" + data.getCustomerId() + "'");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendInvoiceGenerationInfos(CustomerStationData data, ConnectionFactory factory) {
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_INVOICE_GENERATION_STARTED, false, false, false, null);

            String message = data.getCustomerId() + ";" + data.getStations().size();
            channel.basicPublish("", QUEUE_INVOICE_GENERATION_STARTED, null, message.getBytes());
            System.out.println(" [x] Sent invoice generation started data  '" + message + "'");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
