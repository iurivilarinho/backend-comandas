package com.br.food.enums;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;

public final class Types {

    private Types() {
    }

    @Schema(description = "Order channel")
    public enum OrderChannel {
        @Schema(description = "Dine-in order")
        DINE_IN,
        @Schema(description = "Delivery order")
        DELIVERY
    }

    @Schema(description = "Product type")
    public enum ProductType {
        @Schema(description = "Stock ingredient")
        INGREDIENT,
        @Schema(description = "Sellable final product")
        FINISHED
    }

    @Schema(description = "Promotion type")
    public enum PromotionType {
        @Schema(description = "Discount promotion")
        DISCOUNT,
        @Schema(description = "General promotion")
        PROMOTION,
        @Schema(description = "Other type")
        OTHER
    }

    @Schema(description = "Payment method")
    public enum PaymentMethod {
        @Schema(description = "Card payment")
        CARD,
        @Schema(description = "Cash payment")
        CASH,
        @Schema(description = "Instant transfer")
        PIX,
        @Schema(description = "Invoice payment")
        INVOICE
    }

    @Schema(description = "Order status")
    public enum OrderStatus {
        @Schema(description = "Open order receiving items and service operations")
        OPEN("Open"),
        @Schema(description = "Order fully prepared and ready for checkout")
        READY_TO_CLOSE("Ready to close"),
        @Schema(description = "Order closed after full payment")
        CLOSED("Closed"),
        @Schema(description = "Order canceled")
        CANCELED("Canceled");

        private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = buildTransitions();

        private final String description;

        OrderStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public boolean canTransitionTo(OrderStatus target) {
            return TRANSITIONS.getOrDefault(this, Collections.emptySet()).contains(target);
        }

        public static void validateTransition(OrderStatus current, OrderStatus target) {
            if (current == null || current == target) {
                return;
            }
            if (!current.canTransitionTo(target)) {
                throw new IllegalStateException(
                        "Invalid order status transition from " + current + " to " + target + ".");
            }
        }

        private static Map<OrderStatus, Set<OrderStatus>> buildTransitions() {
            Map<OrderStatus, Set<OrderStatus>> transitions = new EnumMap<>(OrderStatus.class);
            transitions.put(OPEN, Set.of(READY_TO_CLOSE, CANCELED));
            transitions.put(READY_TO_CLOSE, Set.of(OPEN, CLOSED, CANCELED));
            transitions.put(CLOSED, Collections.emptySet());
            transitions.put(CANCELED, Collections.emptySet());
            return Collections.unmodifiableMap(transitions);
        }
    }

    @Schema(description = "Order item status")
    public enum OrderItemStatus {
        @Schema(description = "Item received by service")
        RECEIVED("Received"),
        @Schema(description = "Item queued by kitchen")
        QUEUED("Queued"),
        @Schema(description = "Item in preparation")
        IN_PREPARATION("In preparation"),
        @Schema(description = "Item ready for service")
        READY("Ready"),
        @Schema(description = "Item served to the guest")
        SERVED("Served"),
        @Schema(description = "Declined item")
        DECLINED("Declined"),
        @Schema(description = "Canceled item")
        CANCELED("Canceled");

        private static final Map<OrderItemStatus, Set<OrderItemStatus>> TRANSITIONS = buildTransitions();

        private final String description;

        OrderItemStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public boolean canTransitionTo(OrderItemStatus target) {
            return TRANSITIONS.getOrDefault(this, Collections.emptySet()).contains(target);
        }

        public static void validateTransition(OrderItemStatus current, OrderItemStatus target) {
            if (current == null || current == target) {
                return;
            }
            if (!current.canTransitionTo(target)) {
                throw new IllegalStateException(
                        "Invalid order item status transition from " + current + " to " + target + ".");
            }
        }

        private static Map<OrderItemStatus, Set<OrderItemStatus>> buildTransitions() {
            Map<OrderItemStatus, Set<OrderItemStatus>> transitions = new EnumMap<>(OrderItemStatus.class);
            transitions.put(RECEIVED, Set.of(QUEUED, DECLINED, CANCELED));
            transitions.put(QUEUED, Set.of(IN_PREPARATION, DECLINED, CANCELED));
            transitions.put(IN_PREPARATION, Set.of(READY, DECLINED, CANCELED));
            transitions.put(READY, Set.of(SERVED, CANCELED));
            transitions.put(SERVED, Collections.emptySet());
            transitions.put(DECLINED, Collections.emptySet());
            transitions.put(CANCELED, Collections.emptySet());
            return Collections.unmodifiableMap(transitions);
        }
    }

    @Schema(description = "Supply invoice status")
    public enum SupplyInvoiceStatus {
        @Schema(description = "Allocated invoice")
        ALLOCATED("Allocated"),
        @Schema(description = "In consumption")
        IN_CONSUMPTION("In consumption"),
        @Schema(description = "Consumed")
        CONSUMED("Consumed"),
        @Schema(description = "Canceled")
        CANCELED("Canceled");

        private static final Map<SupplyInvoiceStatus, Set<SupplyInvoiceStatus>> TRANSITIONS = buildTransitions();

        private final String description;

        SupplyInvoiceStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public boolean canTransitionTo(SupplyInvoiceStatus target) {
            return TRANSITIONS.getOrDefault(this, Collections.emptySet()).contains(target);
        }

        public static void validateTransition(SupplyInvoiceStatus current, SupplyInvoiceStatus target) {
            if (current == null || current == target) {
                return;
            }
            if (!current.canTransitionTo(target)) {
                throw new IllegalStateException(
                        "Invalid supply invoice status transition from " + current + " to " + target + ".");
            }
        }

        private static Map<SupplyInvoiceStatus, Set<SupplyInvoiceStatus>> buildTransitions() {
            Map<SupplyInvoiceStatus, Set<SupplyInvoiceStatus>> transitions = new EnumMap<>(SupplyInvoiceStatus.class);
            transitions.put(ALLOCATED, Set.of(IN_CONSUMPTION, CANCELED));
            transitions.put(IN_CONSUMPTION, Set.of(CONSUMED));
            transitions.put(CONSUMED, Collections.emptySet());
            transitions.put(CANCELED, Collections.emptySet());
            return Collections.unmodifiableMap(transitions);
        }
    }
}
