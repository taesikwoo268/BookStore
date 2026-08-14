package com.bookstore.service;

import com.bookstore.enums.OrderStatus;
import com.bookstore.exception.InvalidStateTransitionException;
import com.bookstore.model.Order;
import com.bookstore.model.User;
import com.bookstore.repository.OrderRepository;
import com.bookstore.strategy.OrderStateTransitionFactory;
import com.bookstore.strategy.state.OrderStateTransition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderStateMachineService Unit Tests")
class OrderStateMachineServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderStateTransitionFactory transitionFactory;

    @Mock
    private OrderStateTransition mockTransition;

    @InjectMocks
    private OrderStateMachineService stateMachineService;

    private Order testOrder;
    private Long testOrderId = 1L;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@email.com")
                .build();

        testOrder = Order.builder()
                .id(testOrderId)
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("99.99"))
                .orderDate(LocalDateTime.now())
                .shippingAddress("123 Test St")
                .build();
    }

    // ============================================================
    // 1. VALID TRANSITIONS - SUCCESS
    // ============================================================

    @Test
    @DisplayName("confirmOrder - PENDING → CONFIRMED should succeed")
    void confirmOrder_validTransition_shouldSucceed() {
        // Given
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(transitionFactory.isValidTransition(OrderStatus.PENDING, OrderStatus.CONFIRMED))
                .thenReturn(true);
        when(transitionFactory.getTransition(OrderStatus.PENDING, OrderStatus.CONFIRMED))
                .thenReturn(mockTransition);

        Order confirmedOrder = Order.builder()
                .status(OrderStatus.CONFIRMED)
                .confirmedAt(LocalDateTime.now())
                .build();

        when(mockTransition.execute(any(Order.class), eq(OrderStatus.CONFIRMED), anyString()))
                .thenReturn(confirmedOrder);

        // When
        Order result = stateMachineService.confirmOrder(testOrderId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(orderRepository).findById(testOrderId);
        verify(transitionFactory).isValidTransition(OrderStatus.PENDING, OrderStatus.CONFIRMED);
        verify(transitionFactory).getTransition(OrderStatus.PENDING, OrderStatus.CONFIRMED);
        verify(mockTransition).execute(any(Order.class), eq(OrderStatus.CONFIRMED), anyString());
    }

    @Test
    @DisplayName("processOrder - CONFIRMED → PROCESSING should succeed")
    void processOrder_validTransition_shouldSucceed() {
        // Given
        testOrder.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(transitionFactory.isValidTransition(OrderStatus.CONFIRMED, OrderStatus.PROCESSING))
                .thenReturn(true);
        when(transitionFactory.getTransition(OrderStatus.CONFIRMED, OrderStatus.PROCESSING))
                .thenReturn(mockTransition);

        Order processingOrder = Order.builder()
                .status(OrderStatus.PROCESSING)
                .processingAt(LocalDateTime.now())
                .build();

        when(mockTransition.execute(any(Order.class), eq(OrderStatus.PROCESSING), anyString()))
                .thenReturn(processingOrder);

        // When
        Order result = stateMachineService.processOrder(testOrderId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PROCESSING);
    }

    @Test
    @DisplayName("shipOrder - PROCESSING → SHIPPED should succeed")
    void shipOrder_validTransition_shouldSucceed() {
        // Given
        testOrder.setStatus(OrderStatus.PROCESSING);

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(transitionFactory.isValidTransition(OrderStatus.PROCESSING, OrderStatus.SHIPPED))
                .thenReturn(true);
        when(transitionFactory.getTransition(OrderStatus.PROCESSING, OrderStatus.SHIPPED))
                .thenReturn(mockTransition);

        Order shippedOrder = Order.builder()
                .status(OrderStatus.SHIPPED)
                .shippedAt(LocalDateTime.now())
                .build();

        when(mockTransition.execute(any(Order.class), eq(OrderStatus.SHIPPED), anyString()))
                .thenReturn(shippedOrder);

        // When
        Order result = stateMachineService.shipOrder(testOrderId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    @DisplayName("deliverOrder - SHIPPED → DELIVERED should succeed")
    void deliverOrder_validTransition_shouldSucceed() {
        // Given
        testOrder.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(transitionFactory.isValidTransition(OrderStatus.SHIPPED, OrderStatus.DELIVERED))
                .thenReturn(true);
        when(transitionFactory.getTransition(OrderStatus.SHIPPED, OrderStatus.DELIVERED))
                .thenReturn(mockTransition);

        Order deliveredOrder = Order.builder()
                .status(OrderStatus.DELIVERED)
                .deliveredAt(LocalDateTime.now())
                .build();

        when(mockTransition.execute(any(Order.class), eq(OrderStatus.DELIVERED), anyString()))
                .thenReturn(deliveredOrder);

        // When
        Order result = stateMachineService.deliverOrder(testOrderId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    @DisplayName("cancelOrder - PENDING → CANCELLED should succeed")
    void cancelOrder_validTransition_shouldSucceed() {
        // Given
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(transitionFactory.isValidTransition(OrderStatus.PENDING, OrderStatus.CANCELLED))
                .thenReturn(true);
        when(transitionFactory.getTransition(OrderStatus.PENDING, OrderStatus.CANCELLED))
                .thenReturn(mockTransition);

        Order cancelledOrder = Order.builder()
                .status(OrderStatus.CANCELLED)
                .cancelledAt(LocalDateTime.now())
                .cancelledReason("User changed mind")
                .build();

        when(mockTransition.execute(any(Order.class), eq(OrderStatus.CANCELLED), eq("User changed mind")))
                .thenReturn(cancelledOrder);

        // When
        Order result = stateMachineService.cancelOrder(testOrderId, "User changed mind");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(result.getCancelledReason()).isEqualTo("User changed mind");
    }

    // ============================================================
    // 2. INVALID TRANSITIONS - EXCEPTIONS
    // ============================================================

    @Test
    @DisplayName("confirmOrder - Invalid transition should throw exception")
    void confirmOrder_invalidTransition_shouldThrowException() {
        // Given
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(transitionFactory.isValidTransition(OrderStatus.PENDING, OrderStatus.CONFIRMED))
                .thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> stateMachineService.confirmOrder(testOrderId))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Invalid state transition")
                .hasMessageContaining("PENDING")
                .hasMessageContaining("CONFIRMED");

        verify(transitionFactory, never()).getTransition(any(), any());
        verify(mockTransition, never()).execute(any(), any(), any());
    }

    @Test
    @DisplayName("deliverOrder - PENDING → DELIVERED invalid should throw exception")
    void deliverOrder_invalidTransition_shouldThrowException() {
        // Given
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(transitionFactory.isValidTransition(OrderStatus.PENDING, OrderStatus.DELIVERED))
                .thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> stateMachineService.deliverOrder(testOrderId))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Invalid state transition");
    }

    @Test
    @DisplayName("cancelOrder - Non-cancellable status should throw exception")
    void cancelOrder_nonCancellableStatus_shouldThrowException() {
        // Given
        testOrder.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

        // When & Then
        assertThatThrownBy(() -> stateMachineService.cancelOrder(testOrderId, "Cannot cancel"))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("cannot be cancelled");
    }

    // ============================================================
    // 3. ORDER NOT FOUND - EXCEPTIONS
    // ============================================================

    @Test
    @DisplayName("confirmOrder - Order not found should throw exception")
    void confirmOrder_orderNotFound_shouldThrowException() {
        // Given
        Long nonExistentId = 999L;
        when(orderRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> stateMachineService.confirmOrder(nonExistentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    @DisplayName("processOrder - Order not found should throw exception")
    void processOrder_orderNotFound_shouldThrowException() {
        // Given
        Long nonExistentId = 999L;
        when(orderRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> stateMachineService.processOrder(nonExistentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    @DisplayName("shipOrder - Order not found should throw exception")
    void shipOrder_orderNotFound_shouldThrowException() {
        // Given
        Long nonExistentId = 999L;
        when(orderRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> stateMachineService.shipOrder(nonExistentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found");
    }

    // ============================================================
    // 4. UTILITY METHODS - canTransitionTo
    // ============================================================

    @Test
    @DisplayName("canTransitionTo - valid transition should return true")
    void canTransitionTo_validTransition_shouldReturnTrue() {
        // Given
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(transitionFactory.isValidTransition(OrderStatus.PENDING, OrderStatus.CONFIRMED))
                .thenReturn(true);

        // When
        boolean result = stateMachineService.canTransitionTo(testOrderId, OrderStatus.CONFIRMED);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("canTransitionTo - invalid transition should return false")
    void canTransitionTo_invalidTransition_shouldReturnFalse() {
        // Given
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(transitionFactory.isValidTransition(OrderStatus.PENDING, OrderStatus.DELIVERED))
                .thenReturn(false);

        // When
        boolean result = stateMachineService.canTransitionTo(testOrderId, OrderStatus.DELIVERED);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("canTransitionTo - Order not found should throw exception")
    void canTransitionTo_orderNotFound_shouldThrowException() {
        // Given
        Long nonExistentId = 999L;
        when(orderRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> stateMachineService.canTransitionTo(nonExistentId, OrderStatus.CONFIRMED))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found");
    }

    // ============================================================
    // 5. EDGE CASES
    // ============================================================

    @Test
    @DisplayName("cancelOrder - with null reason should use default reason")
    void cancelOrder_nullReason_shouldUseDefaultReason() {
        // Given
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(transitionFactory.isValidTransition(OrderStatus.PENDING, OrderStatus.CANCELLED))
                .thenReturn(true);
        when(transitionFactory.getTransition(OrderStatus.PENDING, OrderStatus.CANCELLED))
                .thenReturn(mockTransition);

        Order cancelledOrder = Order.builder()
                .status(OrderStatus.CANCELLED)
                .cancelledAt(LocalDateTime.now())
                .cancelledReason("Cancelled by user")
                .build();

        when(mockTransition.execute(any(Order.class), eq(OrderStatus.CANCELLED), eq("Cancelled by user")))
                .thenReturn(cancelledOrder);

        // When
        Order result = stateMachineService.cancelOrder(testOrderId, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(result.getCancelledReason()).isEqualTo("Cancelled by user");
    }

    @Test
    @DisplayName("confirmOrder - should call repository save")
    void confirmOrder_shouldCallRepositorySave() {
        // Given
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(transitionFactory.isValidTransition(OrderStatus.PENDING, OrderStatus.CONFIRMED))
                .thenReturn(true);
        when(transitionFactory.getTransition(OrderStatus.PENDING, OrderStatus.CONFIRMED))
                .thenReturn(mockTransition);

        Order confirmedOrder = Order.builder()
                .status(OrderStatus.CONFIRMED)
                .confirmedAt(LocalDateTime.now())
                .build();

        when(mockTransition.execute(any(Order.class), eq(OrderStatus.CONFIRMED), anyString()))
                .thenReturn(confirmedOrder);

        // When
        stateMachineService.confirmOrder(testOrderId);

        // Then
        verify(mockTransition).execute(any(Order.class), eq(OrderStatus.CONFIRMED), anyString());
    }
}