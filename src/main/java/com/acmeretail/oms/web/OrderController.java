package com.acmeretail.oms.web;

import com.acmeretail.oms.domain.model.Address;
import com.acmeretail.oms.domain.model.Order;
import com.acmeretail.oms.service.OrderService;
import com.acmeretail.oms.service.pricing.PriceBreakdown;
import com.acmeretail.oms.web.dto.AddLineRequest;
import com.acmeretail.oms.web.dto.ApplyCouponRequest;
import com.acmeretail.oms.web.dto.CreateOrderRequest;
import com.acmeretail.oms.web.dto.OrderLineRequest;
import com.acmeretail.oms.web.dto.OrderResponse;
import com.acmeretail.oms.web.dto.PriceBreakdownResponse;
import com.acmeretail.oms.web.mapper.ResponseMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        Address address = request.shippingAddress() != null ? request.shippingAddress().toAddress() : null;
        Order order = orderService.createDraft(request.customerId(), request.shippingMethod(), address);

        if (request.lines() != null) {
            for (OrderLineRequest line : request.lines()) {
                order = orderService.addLine(order.getId(), line.sku(), line.quantity());
            }
        }
        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            order = orderService.applyCoupon(order.getId(), request.couponCode());
        }
        return ResponseMapper.toOrderResponse(order);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id) {
        return ResponseMapper.toOrderResponse(orderService.getOrder(id));
    }

    @PostMapping("/{id}/lines")
    public OrderResponse addLine(@PathVariable Long id, @Valid @RequestBody AddLineRequest request) {
        Order order = orderService.addLine(id, request.sku(), request.quantity());
        return ResponseMapper.toOrderResponse(order);
    }

    @PostMapping("/{id}/coupon")
    public OrderResponse applyCoupon(@PathVariable Long id, @Valid @RequestBody ApplyCouponRequest request) {
        Order order = orderService.applyCoupon(id, request.code());
        return ResponseMapper.toOrderResponse(order);
    }

    @GetMapping("/{id}/quote")
    public PriceBreakdownResponse quote(@PathVariable Long id) {
        PriceBreakdown breakdown = orderService.quote(id);
        return ResponseMapper.toBreakdownResponse(breakdown);
    }

    @PostMapping("/{id}/place")
    public OrderResponse place(@PathVariable Long id) {
        return ResponseMapper.toOrderResponse(orderService.placeOrder(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseMapper.toOrderResponse(orderService.cancelOrder(id)));
    }
}
