package com.acmeretail.oms.service;

import com.acmeretail.oms.domain.model.Customer;
import com.acmeretail.oms.exception.ResourceNotFoundException;
import com.acmeretail.oms.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Customer getById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Customer", id));
    }

    @Transactional(readOnly = true)
    public Customer getByEmail(String email) {
        return customerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> ResourceNotFoundException.of("Customer", email));
    }

    /**
     * Adds to a customer's lifetime spend and re-evaluates their loyalty tier,
     * persisting the change. Returns the (possibly upgraded) tier.
     */
    @Transactional
    public Customer recordSpend(Long customerId, BigDecimal amount) {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("spend amount must not be negative");
        }
        Customer customer = getById(customerId);
        BigDecimal current = customer.getLifetimeSpend() == null ? BigDecimal.ZERO : customer.getLifetimeSpend();
        customer.setLifetimeSpend(current.add(amount));
        customer.recalculateLoyaltyTier();
        return customerRepository.save(customer);
    }
}
