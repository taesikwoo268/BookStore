package com.bookstore.service;

import com.bookstore.dto.request.CheckoutRequest;
import com.bookstore.dto.response.CheckoutResponse;
import com.bookstore.dto.response.OrderItemResponse;
import com.bookstore.dto.response.PaymentResponse;
import com.bookstore.enums.PaymentMethod;
import com.bookstore.enums.PaymentStatus;
import com.bookstore.exception.BookNotFoundException;
import com.bookstore.exception.InsufficientStockException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.model.*;
import com.bookstore.enums.OrderStatus;
import com.bookstore.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final StockService stockService;

    @Retryable(
            value = {OptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    @Transactional
    public CheckoutResponse checkout(Long userId, CheckoutRequest request) {
        log.info("🛒 Starting checkout for user: {}", userId);

        Cart cart = validateCart(userId);
        checkInventory(cart);

        Order order = createOrder(userId, cart, request);
        deductStock(cart);
        Payment payment = createPayment(order, request);
        clearCart(cart);
        return buildCheckoutResponse(order, payment);
    }

    private Cart validateCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Cart is empty for user: " + userId);
        }
        for(CartItem item : cart.getCartItems()) {
            if(item.getBook() == null) {
                throw new RuntimeException("Book not found for cart item: " + item.getId());
            }
        }
        return cart;
    }

    private void checkInventory(Cart cart) {
        for (CartItem item : cart.getCartItems()) {
            Book book = item.getBook();
            if (book.getStock() < item.getQuantity()) {
                throw new InsufficientStockException(
                        "Not enough stock for book: " + book.getTitle() +
                                ". Requested: " + item.getQuantity() +
                                ", Available: " + book.getStock()
                );
            }
        }
    }

    private Order createOrder(Long userId, Cart cart, CheckoutRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .orderDate(LocalDateTime.now())
                .shippingAddress(request.getShippingAddress())
                .build();

        List<OrderItem> orderItems = cart.getCartItems().stream().map(cartItem -> {
            Book book = cartItem.getBook();
            OrderItem orderItem = OrderItem.builder()
                    .book(book)
                    .quantity(cartItem.getQuantity())
                    .price(book.getPrice())
                    .build();
            orderItem.setOrder(order);
            return orderItem;
        }).toList();
        order.setOrderItems(orderItems);
        order.calculateTotal();

        return orderRepository.save(order);
    }

    private void deductStock(Cart cart) {
        for(CartItem item : cart.getCartItems()) {
            Book book = item.getBook();
            stockService.deductStockWithRetry(book.getId(), item.getQuantity());
            log.info("📚 Stock updated: {} -> {}, sales: {}",
                    book.getTitle(), book.getStock(), book.getSalesCount());
        }
    }

    private Payment createPayment(Order order, CheckoutRequest request) {
        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .method(PaymentMethod.fromString(request.getPaymentMethod()))
                .status(PaymentStatus.PENDING)
                .transactionId("TXN-" + UUID.randomUUID().toString())
                .paymentDate(LocalDateTime.now())
                .build();
        return paymentRepository.save(payment);
    }

    private void clearCart(Cart cart) {
        cartItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();
        log.info("🗑️ Cart cleared for user: {}", cart.getUser().getId());
    }

    private CheckoutResponse buildCheckoutResponse(Order order,Payment payment) {
       List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
               .map(orderItem -> OrderItemResponse.builder()
                       .bookId(orderItem.getBook().getId())
                       .bookTitle(orderItem.getBook().getTitle())
                       .bookIsbn(orderItem.getBook().getIsbn())
                       .quantity(orderItem.getQuantity())
                       .price(orderItem.getPrice())
                       .subtotal(orderItem.getSubtotal())
                       .build())
               .collect(Collectors.toList());

        PaymentResponse paymentResponse = PaymentResponse.builder()
                .paymentId(payment.getId())
                .amount(payment.getAmount())
                .method(payment.getMethod().toString())
                .status(payment.getStatus().toString())
                .transactionId(payment.getTransactionId())
                .paymentDate(payment.getPaymentDate())
                .build();
        return CheckoutResponse.builder()
                .orderId(order.getId())
                .orderNumber("ORD-" + order.getId())
                .userId(order.getUser().getId())
                .items(itemResponses)
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .shippingAddress(order.getShippingAddress())
                .payment(paymentResponse)
                .message("Order placed successfully!")
                .build();
    }
}