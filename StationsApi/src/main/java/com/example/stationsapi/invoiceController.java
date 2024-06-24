package com.example.stationsapi;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("/invoices")
public class invoiceController {

    @PostMapping("/{customerId}")
    public String generateInvoice(@PathVariable String customerId) {
        MessageSender sender = new MessageSender();
        sender.generateInvoice(customerId);
        return String.format("Start generating Invoice for customer with id %s",customerId);
    }
//In Spring Framework, ResponseEntity is a powerful and flexible way to configure the HTTP response for a RESTful API endpoint.
// It represents the entire HTTP response, including the status code, headers, and body.
    @GetMapping("/{customerId}")
    public ResponseEntity getInvoice(@PathVariable String customerId) {
        //Constructs the file path for the invoice PDF based on the customerId.
        String filePath = "invoices/customer_invoice_" + customerId + ".pdf";
        File file = new File(filePath);

        if (file.exists()) {
            Resource resource = new FileSystemResource(file);
            //This line creates an instance of HttpHeaders which will be used to add HTTP headers to the response (test)
            HttpHeaders headers = new HttpHeaders();
            // It indicates that the response content should be treated as an attachment, and specifies the filename for the attachment.
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());
            //The value for the Content-Type header. It specifies that the response content is of type PDF.
            headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");
            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
