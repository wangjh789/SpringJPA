package com.jpabook.jpashop.repository;

import com.jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;
//  jpa 준영속 엔티티, 영속성 컨텍스트가 더이상 관리하지 않는 엔티티.
//  jpa는 식별자 기반으로 엔티티를 관리함
//  변경감지 기능(영속 엔티티로 끌어옴) 또는 merge를 통해

//  merge는 파라미터로 넘어온 객체로 싹 바꿔치기 함, 반환값만 영속 엔티티고 파라미터는 아직도 준영속 엔티티임.
//  변경 감지는 원하는 속성만 변경가능한데 merge는 작성하지 않은 필드는 null로 넣어버림
    public void save(Item item){
//        if(item.getId() == null){ //삽입 시
//            em.persist(item);
//        }else{ //수정 시
//            em.merge(item);
//        }
//  변경 감지로 적용
        em.persist(item);
    }

    public Item findOne(Long id){
        return em.find(Item.class,id);
    }

    public List<Item> findAll(){
        return em.createQuery("select i from Item i",Item.class)
                .getResultList();
    }
}
