package com.example.pdfgenerator.repository;

import com.example.pdfgenerator.entity.Customer;
import org.springframework.data.repository.CrudRepository;


public interface CustomerRepository  extends CrudRepository<Customer, Integer> {
}
