package com.example.order_service.service;

import com.example.order_service.entity.Order;
import com.example.order_service.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;

    private static final String PRODUCT_SERVICE_URL = "http://localhost:8081/products/";

    // All orders
    public List<Order> getAllOrders() {
        logger.info("Fetching all orders");
        return orderRepository.findAll();
    }

    // Get order by ID
    public Optional<Order> getOrderById(Long id) {
        logger.info("Fetching order with id: {}", id);
        return orderRepository.findById(id);
    }

    public Order createOrder(Order order) {
        logger.info("Creating order for customer: {}", order.getCustomerName());
        try {
            for (Long productId : order.getProductIds()) {
                ResponseEntity<Object> response = restTemplate
                        .getForEntity(PRODUCT_SERVICE_URL + productId, Object.class);

                if (!response.getStatusCode().is2xxSuccessful()) {
                    logger.warn("Product with ID: {} not found.", productId);
                    throw new RuntimeException("Product with ID " + productId +" does not exist");
                }
                logger.info("Product with ID: {}", productId);
            }

//        return orderRepository.save(order);
            Order savedOrder = orderRepository.save(order);
            logger.info("Order create with ID: {}", savedOrder.getId());
            return savedOrder;
        }   catch (RestClientException ex) {
            logger.error("Failed to communicate with Product Service: {}", ex.getMessage());
            throw ex;
        }
    }
}
