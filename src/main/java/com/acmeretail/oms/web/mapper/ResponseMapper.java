package com.acmeretail.oms.web.mapper;

import com.acmeretail.oms.domain.model.Customer;
import com.acmeretail.oms.domain.model.Order;
import com.acmeretail.oms.domain.model.OrderLine;
import com.acmeretail.oms.domain.model.Product;
import com.acmeretail.oms.service.pricing.PriceBreakdown;
import com.acmeretail.oms.web.dto.CustomerResponse;
import com.acmeretail.oms.web.dto.OrderLineResponse;
import com.acmeretail.oms.web.dto.OrderResponse;
import com.acmeretail.oms.web.dto.PriceBreakdownResponse;
import com.acmeretail.oms.web.dto.ProductResponse;

import java.util.List;

/**
 * Maps domain objects to their API response representations. Kept deliberately as
 * a set of small, pure static methods.
 */
public final class ResponseMapper {

    private ResponseMapper() {
    }

    public static OrderLineResponse toLineResponse(OrderLine line) {
        return new OrderLineResponse(
                line.getSku(),
                line.getDescription(),
                line.getUnitPrice(),
                line.getQuantity(),
                line.lineSubtotal());
    }

    public static OrderResponse toOrderResponse(Order order) {
        List<OrderLineResponse> lines = order.getLines().stream()
                .map(ResponseMapper::toLineResponse)
                .toList();
        Long customerId = order.getCustomer() != null ? order.getCustomer().getId() : null;
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                customerId,
                order.getStatus(),
                order.getShippingMethod(),
                order.getCouponCode(),
                order.getCurrencyCode(),
                lines,
                order.getSubtotal(),
                order.getDiscountTotal(),
                order.getShippingTotal(),
                order.getTaxTotal(),
                order.getGrandTotal());
    }

    public static PriceBreakdownResponse toBreakdownResponse(PriceBreakdown breakdown) {
        return new PriceBreakdownResponse(
                breakdown.getSubtotal().getCurrencyCode(),
                breakdown.getSubtotal().getAmount(),
                breakdown.getLoyaltyDiscount().getAmount(),
                breakdown.getCouponDiscount().getAmount(),
                breakdown.getDiscountTotal().getAmount(),
                breakdown.getDiscountedSubtotal().getAmount(),
                breakdown.getShippingCost().getAmount(),
                breakdown.getTax().getAmount(),
                breakdown.getGrandTotal().getAmount(),
                breakdown.getShippingMethod(),
                breakdown.getEstimatedTransitDays(),
                breakdown.isFreeShippingApplied(),
                breakdown.isDiscountCapped(),
                breakdown.getAppliedCouponCode());
    }

    public static ProductResponse toProductResponse(Product product) {
        String category = product.getCategory() != null ? product.getCategory().getName() : null;
        return new ProductResponse(
                product.getSku(),
                product.getName(),
                product.getUnitPrice(),
                product.getCurrencyCode(),
                product.getWeightKg(),
                category,
                product.isDigital(),
                product.isActive());
    }

    public static CustomerResponse toCustomerResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getLoyaltyTier(),
                customer.getLifetimeSpend(),
                customer.isTaxExempt());
    }
}
