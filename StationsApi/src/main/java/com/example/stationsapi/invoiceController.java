package com.example.stationsapi;

import org.springframework.web.bind.annotation.*;

@RestController
public class invoiceController {

    @GetMapping("/")
    public String generateInvoice(@RequestParam String customerId) {
        MessageSender sender = new MessageSender();
        sender.generateInvoice(customerId);
        return String.format("Start generating Invoice for customer with id %s",customerId);
    }

}
