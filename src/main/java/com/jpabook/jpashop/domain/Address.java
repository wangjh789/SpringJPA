package com.jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Embeddable;

@Embeddable
@Getter
//값 타입은 변경 불가능한 클래스를 만들어야한다.
public class Address {

    private String city;
    private String street;
    private String zipcode;

    //jpa의 제약 기본 생성자를 만들어야함 리플렉션이나 프록시 기술을 쓰기위해
    protected Address(){}

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
