package com.example.datacollectionreceiver;

import com.rabbitmq.client.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
class DataCollectionReceiverApplicationTests {

    @InjectMocks
    DataCollectionReceiverApplication dataCollectionReceiverApplication;

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
    public void testReceiveNumberOfDatabasesForCustomer() throws Exception {
        dataCollectionReceiverApplication.receiveNumberOfDatabasesForCustomer(connectionFactory);

        verify(connectionFactory, times(1)).setHost("localhost");
        verify(channel, times(1)).queueDeclare(DataCollectionReceiverApplication.QUEUE_INVOICE_GENERATION_STARTED, false, false, false, null);
        verify(channel, times(1)).basicConsume((String) eq(DataCollectionReceiverApplication.QUEUE_INVOICE_GENERATION_STARTED), eq(true), (DeliverCallback) any(DeliverCallback.class), (CancelCallback) any());
    }

    @Test
    public void testReceiveInvoiceData() throws Exception {
        dataCollectionReceiverApplication.receiveInvoiceData(connectionFactory);

        verify(connectionFactory, times(1)).setHost("localhost");
        verify(channel, times(1)).queueDeclare(DataCollectionReceiverApplication.QUEUE_CUSTOMER_CHARGE_DATA, false, false, false, null);
        verify(channel, times(1)).basicConsume((String) eq(DataCollectionReceiverApplication.QUEUE_CUSTOMER_CHARGE_DATA), eq(true), (DeliverCallback) any(DeliverCallback.class), (CancelCallback) any());
    }

    @Test
    public void testSendTotalCharge() throws Exception {
        dataCollectionReceiverApplication.sendTotalCharge("1", connectionFactory);

        verify(connectionFactory, times(1)).setHost("localhost");
        verify(channel, times(1)).queueDeclare(DataCollectionReceiverApplication.QUEUE_CUSTOMER_TOTAL_CHARGE, false, false, false, null);
        verify(channel, times(1)).basicPublish(eq(""), (String) eq(DataCollectionReceiverApplication.QUEUE_CUSTOMER_TOTAL_CHARGE), eq(null), any(byte[].class));
    }

}
