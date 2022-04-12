package com.jpabook.jpashop.api;

import com.jpabook.jpashop.domain.Member;
import com.jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

@RestController  // controller, response body
@RequiredArgsConstructor
public class MemberApiController {
    private final MemberService memberService;

    @GetMapping("/api/v1/members")
    // 엔티티를 외부에 직접 노출하게 됨, @JsonIgnore로 숨길 수 있음 -> 다른 api 에선 필요할 수 있음,
    // 엔티티에 프레젠테이션을 위한 로직이 들어감 -> 수정이 어려움
    // 엔티티 필드가 바뀌면 api스펙에 영향
    // 응답이 array로 감싸져서 올 경우 다른 요구사항을 위한 확장을 하기 어렵다.
    public List<Member> membersV1(){
        return memberService.findMembers();
    }

    @GetMapping("/api/v2/members")
    //엔티티가 변환해도 api 스펙이 변하지 않음
    // 한번 감싸서 반환해서 유연성이 증가함
    public Result memberV2(){
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream().
                map(i -> new MemberDto(i.getName())).collect(Collectors.toList());
        return new Result(collect.size(),collect);
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private int count;
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto{
        private String name;
    }



    @PostMapping("/api/v1/members")
    //requestBody 는 json을 member에 맞춰서 넣어줌
    //valid는 dto 제약조건 검사
    //엔티티에 프레젠테이션 검증 로직이 들어가버림 -> dto가 필요한 이유
    //엔티티의 필드명이 바뀔 경우, api의 스펙이 변경됨 -> dto가 필요한 이유
    // api 스펙을 위한 별도의 dto가 필요 -> V2
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member){
        Long joinedId = memberService.join(member);
        return new CreateMemberResponse(joinedId);
    }

    @PostMapping("/api/v2/members")
    //dto 에 제약조건을 걸 수 있어 엔티티가 깔끔하게 유지될 수 있음
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request){
        Member member = new Member();
        member.setName(request.getName());

        Long join = memberService.join(member);
        return new CreateMemberResponse(join);
    }


    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id,
                                  @RequestBody @Valid UpdateMemberRequest request){
        memberService.update(id,request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId() ,findMember.getName());
    }

    @Data
    static class CreateMemberRequest{
        @NotEmpty
        private String name;
    }

    @Data @AllArgsConstructor
    static class CreateMemberResponse{
        private Long id;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }

}
