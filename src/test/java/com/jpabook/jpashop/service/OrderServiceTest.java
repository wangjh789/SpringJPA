package com.jpabook.jpashop.service;

import com.jpabook.jpashop.domain.Address;
import com.jpabook.jpashop.domain.Member;
import com.jpabook.jpashop.domain.Order;
import com.jpabook.jpashop.domain.OrderStatus;
import com.jpabook.jpashop.domain.item.Book;
import com.jpabook.jpashop.exception.NotEnoughStockException;
import com.jpabook.jpashop.repository.OrderRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class OrderServiceTest {

    @Autowired
    EntityManager em;
    @Autowired OrderService orderService;
    @Autowired
    OrderRepository orderRepository;

    @Test
    public void 상품주문() throws Exception {
        //given
        Member member = createMember("user1");

        Book book = createBook("시골 JPA",10000, 10);

        int orderCount = 3;
        Map<Long,Integer> itemMap = new HashMap<>();
        itemMap.put(book.getId(), orderCount);

        //when
        Long orderId = orderService.order(member.getId(), itemMap);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals(OrderStatus.ORDER,getOrder.getStatus(),"상품 주문시 상태는 ORDER");
        assertEquals(getOrder.getOrderItems().size(),1,"주문한 상품 종류 수가 정확해야 한다.");
        assertEquals(10000*orderCount,getOrder.getTotalPrice(),"주문 가격은 가격 * 수량");
        assertEquals(7,book.getStockQuantity(),"주문 수량만큼 재고가 줄어야 한다.");

    }

    @Test
    public void 주문취소() throws Exception {
        //given
        Member member = createMember("user1");
        Book book = createBook("시골JPA", 10000, 10);

        int orderCount = 3;
        Map<Long,Integer> itemMap = new HashMap<>();
        itemMap.put(book.getId(), orderCount);
        Long orderId = orderService.order(member.getId(), itemMap);

        //when
        orderService.cancelOrder(orderId);

        //then
        Order getOrder = orderRepository.findOne(orderId);
        assertEquals(OrderStatus.CANCEL,getOrder.getStatus(),"주문 취소된 주문의 상태는 CANCEL");
        assertEquals(10,book.getStockQuantity(),"주문 취소된 아이템의 수량이 원래대로 돌아와야 한다.");
    }

    @Test(expected = NotEnoughStockException.class)
    public void 상품주문_재고수량_초과() throws Exception {
        //given
        Member member = createMember("user1");
        Book book = createBook("시골 JPA",10000,10);
        int orderCount = 11;
        Map<Long,Integer> itemMap = new HashMap<>();
        itemMap.put(book.getId(), orderCount);

        //when
        Long order = orderService.order(member.getId(), itemMap);

        //then
        fail("재고 수량 부족 예외가 발생해야 한다.");
    }

    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member createMember(String name) {
        Member member = new Member();
        member.setName(name);
        member.setAddress(new Address("서울","경기","123-123"));
        em.persist(member);
        return member;
    }

}