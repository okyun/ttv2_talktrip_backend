package com.talktrip.talktrip.domain.member.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.talktrip.talktrip.domain.member.entity.Member;
import com.talktrip.talktrip.domain.member.enums.Gender;
import com.talktrip.talktrip.domain.member.enums.MemberRole;
import com.talktrip.talktrip.domain.member.enums.MemberState;
import lombok.Getter;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Getter
public class MemberResponseDTO {

    private final Long id;
    private final String accountEmail;
    private final String name;
    private final String nickname;
    private final MemberRole memberRole;
    private final Gender gender;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDate birthday;
    private final String phoneNum;
    private final String profileImage;
    private final MemberState memberState;

    /** Redis 역직렬화용 — {@link com.talktrip.talktrip.global.config.CacheConfig} */
    @JsonCreator
    public MemberResponseDTO(
            @JsonProperty("id") Long id,
            @JsonProperty("accountEmail") String accountEmail,
            @JsonProperty("name") String name,
            @JsonProperty("nickname") String nickname,
            @JsonProperty("memberRole") MemberRole memberRole,
            @JsonProperty("gender") Gender gender,
            @JsonProperty("birthday") LocalDate birthday,
            @JsonProperty("phoneNum") String phoneNum,
            @JsonProperty("profileImage") String profileImage,
            @JsonProperty("memberState") MemberState memberState
    ) {
        this.id = id;
        this.accountEmail = accountEmail;
        this.name = name;
        this.nickname = nickname;
        this.memberRole = memberRole;
        this.gender = gender;
        this.birthday = birthday;
        this.phoneNum = phoneNum;
        this.profileImage = profileImage;
        this.memberState = memberState;
    }

    public MemberResponseDTO(Member member) {
        this(
                member.getId(),
                member.getAccountEmail(),
                member.getName(),
                member.getNickname(),
                member.getMemberRole(),
                member.getGender(),
                member.getBirthday(),
                member.getPhoneNum(),
                member.getProfileImage(),
                member.getMemberState()
        );
    }

    public static MemberResponseDTO from(Member member) {
        return new MemberResponseDTO(member);
    }

    // JWT 토큰 생성 시 claim에 넣을 정보 정리
    public Map<String, Object> getClaims() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);
        claims.put("email", accountEmail);
        claims.put("name", name);
        claims.put("nickname", nickname);
        claims.put("role", memberRole.name());
        claims.put("gender", gender.name());
        claims.put("birthday", birthday.toString());
        claims.put("phoneNum", phoneNum);
        claims.put("profileImage", profileImage);
        claims.put("state", memberState.name());
        return claims;
    }
}