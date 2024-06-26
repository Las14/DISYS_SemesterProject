package com.example.pdfgenerator;

import com.example.pdfgenerator.entity.Customer;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

@SpringBootApplication
public class PdfGeneratorApplication {
    public final static String QUEUE_CUSTOMER_TOTAL_CHARGE = "customer_total_charge"; //orange

    public static GeneratorService generatorService;

    public static void main(String[] args) {
        SpringApplication.run(PdfGeneratorApplication.class, args);
    }


    //Run this code when application has started
    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            try {
                generatorService = ctx.getBean(GeneratorService.class);
                ConnectionFactory factory = new ConnectionFactory();
                receiveTotalCharge(factory);//receive orange message

            }
            catch(Exception e) {
                System.out.println("[*] An exception occurred...");
            }
        };
    }

    public static void receiveTotalCharge(ConnectionFactory factory) throws IOException, TimeoutException {
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_CUSTOMER_TOTAL_CHARGE, false, false, false, null);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            try {
                String data = new String(delivery.getBody(), "UTF-8");
                String[] idTotalCharge = data.split(";");
                int id = Integer.parseInt(idTotalCharge[0]);
                double charge = Double.parseDouble(idTotalCharge[1]);
                System.out.println("[x] Received customer Id: " + id);
                System.out.println("[x] Received customer charge: " + charge);
                generateCustomerInvoice(id, charge);

            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        channel.basicConsume(QUEUE_CUSTOMER_TOTAL_CHARGE, true, deliverCallback, consumerTag -> { });
    }


    private static void generateCustomerInvoice(int id, double charge){
        Customer customer = generatorService.getCustomer(id);

        // Ensure the invoices directory exists
        File invoicesDir = new File("invoices");
        //Checks if the invoices directory exists, and if not, creates it
        if (!invoicesDir.exists()) {
            invoicesDir.mkdirs();
        }

        // Define the file path to save the PDF
        String filePath = "invoices/customer_invoice_" + id + ".pdf";
            //Creates a FileOutputStream to write the PDF file to the specified filePath
        try (FileOutputStream fos = new FileOutputStream(filePath);
             //Creates a PdfWriter instance to write content to the PDF file.
             PdfWriter writer = new PdfWriter(fos);
             //Creates a PdfDocument instance representing the PDF document.
             PdfDocument pdfDoc = new PdfDocument(writer);
             //Creates a Document instance to add elements (like paragraphs) to the PDF.
             Document document = new Document(pdfDoc)) {

            // Add content to the PDF
            if(customer == null) {
                document.add(new Paragraph("No open invoices found"));
            } else {
                document.add(new Paragraph("Invoice"));
                document.add(new Paragraph("Customer ID: " + customer.getId()));
                document.add(new Paragraph("Customer Name: " + customer.getFirstName() + " " + customer.getLastName()));
                document.add(new Paragraph("Charge: $" + charge));
                System.out.println("Invoice generated successfully: " + filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}