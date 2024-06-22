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

    private void pollingForInvoice(String customerId) {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    var status = invoiceStatus(customerId);
                    System.out.println("status: " + status);
                    if (status) {
                        timer.cancel();
                    }
                } catch (IOException e) {
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
            try (InputStream in = new BufferedInputStream(conn.getInputStream());
                 FileOutputStream out = new FileOutputStream(savePath)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
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
