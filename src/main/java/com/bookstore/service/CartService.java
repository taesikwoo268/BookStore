package com.bookstore.service;

import com.bookstore.dto.request.AddToCartRequest;
import com.bookstore.dto.request.UpdateCartItemRequest;
import com.bookstore.dto.response.CartResponse;
import com.bookstore.exception.BookNotFoundException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapper.CartMapper;
import com.bookstore.model.Book;
import com.bookstore.model.Cart;
import com.bookstore.model.CartItem;
import com.bookstore.model.User;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.CartItemRepository;
import com.bookstore.repository.CartRepository;
import com.bookstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    // ============================================================
    // 1. GET CART
    // ============================================================

    @Transactional
    public CartResponse getCart(Long userId) {
        log.info("🛒 Getting cart for user: {}", userId);

        Cart cart = getOrCreateCart(userId);
        return cartMapper.toCartResponse(cart);
    }

    // ============================================================
    // 2. ADD ITEM TO CART
    // ============================================================

    @Transactional
    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        log.info("🛒 Adding item to cart for user: {}, bookId: {}, quantity: {}",
                userId, request.getBookId(), request.getQuantity());

        Cart cart = getOrCreateCart(userId);

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + request.getBookId()));

        if (book.getStock() < request.getQuantity()) {
            throw new RuntimeException("Not enough stock. Available: " + book.getStock());
        }

        CartItem existingItem = cartItemRepository.findByCartIdAndBookId(cart.getId(), book.getId())
                .orElse(null);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            if (book.getStock() < newQuantity) {
                throw new RuntimeException("Not enough stock. Available: " + book.getStock() + ", Request: " + newQuantity);
            }
            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);
            log.info("✅ Updated cart item: {}, new quantity: {}", existingItem.getId(), newQuantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .book(book)
                    .quantity(request.getQuantity())
                    .build();
            cartItemRepository.save(newItem);
            log.info("✅ Added new item to cart: {}", newItem.getId());
        }

        return cartMapper.toCartResponse(cart);
    }

    // ============================================================
    // 3. UPDATE CART ITEM
    // ============================================================

    @Transactional
    public CartResponse updateCartItem(Long userId, Long itemId, UpdateCartItemRequest request) {
        log.info("🛒 Updating cart item: {} for user: {}, quantity: {}",
                itemId, userId, request.getQuantity());

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new RuntimeException("You do not have permission to modify this cart item");
        }

        Book book = cartItem.getBook();
        if (book.getStock() < request.getQuantity()) {
            throw new RuntimeException("Not enough stock. Available: " + book.getStock());
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);
        log.info("✅ Updated cart item: {}, new quantity: {}", itemId, request.getQuantity());

        Cart cart = cartItem.getCart();
        return cartMapper.toCartResponse(cart);
    }

    // ============================================================
    // 4. DELETE CART ITEM
    // ============================================================

    @Transactional
    public CartResponse deleteCartItem(Long userId, Long itemId) {
        log.info("🗑️ Deleting cart item: {} for user: {}", itemId, userId);

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new RuntimeException("You do not have permission to delete this cart item");
        }

        Cart cart = cartItem.getCart();
        cartItemRepository.delete(cartItem);
        cart.getCartItems().remove(cartItem);
        log.info("✅ Deleted cart item: {}", itemId);
        return cartMapper.toCartResponse(cart);
    }

    // ============================================================
    // 5. CLEAR CART
    // ============================================================

    @Transactional
    public CartResponse clearCart(Long userId) {
        log.info("🗑️ Clearing cart for user: {}", userId);

        Cart cart = getOrCreateCart(userId);
        cartItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();
        log.info("✅ Cart cleared for user: {}", userId);

        return cartMapper.toCartResponse(cart);
    }

    // ============================================================
    // 6. HELPERS
    // ============================================================

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(userId));
    }

    private Cart createCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Cart cart = Cart.builder()
                .user(user)
                .build();

        Cart savedCart = cartRepository.save(cart);
        log.info("✅ Created new cart for user: {}", userId);
        return savedCart;
    }
}