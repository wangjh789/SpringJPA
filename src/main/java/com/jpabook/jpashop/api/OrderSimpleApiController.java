package com.jpabook.jpashop.api;

import com.jpabook.jpashop.domain.Address;
import com.jpabook.jpashop.domain.Order;
import com.jpabook.jpashop.domain.OrderStatus;
import com.jpabook.jpashop.repository.OrderRepository;
import com.jpabook.jpashop.repository.OrderSearch;
import com.jpabook.jpashop.repository.OrderSimpleQueryDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xx TO ONE
 * order
 * order -> Member
 * order -> delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {
    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/simple-orders")
    //jackson 패키지 member ->  orders , order->member 무한반복 양방향 연관관계 문제 발생
    // 한쪽은 JsonIgnore 어노테이션 사용
    //프록시 멤버 객체(ByteBuddy interceptor)를 넣어둠, 레이지 로딩 -> 프록시를 초기화한다. json는 이 사실을 모름, 에러
    //엔티티를 외부에 노출 -> 불필요한 데이터도 노출하게 되어 나중에 수정하기 힘들어짐
    public List<Order> ordersV1(){
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for(Order order:all){
            order.getMember().getName(); //강제 초기화
            order.getDelivery().getAddress(); //강제 초기화
        }
        return all;
    }

    @GetMapping("/api/v2/simple-orders")
    // dto 사용하면 api 스펙에 맞춰 개발이 가능
    // order의 수만큼 쿼리가 날아감, lazy 로딩의 원리
    public List<SimpleOrderDto> orderV2(){
        //order 2개
        // n+1 문제 발생
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        return orders.stream().map(SimpleOrderDto::new).collect(Collectors.toList());
    }

    @GetMapping("/api/v3/simple-orders")
    //페치 조인 member 와 delivery를 한번에 땡겨옴
    // 범용성 있는 쿼리
    // 단점이 있다면 조인되는 테이블의 모든 컬럼을 select절에 엔티티를 다 찍어서 가져온다 -> 네트웍을 많이 사용
    public List<SimpleOrderDto> orderV3(){
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        return orders.stream().map(SimpleOrderDto::new).collect(Collectors.toList());
    }

    @GetMapping("/api/v4/simple-orders")
    // dto에 맞춘 쿼리를 작성해 조인된 테이블 중 dto 속성만 조회
    // 재사용성이 없어 재사용이 거의 불가능 하다.
    // api 스펙에 맞춘 코드가 리파지토리에 들어감
    // 리파지토리는 엔티티의 객체 그래프들을 조회하는데 사용해야 함
    // 네트웍은 보통 join의 부분에서 많이 발생하고 select 절은 미비하다.
    public List<OrderSimpleQueryDto> orderV4(){
        return orderRepository.findOrderDtos();
    }

    @Data
    static class SimpleOrderDto{
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order){
            orderId = order.getId();
            name = order.getMember().getName(); //lazy 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); //lazy 초기화
        }
    }
}
