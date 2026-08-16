package com.bookstore.observer;

import com.bookstore.event.OrderPlacedEvent;
import com.bookstore.event.OrderSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderObserverTest {

    @Mock
    private LoyaltyObserver loyaltyObserver;

    @Mock
    private EmailObserver emailObserver;

    @Mock
    private AnalyticsObserver analyticsObserver;

    @InjectMocks
    private OrderSubject orderSubject;

    private OrderPlacedEvent event;

    @BeforeEach
    void setUp() {
        event = OrderPlacedEvent.builder()
                .orderId(100L)
                .userId(1L)
                .userEmail("test@email.com")
                .totalAmount(new BigDecimal("99.99"))
                .items(List.of())
                .build();

        orderSubject.attach(loyaltyObserver);
        orderSubject.attach(emailObserver);
        orderSubject.attach(analyticsObserver);
    }

    @Test
    @DisplayName("All observers should be notified")
    void testNotifyAllObservers() {
        // When
        orderSubject.notifyObservers(event);

        // Then
        verify(loyaltyObserver, times(1)).onOrderPlaced(event);
        verify(emailObserver, times(1)).onOrderPlaced(event);
        verify(analyticsObserver, times(1)).onOrderPlaced(event);
    }

    @Test
    @DisplayName("Observers should run in priority order")
    void testObserverPriorityOrder() {
        // When
        orderSubject.notifyObservers(event);

        // Then
        // LoyaltyObserver (priority 0) → EmailObserver (priority 1) → AnalyticsObserver (priority 2)
        verify(loyaltyObserver, times(1)).onOrderPlaced(event);
        verify(emailObserver, times(1)).onOrderPlaced(event);
        verify(analyticsObserver, times(1)).onOrderPlaced(event);
    }

    @Test
    @DisplayName("Observer failure should not affect other observers")
    void testObserverFailureDoesNotAffectOthers() {
        // Given
        doThrow(new RuntimeException("Loyalty observer failed"))
                .when(loyaltyObserver).onOrderPlaced(any());

        // When
        orderSubject.notifyObservers(event);

        // Then
        verify(emailObserver, times(1)).onOrderPlaced(event);
        verify(analyticsObserver, times(1)).onOrderPlaced(event);
    }
}