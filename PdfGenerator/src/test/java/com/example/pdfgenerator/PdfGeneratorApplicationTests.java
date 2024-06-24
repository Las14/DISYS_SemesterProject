package com.example.pdfgenerator;

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
class PdfGeneratorApplicationTests {

    //@InjectMocks
    private PdfGeneratorApplication pdfGeneratorApplication;

   // @Mock
    private ConnectionFactory connectionFactory;

   // @Mock
    private Connection rabbitConnection;

   // @Mock
    private Channel channel;

    @BeforeEach
    public void setUp() throws IOException, TimeoutException {
        MockitoAnnotations.openMocks(this);

        when(connectionFactory.newConnection()).thenReturn(rabbitConnection);
        when(rabbitConnection.createChannel()).thenReturn(channel);
    }

    @Test
    public void testReceiveTotalCharge() throws Exception {
        pdfGeneratorApplication.receiveTotalCharge(connectionFactory);

        verify(connectionFactory, times(1)).setHost("localhost");
        verify(channel, times(1)).queueDeclare(PdfGeneratorApplication.QUEUE_CUSTOMER_TOTAL_CHARGE, false, false, false, null);
        verify(channel, times(1)).basicConsume((String) eq(PdfGeneratorApplication.QUEUE_CUSTOMER_TOTAL_CHARGE), eq(true), (DeliverCallback) any(DeliverCallback.class), (CancelCallback) any());
    }

}
