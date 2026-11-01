package com.example.goldapple.service;

import com.example.goldapple.dto.CustomerResponse;
import com.example.goldapple.dto.SaveCustomerRequest;
import com.example.goldapple.entity.Customer;
import com.example.goldapple.exception.ApiException;
import com.example.goldapple.exception.ErrorCode;
import com.example.goldapple.mapper.CustomerMapper;
import com.example.goldapple.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public List<CustomerResponse> getCustomers() {
        return customerRepository.findAllByOrderByIdAsc().stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    public CustomerResponse getCustomer(Long customerId) {
        return customerMapper.toResponse(getExistingCustomer(customerId));
    }

    @Transactional
    public CustomerResponse create(SaveCustomerRequest request) {
        Customer customer = new Customer();
        apply(customer, request);
        customer.setCreatedAt(Instant.now());
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse update(Long customerId, SaveCustomerRequest request) {
        Customer customer = getExistingCustomer(customerId);
        apply(customer, request);
        return customerMapper.toResponse(customer);
    }

    private void apply(Customer customer, SaveCustomerRequest request) {
        customer.setFirstName(request.firstName().trim());
        customer.setLastName(request.lastName().trim());
        customer.setPhone(request.phone().trim());
        customer.setDateOfBirth(request.birthDate());
        customer.setStatus(request.status());
    }

    public Customer getExistingCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.CUSTOMER_NOT_FOUND, "Клиент не найден"));
    }
}
