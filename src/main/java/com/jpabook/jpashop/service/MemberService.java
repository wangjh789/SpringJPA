package com.jpabook.jpashop.service;

import com.jpabook.jpashop.domain.Member;
import com.jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
//final 아규먼트를 이용한 생성자 만들어줌
@RequiredArgsConstructor
// jpa 조회 성능 최적화 영속성 컨텍스트 플러시 안하고 더티체킹 안함
public class MemberService {
    private final MemberRepository memberRepository;

// 필드 주입이라 테스트 시 다른 repository를 넣기 힘듦
// 도중에 누가 바꿀 수 있음
//    @Autowired
//    public void setMemberRepository(MemberRepository memberRepository){
//        this.memberRepository = memberRepository;
//    }

//생성자를 이용하는게 가장 좋음
//    @Autowired
//    public MemberService(MemberRepository memberRepository){
//        this.memberRepository = memberRepository;
//    }

    /**
     * 회원가입
     */
    @Transactional
    public Long join(Member member){
        validateDuplicateMember(member);
        memberRepository.save(member);
        return member.getId();
    }

    /**
     * 전체 회원 조회
     */
    public List<Member> findMembers(){
        return memberRepository.findAll();
    }

    public Member findOne(Long memberId){
        return memberRepository.findOne(memberId);
    }

    // 동시에 db insert를 하게 되면 문제가 될수 있음, db의 유니크 제약조건을 거는게 확실하게 안전함
    private void validateDuplicateMember(Member member){
        //Exception
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if(!findMembers.isEmpty()){
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }

    }

    @Transactional
    //커맨드와 쿼리를 분리해라, 조회로직과 변경 로직을 분리
    public void update(Long id, String name) {
        Member one = memberRepository.findOne(id);
        one.setName(name);
    }
}
