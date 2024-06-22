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

    @GetMapping("/{customerId}")
    public ResponseEntity getInvoice(@PathVariable String customerId) {
        String filePath = "invoices/customer_invoice_" + customerId + ".pdf";
        File file = new File(filePath);

        if (file.exists()) {
            Resource resource = new FileSystemResource(file);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());
            headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");
            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
