package com.example.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class InvoiceController {

    @FXML
    private TextField customer_id;

    @FXML
    private Button collectData;

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        collectData.setOnAction(event -> collectData());
    }
    //https://www.digitalocean.com/community/tutorials/java-httpurlconnection-example-java-http-request-get-post
    @FXML
    private void collectData() {
        String customerId = customer_id.getText();
        try {
            URL url = new URL("http://localhost:8080/invoices/" + customerId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            int responseCode = conn.getResponseCode();
            System.out.println("POST Response Code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                pollingForInvoice(customerId);
            } else {
                System.out.println("POST request did not work.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//https://medium.com/@akshathaholla91/scheduling-poll-based-tasks-using-timer-in-java-bebbb0e814aa
    private void pollingForInvoice(String customerId) {
        //Creates a new Timer instance.
        Timer timer = new Timer();
        //class is an abstract class that implements the Runnable interface and represents a task that can be scheduled for execution by a Timer.
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    //Calls the invoiceStatus method to check if the invoice for the specified customer ID is ready.
                    var status = invoiceStatus(customerId);
                    System.out.println("status: " + status);
                   //Checks if the invoice is ready.
                    if (status) {
                        timer.cancel();
                    }
                } catch (IOException e) {
                    //Outputs the stack trace of the exception to the console, which helps in debugging by showing where the exception occurred.
                    e.printStackTrace();
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 5000); // Poll every 5 seconds
    }

    private boolean invoiceStatus(String customerId) throws IOException {
        URL url = new URL("http://localhost:8080/invoices/" + customerId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            String savePath = "C:/Users/Maria/OneDrive/Desktop/invoices/invoice_" + customerId + ".pdf";
            //Obtains the input stream from the HTTP connection (conn), allowing you to read the response data from the server.
            try (InputStream in = new BufferedInputStream(conn.getInputStream());
                 //Creates a FileOutputStream to the file located at savePath, allowing you to write data to this file.
                 // If the file does not exist, it will be created. If it does exist, it will be overwritten.
                 FileOutputStream out = new FileOutputStream(savePath)) {
                //Declares a byte array buffer used to temporarily store chunks of data read from the input stream.
                byte[] buffer = new byte[4096];
                int bytesRead;
                // Reads up to buffer.length bytes of data from the input stream into the buffer.
                // Returns the number of bytes read, or -1 if the end of the stream is reached.
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            updateStatusLabel("Invoice downloaded successfully: " + savePath);
            Desktop.getDesktop().open(new File(savePath));
            return true; // PDF is ready and downloaded
        } else {
            System.out.println("GET request did not work.");
            updateStatusLabel("GET request did not work.");
        }
        return false; // PDF is not ready yet or download failed
    }

    private void updateStatusLabel(String text) {
        Platform.runLater(() -> statusLabel.setText(text));
    }
}
