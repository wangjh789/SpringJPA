package com.jpabook.jpashop.api;

import com.jpabook.jpashop.domain.Address;
import com.jpabook.jpashop.domain.Order;
import com.jpabook.jpashop.domain.OrderItem;
import com.jpabook.jpashop.domain.OrderStatus;
import com.jpabook.jpashop.repository.OrderRepository;
import com.jpabook.jpashop.repository.OrderSearch;
import com.jpabook.jpashop.repository.order.query.OrderFlatDto;
import com.jpabook.jpashop.repository.order.query.OrderQueryDto;
import com.jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    @GetMapping("/api/v1/orders")
    //hibernate5Module에 의해 초기화된 (프록시 객체가 아닌)엔티티만 api로 출력한다.
    //엔티티 직접 노출
    public List<Order> orderV1(){
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) { //강제 프록시 객체 초기화
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(i->i.getItem().getName());
        }
        return all;
    }

    @GetMapping("/api/v2/orders")
    // 모든것을 dto로 래핑해서 반환
    // 클라이언트 입장에서 dto로 래핑하면 flat하게 편하게 사용할 수 있다.
    // address와 같은 value 오브젝트는 노출해도 됨
    // 컬렉션 조인 의 경우 쿼리가 상당히 많이 수행됨 -> 최적화 필요
    public List<OrderDto> oversV2(){
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        return orders.stream().map(OrderDto::new).collect(Collectors.toList());
    }

    @GetMapping("api/v3/orders")
    // 데이터가 뻥튀기 되어 반환값에 중복을 포함함 -> distinct 사용 (중복을 걸러 컬렉션에 넣어준다. db의 결과값은 여전함)
    // 1대 다 를 페치조인 하는 순간 페이징이 불가능함! -> 메모리에 걸과값을 다 넣은 다음 페이징을 실행함
    // db 쿼리의 결과값이 뻥튀기 되어 페이징 기준이 다 틀어짐
    // 컬렉션 페치 조인은 한번만 사용해야 함
    public List<OrderDto> ordersV3(){
        List<Order> orders = orderRepository.findAllWithItem();
        return orders.stream().map(OrderDto::new).collect(Collectors.toList());
    }

    @GetMapping("api/v3.1/orders")
    // xxTO ONE 관계는 fetch join으로 우선 조회하고 컬렉션은 지연 로딩으로 조회해라 -> row 수에 영향을 미치지 않으므로 페이징 가능
    // ymal에 default_batch_fetch_size -> in 절의 사이즈 설정
    // lazy loading 돌 때 in 절로 테이블당 쿼리 한번으로 땡겨옴 -> n+1 문제 해결을 위해
    // 10개씩 10번이든 100개씩 1번이든 메모리 사용량은 똑같다.
    // 10개씩 10번 : 부하는 줄지만 오래 걸림 / 100개씩 1번 : 부하가 가지만 짧게 걸림
    public List<OrderDto> ordersV3_page(
            @RequestParam(value = "offset",defaultValue = "0") int offset,
            @RequestParam(value = "limit",defaultValue = "100") int limit
            ){
        List<Order> all = orderRepository.findAllWithMemberDelivery(offset,limit);

        List<OrderDto> result = all.stream().map(OrderDto::new).collect(Collectors.toList());

        return result;
    }
    @GetMapping("/api/v4/orders")
    // dto로 직접 조회
    // xxToOne 관계를 먼저 불러옴 -> root 쿼리 실행 row수가 일정하므로
    // xxToMany는 별도의 메서드로 조회 -> 1 + N 번 수행됨 (컬렉션의 size만큼, order만큼 실행됨)
    public List<OrderQueryDto> ordersV4(){
        return orderQueryRepository.findOrderQueryDtos();
    }

    @GetMapping("/api/v5/orders")
    // dto 직접 조회 최적화 -> order 2개의 id 들을 뽑아와서 in 절로 해당되는 orderitem 들을 가져와 그룹화한 후 맵으로 만들어 order에 넣음
    // map을 사용해서 매칭 성능 O(1)
    public List<OrderQueryDto> orderV5(){
        return orderQueryRepository.findAllByDto_optimization();
    }
    @GetMapping("/api/v6/orders")
    public List<OrderFlatDto> orderV6(){
        return orderQueryRepository.findAllByDto_flat();
    }

    @Data
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream().map(OrderItemDto::new).collect(Collectors.toList());
        }
    }
    @Data
    static class OrderItemDto{

        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
