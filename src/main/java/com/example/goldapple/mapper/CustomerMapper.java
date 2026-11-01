package com.example.goldapple.mapper;

import com.example.goldapple.dto.CustomerResponse;
import com.example.goldapple.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getPhone(),
                customer.getDateOfBirth(),
                customer.getStatus(),
                customer.getCreatedAt()
        );
    }
}
