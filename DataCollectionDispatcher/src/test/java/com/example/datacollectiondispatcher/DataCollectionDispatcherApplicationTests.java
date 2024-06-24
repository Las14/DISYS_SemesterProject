package com.example.datacollectiondispatcher;


import com.example.datacollectiondispatcher.entity.StationEntity;
import com.rabbitmq.client.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class DataCollectionDispatcherApplicationTests {
    private DataCollectionDispatcherApplication dataCollectionDispatcherApplication;

    @Mock
    private ConnectionFactory connectionFactory;

    @Mock
    private Connection rabbitConnection;

    @Mock
    private Channel channel;

    @BeforeEach
    public void setUp() throws IOException, TimeoutException {
        MockitoAnnotations.openMocks(this);

        when(connectionFactory.newConnection()).thenReturn(rabbitConnection);
        when(rabbitConnection.createChannel()).thenReturn(channel);
    }

    @Test
    public void testReceiveCustomerNumber() throws Exception {
        dataCollectionDispatcherApplication.receiveCustomerNumber(connectionFactory);

        verify(connectionFactory, times(1)).setHost("localhost");
        verify(channel, times(1)).queueDeclare(DataCollectionDispatcherApplication.QUEUE_CUSTOMER_ID, false, false, false, null);
        verify(channel, times(1)).basicConsume((String) eq(DataCollectionDispatcherApplication.QUEUE_CUSTOMER_ID), eq(true), (DeliverCallback) any(DeliverCallback.class), (CancelCallback) any());
    }

    @Test
    public void testSendStationData() throws Exception {
        var station = new StationEntity();
        station.setLat(1L);
        station.setLng(2L);
        station.setId(1);
        station.setDbUrl("http://askjhf.com");
        CustomerStationData data = new CustomerStationData("123", List.of(station));

        dataCollectionDispatcherApplication.sendStationData(data, connectionFactory);

        verify(channel, times(1)).queueDeclare(DataCollectionDispatcherApplication.QUEUE_CUSTOMER_STATIONS_DATA, false, false, false, null);
        verify(channel, times(1)).basicPublish(eq(""), eq(DataCollectionDispatcherApplication.QUEUE_CUSTOMER_STATIONS_DATA), eq(null), any(byte[].class));
    }

    @Test
    public void testSendInvoiceGenerationInfos() throws Exception {
        var station = new StationEntity();
        station.setLat(1L);
        station.setLng(2L);
        station.setId(1);
        station.setDbUrl("http://askjhf.com");
        CustomerStationData data = new CustomerStationData("123", List.of(station));

        dataCollectionDispatcherApplication.sendInvoiceGenerationInfos(data, connectionFactory);

        verify(channel, times(1)).queueDeclare(DataCollectionDispatcherApplication.QUEUE_INVOICE_GENERATION_STARTED, false, false, false, null);
        verify(channel, times(1)).basicPublish(eq(""), eq(DataCollectionDispatcherApplication.QUEUE_INVOICE_GENERATION_STARTED), eq(null), any(byte[].class));
    }

}
