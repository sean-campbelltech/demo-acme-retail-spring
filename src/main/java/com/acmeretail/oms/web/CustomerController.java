package com.acmeretail.oms.web;

import com.acmeretail.oms.service.CustomerService;
import com.acmeretail.oms.service.OrderService;
import com.acmeretail.oms.web.dto.CustomerResponse;
import com.acmeretail.oms.web.dto.OrderResponse;
import com.acmeretail.oms.web.mapper.ResponseMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final OrderService orderService;

    public CustomerController(CustomerService customerService, OrderService orderService) {
        this.customerService = customerService;
        this.orderService = orderService;
    }

    @GetMapping
    public List<CustomerResponse> list() {
        return customerService.findAll().stream()
                .map(ResponseMapper::toCustomerResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable Long id) {
        return ResponseMapper.toCustomerResponse(customerService.getById(id));
    }

    @GetMapping("/{id}/orders")
    public List<OrderResponse> orders(@PathVariable Long id) {
        return orderService.listForCustomer(id).stream()
                .map(ResponseMapper::toOrderResponse)
                .toList();
    }

    @PostMapping("/{id}/spend")
    public CustomerResponse recordSpend(@PathVariable Long id, @RequestParam BigDecimal amount) {
        return ResponseMapper.toCustomerResponse(customerService.recordSpend(id, amount));
    }
}
