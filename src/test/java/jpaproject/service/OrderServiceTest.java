package jpaproject.service;

import jakarta.persistence.EntityManager;
import jpaproject.domain.Address;
import jpaproject.domain.Member;
import jpaproject.domain.Order;
import jpaproject.domain.OrderStatus;
import jpaproject.domain.item.Book;
import jpaproject.domain.item.Item;
import jpaproject.exception.NotEnoughStockException;
import jpaproject.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    EntityManager em;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Test
    @DisplayName("상품 주문 테스트")
    void 상품_주문() {
        // given
        Member member = createMember();

        Book book = createbook("JPA", 10000, 10);

        int orderCount = 2;

        // when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        // then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals(OrderStatus.ORDER, getOrder.getStatus());
        assertEquals(1, getOrder.getOrderItems().size());
        assertEquals(10000 * orderCount, getOrder.getTotalPrice());
        assertEquals(8, book.getStockQuantity());
    }



    @Test
    @DisplayName("재고 수량 초과 테스트")
    void 상품주문_재고수량초과() {
        // given
        Member member = createMember();
        Item item = createbook("JPA", 10000, 10);

        int orderCount = 11;
        
        // when, then
        assertThrows(NotEnoughStockException.class, () -> orderService.order(member.getId(), item.getId(), orderCount));
    }
    
    @Test
    @DisplayName("주문 취소 테스트")
    void 주문취소() {
        // given
        Member member = createMember();
        Item item = createbook("JPA", 10000, 10);
        int orderCount = 2;

        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

        // when
        orderService.cancelOrder(orderId);
        
        // then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals(OrderStatus.CANCEL, getOrder.getStatus());
        assertEquals(10, item.getStockQuantity());
    }

    private Book createbook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "강가", "123-123"));
        em.persist(member);
        return member;
    }
}