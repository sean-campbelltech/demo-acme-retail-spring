package com.acmeretail.oms.domain.model;

import com.acmeretail.oms.domain.enums.OrderStatus;
import com.acmeretail.oms.domain.enums.ShippingMethod;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 24)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderColumn(name = "line_position")
    private List<OrderLine> lines = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private OrderStatus status = OrderStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipping_method", nullable = false, length = 16)
    private ShippingMethod shippingMethod = ShippingMethod.STANDARD;

    @Embedded
    @AttributeOverride(name = "line1", column = @Column(name = "ship_line1"))
    @AttributeOverride(name = "line2", column = @Column(name = "ship_line2"))
    @AttributeOverride(name = "city", column = @Column(name = "ship_city"))
    @AttributeOverride(name = "region", column = @Column(name = "ship_region"))
    @AttributeOverride(name = "postalCode", column = @Column(name = "ship_postal_code"))
    @AttributeOverride(name = "countryCode", column = @Column(name = "ship_country_code"))
    private Address shippingAddress = new Address();

    @Column(name = "coupon_code", length = 40)
    private String couponCode;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode = "USD";

    @Column(name = "subtotal", precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "discount_total", precision = 12, scale = 2)
    private BigDecimal discountTotal;

    @Column(name = "shipping_total", precision = 12, scale = 2)
    private BigDecimal shippingTotal;

    @Column(name = "tax_total", precision = 12, scale = 2)
    private BigDecimal taxTotal;

    @Column(name = "grand_total", precision = 12, scale = 2)
    private BigDecimal grandTotal;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "placed_at")
    private Instant placedAt;

    public Order() {
    }

    public Order(String orderNumber, Customer customer) {
        this.orderNumber = orderNumber;
        this.customer = customer;
    }

    public OrderLine addLine(OrderLine line) {
        if (line == null) {
            throw new IllegalArgumentException("line must not be null");
        }
        if (!status.isEditable()) {
            throw new IllegalStateException("cannot modify lines of an order in status " + status);
        }
        line.setOrder(this);
        lines.add(line);
        return line;
    }

    public boolean removeLineBySku(String sku) {
        if (!status.isEditable()) {
            throw new IllegalStateException("cannot modify lines of an order in status " + status);
        }
        return lines.removeIf(line -> line.getSku() != null && line.getSku().equals(sku));
    }

    public int totalQuantity() {
        int total = 0;
        for (OrderLine line : lines) {
            total += line.getQuantity();
        }
        return total;
    }

    public BigDecimal totalWeight() {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderLine line : lines) {
            total = total.add(line.lineWeight());
        }
        return total;
    }

    public boolean hasPhysicalGoods() {
        for (OrderLine line : lines) {
            if (line.isRequiresShipping()) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    /**
     * Applies a validated status transition, recording the placed timestamp the
     * first time the order leaves {@code DRAFT}.
     */
    public void transitionTo(OrderStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new IllegalStateException("illegal transition " + status + " -> " + target);
        }
        if (status == OrderStatus.DRAFT && target == OrderStatus.PLACED) {
            this.placedAt = Instant.now();
        }
        this.status = target;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<OrderLine> getLines() {
        return lines;
    }

    public void setLines(List<OrderLine> lines) {
        this.lines = lines;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public ShippingMethod getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(ShippingMethod shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(Address shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDiscountTotal() {
        return discountTotal;
    }

    public void setDiscountTotal(BigDecimal discountTotal) {
        this.discountTotal = discountTotal;
    }

    public BigDecimal getShippingTotal() {
        return shippingTotal;
    }

    public void setShippingTotal(BigDecimal shippingTotal) {
        this.shippingTotal = shippingTotal;
    }

    public BigDecimal getTaxTotal() {
        return taxTotal;
    }

    public void setTaxTotal(BigDecimal taxTotal) {
        this.taxTotal = taxTotal;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(BigDecimal grandTotal) {
        this.grandTotal = grandTotal;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getPlacedAt() {
        return placedAt;
    }

    public void setPlacedAt(Instant placedAt) {
        this.placedAt = placedAt;
    }
}
