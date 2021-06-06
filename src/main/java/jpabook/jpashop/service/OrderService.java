package jpabook.jpashop.service;

import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    /**
     * 주문
     */
    @Transactional
    public Long order(Long memberId, Long itemId, int count) {

        //엔티티 조회
        Member member = memberRepository.findOne(memberId);
        Item item = itemRepository.findOne(itemId);

        //배송정보 생성 (Cascade가 걸려있어서 자동으로 persist됨)
        // 이용하는 객체가 하나 일때만 사용하자, 라이프 사이클 고려해서 리팩토링
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());

        //주문상품 생성 (Cascade가 걸려있어서 자동으로 persist됨)
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

        /*  위와 같이 생성메서드를 사용하고도 아래와 같은 방법으로도 만들 시 유지보수가 힘들어진다
            생성 로직 2가지 사용 x 위와 같이 사용하는 것을 추천
            이를 막기 위해선 기본 생성자를 protected로 생성한다
        OrderItem orderItem1 = new OrderItem();
        orderItem1.setOrder();
        */

        //주문 생성
        Order order = Order.createOrder(member, delivery, orderItem);

        //주문 저장
        orderRepository.save(order);

        return order.getId();
    }

    //취소

    /**
     * 주문 취소
     */
    @Transactional
    public void cancelOrder(Long orderId) {

        //주문 엔티티 조회
        Order order = orderRepository.findOne(orderId);

        //주문 취소
        order.cancel(); //JPA의 강점 - 자동으로 엔티티의 수정된 값 update쿼리들이 날라감
    }

    //검색 -> 컨트롤러에 위임만 하는 메서드는 서비스로직을 만들지 않고 바로 컨트롤러에서 레포지토리를 호출하는 것이 나을수도!
    public List<Order> findOrders(OrderSearch orderSearch) {
        return orderRepository.findAllByString(orderSearch);
    }
}
