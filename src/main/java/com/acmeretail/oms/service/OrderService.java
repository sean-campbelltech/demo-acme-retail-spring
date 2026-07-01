package com.acmeretail.oms.service;

import com.acmeretail.oms.domain.enums.LoyaltyTier;
import com.acmeretail.oms.domain.enums.OrderStatus;
import com.acmeretail.oms.domain.enums.ShippingMethod;
import com.acmeretail.oms.domain.model.Address;
import com.acmeretail.oms.domain.model.Coupon;
import com.acmeretail.oms.domain.model.Customer;
import com.acmeretail.oms.domain.model.Order;
import com.acmeretail.oms.domain.model.OrderLine;
import com.acmeretail.oms.domain.model.Product;
import com.acmeretail.oms.domain.vo.Money;
import com.acmeretail.oms.exception.PricingException;
import com.acmeretail.oms.exception.ResourceNotFoundException;
import com.acmeretail.oms.repository.CustomerRepository;
import com.acmeretail.oms.repository.OrderRepository;
import com.acmeretail.oms.repository.ProductRepository;
import com.acmeretail.oms.service.pricing.PriceBreakdown;
import com.acmeretail.oms.service.support.OrderNumberGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

/**
 * Application service coordinating the order lifecycle: building drafts, adding
 * lines, attaching coupons, quoting prices and finally placing orders (which
 * verifies and reserves stock, persists the computed totals and advances the order
 * status).
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final PricingService pricingService;
    private final InventoryService inventoryService;
    private final CouponService couponService;
    private final OrderNumberGenerator orderNumberGenerator;

    public OrderService(OrderRepository orderRepository,
                        CustomerRepository customerRepository,
                        ProductRepository productRepository,
                        PricingService pricingService,
                        InventoryService inventoryService,
                        CouponService couponService,
                        OrderNumberGenerator orderNumberGenerator) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.pricingService = pricingService;
        this.inventoryService = inventoryService;
        this.couponService = couponService;
        this.orderNumberGenerator = orderNumberGenerator;
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> ResourceNotFoundException.of("Order", orderId));
    }

    @Transactional(readOnly = true)
    public Order getByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> ResourceNotFoundException.of("Order", orderNumber));
    }

    @Transactional(readOnly = true)
    public List<Order> listForCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    @Transactional
    public Order createDraft(Long customerId, ShippingMethod shippingMethod, Address shippingAddress) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Customer", customerId));

        Order order = new Order(orderNumberGenerator.next(), customer);
        if (shippingMethod != null) {
            order.setShippingMethod(shippingMethod);
        }
        if (shippingAddress != null) {
            order.setShippingAddress(shippingAddress);
        } else if (customer.getDefaultShippingAddress() != null) {
            order.setShippingAddress(customer.getDefaultShippingAddress());
        }
        return orderRepository.save(order);
    }

    @Transactional
    public Order addLine(Long orderId, String sku, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        Order order = getOrder(orderId);
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> ResourceNotFoundException.of("Product", sku));
        if (!product.isActive()) {
            throw new PricingException("product " + sku + " is not available for sale");
        }
        order.addLine(new OrderLine(product, quantity));
        return orderRepository.save(order);
    }

    @Transactional
    public Order applyCoupon(Long orderId, String couponCode) {
        Order order = getOrder(orderId);
        // Validate eagerly so that an invalid coupon is rejected at attach time.
        Currency currency = Currency.getInstance(order.getCurrencyCode());
        Money discountableSubtotal = discountableSubtotal(order, currency);
        LoyaltyTier tier = order.getCustomer() != null ? order.getCustomer().getLoyaltyTier() : LoyaltyTier.NONE;
        Coupon coupon = couponService.loadApplicableCoupon(
                couponCode, discountableSubtotal, tier, LocalDate.now());
        order.setCouponCode(coupon.getCode());
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public PriceBreakdown quote(Long orderId) {
        return pricingService.priceOrder(getOrder(orderId));
    }

    /**
     * Places a draft order: re-prices it, confirms stock, reserves inventory,
     * persists the computed totals, records any coupon redemption and advances the
     * status to {@code PLACED}.
     */
    @Transactional
    public Order placeOrder(Long orderId) {
        Order order = getOrder(orderId);
        if (!order.getStatus().isEditable()) {
            throw new PricingException("order " + order.getOrderNumber()
                    + " has already been placed (status " + order.getStatus() + ")");
        }
        if (order.isEmpty()) {
            throw new PricingException("cannot place an empty order");
        }

        inventoryService.verifyAvailability(order.getLines());

        PriceBreakdown breakdown = pricingService.priceOrder(order);
        applyTotals(order, breakdown);

        inventoryService.reserve(order.getLines());

        if (breakdown.getAppliedCouponCode() != null) {
            couponService.findByCode(breakdown.getAppliedCouponCode())
                    .ifPresent(couponService::recordRedemption);
        }

        order.transitionTo(OrderStatus.PLACED);
        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = getOrder(orderId);
        order.transitionTo(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    private void applyTotals(Order order, PriceBreakdown breakdown) {
        order.setSubtotal(breakdown.getSubtotal().getAmount());
        order.setDiscountTotal(breakdown.getDiscountTotal().getAmount());
        order.setShippingTotal(breakdown.getShippingCost().getAmount());
        order.setTaxTotal(breakdown.getTax().getAmount());
        order.setGrandTotal(breakdown.getGrandTotal().getAmount());
    }

    private Money discountableSubtotal(Order order, Currency currency) {
        Money total = Money.zero(currency);
        for (OrderLine line : order.getLines()) {
            if (line.isDiscountable()) {
                total = total.add(Money.of(line.lineSubtotal(), currency));
            }
        }
        return total;
    }
}
