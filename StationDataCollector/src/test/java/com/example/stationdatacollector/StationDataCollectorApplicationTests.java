package com.example.stationdatacollector;

import com.example.stationdatacollector.reveivedModels.CustomerStationData;
import com.example.stationdatacollector.reveivedModels.StationEntity;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.CancelCallback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class StationDataCollectorApplicationTests {

    private StationDataCollectorApplication stationDataCollectorApplication;

    @Mock
    private ConnectionFactory connectionFactory;

    @Mock
    private Connection rabbitConnection;

    @Mock
    private Channel channel;

    @Mock
    private java.sql.Connection sqlConnection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @BeforeEach
    public void setUp() throws IOException, TimeoutException {
        MockitoAnnotations.openMocks(this);

        when(connectionFactory.newConnection()).thenReturn(rabbitConnection);
        when(rabbitConnection.createChannel()).thenReturn(channel);
    }

    @Test
    public void testReceiveInvoiceData() throws Exception {
        stationDataCollectorApplication.receiveInvoiceData(connectionFactory);

        verify(channel, times(1)).queueDeclare(StationDataCollectorApplication.QUEUE_CUSTOMER_STATIONS_DATA, false, false, false, null);
        verify(channel, times(1)).basicConsume(eq(StationDataCollectorApplication.QUEUE_CUSTOMER_STATIONS_DATA), eq(true), (DeliverCallback) any(), (CancelCallback) any());
    }

    @Test
    public void testGetCustomerChargeFromDBs() throws SQLException {
        CustomerStationData data = new CustomerStationData();
        StationEntity station1 = new StationEntity();
        station1.setDbUrl("localhost");
        StationEntity station2 = new StationEntity();
        station2.setDbUrl("localhost");
        data.setStations(List.of(station1, station2));
        data.setCustomerId("1");

        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString())).thenReturn(sqlConnection);
            when(sqlConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true).thenReturn(false).thenReturn(true).thenReturn(false);
            when(resultSet.getDouble("total_kwh")).thenReturn(10.0);

            stationDataCollectorApplication.getCustomerChargeFromDBs(data);

            verify(sqlConnection, times(2)).prepareStatement(anyString());
            verify(preparedStatement, times(2)).executeQuery();
            verify(resultSet, times(2)).getDouble("total_kwh");
        }
    }

    @Test
    public void testSendCharge() throws IOException, TimeoutException {
        String customerId = "123";
        double totalCharge = 50.0;

        stationDataCollectorApplication.sendCharge(customerId, totalCharge, connectionFactory);

        verify(channel, times(1)).queueDeclare(StationDataCollectorApplication.QUEUE_CUSTOMER_CHARGE_DATA, false, false, false, null);
        verify(channel, times(1)).basicPublish("", StationDataCollectorApplication.QUEUE_CUSTOMER_CHARGE_DATA, null, (customerId + ";" + totalCharge).getBytes());
    }
}