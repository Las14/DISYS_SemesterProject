package com.example.datacollectiondispatcher;

import com.example.datacollectiondispatcher.entity.StationEntity;
import com.example.datacollectiondispatcher.repository.StationRepository;
import com.rabbitmq.client.DeliverCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

@SpringBootApplication
public class DataCollectionDispatcherApplication {

    private final static String QUEUE_NAME = "generate_invoice";

    public static void main(String[] args) {
        SpringApplication.run(DataCollectionDispatcherApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            try {
                receiveCustomerNumber();

                DispatcherService dispatcherService = ctx.getBean(DispatcherService.class);
                List<StationEntity> stations = dispatcherService.fetchStations();
                stations.forEach(station -> System.out.println(station.getDbUrl()));
            }
            catch(Exception e) {
                System.out.println(" [*] An exception occurred...");
            }
        };
    }

    public static void receiveCustomerNumber() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received customer Id '" + message + "'");
        };
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }
}
