package com.jpabook.jpashop.service;

import com.jpabook.jpashop.domain.Member;
import com.jpabook.jpashop.repository.MemberRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
//스프링과 묶어서 테스트 한다.
@SpringBootTest
// 스프링 부트를 띄운 상태에서의 테스트를 위함/ autowired 같은 기능을 위함
@Transactional
//커밋 될때 영속성 컨텍스트가 플러쉬되면서 쿼리가 한번에 나가는데
// 테스트는 플러쉬 안하고 롤백 시킴
public class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired
    MemberRepository memberRepository;

    @Test
//    @Rollback(value = false)
// 플러쉬까지 확인하고 싶을떄
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("kim");

        //when
        Long savedId = memberService.join(member);

        //then
        Assert.assertEquals(member,memberRepository.findOne(savedId));
    }

    @Test(expected = IllegalStateException.class)
    public void 중복가입_예외() throws Exception {
        //given
        Member memberA = new Member();
        memberA.setName("kim");
        memberService.join(memberA);
        Member memberB = new Member();
        memberB.setName("kim");

        //when
        memberService.join(memberB);

        //then
        Assertions.fail("예외가 발생해야 한다.");
    }

}