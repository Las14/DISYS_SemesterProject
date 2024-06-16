package com.example.pdfgenerator;

import com.example.pdfgenerator.entity.Customer;
import com.example.pdfgenerator.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GeneratorService {
    private final CustomerRepository customerRepository;

    @Autowired
    public GeneratorService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    Customer getCustomer(int customerId) {
        Optional<Customer> customerOptional = customerRepository.findById(customerId);
        return customerOptional.orElse(null);
    }
}
