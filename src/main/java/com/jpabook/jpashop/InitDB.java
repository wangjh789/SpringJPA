package com.jpabook.jpashop;

import com.jpabook.jpashop.domain.*;
import com.jpabook.jpashop.domain.item.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

@Component
// 컴포넌트 스캔으로 스프링이 빈을 등록해줌
@RequiredArgsConstructor
public class InitDB {
    private final InitService initService;

    @PostConstruct
    //스프링이 다 올라온 다음에 실행
    public void init(){
        initService.dbInit1();
        initService.dbInit2();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService{

        private final EntityManager em;
        public void dbInit1(){
            Member member = createMember("userA");

            Book book1 = createBook("JPA1 book", 10000);

            Book book2 = createBook("JPA2 book", 20000);


            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 10000, 1);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 20000, 2);

            Delivery delivery = new Delivery();
            delivery.setAddress(member.getAddress());
            List<OrderItem> orderItems = new ArrayList<>();
            orderItems.add(orderItem1);
            orderItems.add(orderItem2);
            Order order = Order.createOrder(member, delivery, orderItems);
            em.persist(order);
        }

        public void dbInit2(){
            Member member = createMember("userB");

            Book book1 = createBook("Spring1 book", 10000);

            Book book2 = createBook("Spring2 book", 20000);


            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 10000, 1);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 20000, 2);

            Delivery delivery = new Delivery();
            delivery.setAddress(member.getAddress());
            List<OrderItem> orderItems = new ArrayList<>();
            orderItems.add(orderItem1);
            orderItems.add(orderItem2);
            Order order = Order.createOrder(member, delivery, orderItems);
            em.persist(order);
        }

        private Book createBook(String JPA1_book, int price) {
            Book book1 = new Book();
            book1.setName(JPA1_book);
            book1.setStockQuantity(1000);
            book1.setPrice(price);
            em.persist(book1);
            return book1;
        }

        private Member createMember(String username) {
            Member member = new Member();
            member.setName(username);
            member.setAddress(new Address("서울","1","1111"));
            em.persist(member);
            return member;
        }


    }
}
