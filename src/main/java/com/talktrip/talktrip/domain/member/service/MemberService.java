package com.talktrip.talktrip.domain.member.service;

import com.talktrip.talktrip.domain.member.dto.request.MemberUpdateRequestDTO;
import com.talktrip.talktrip.domain.member.dto.response.MemberProfileView;
import com.talktrip.talktrip.domain.member.dto.response.MemberResponseDTO;
import com.talktrip.talktrip.domain.member.entity.Member;
import com.talktrip.talktrip.domain.member.repository.MemberRepository;
import com.talktrip.talktrip.global.s3.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final S3Uploader s3Uploader;

    @Transactional
    public void updateMemberProfile(Long memberId, MemberUpdateRequestDTO dto, MultipartFile profileImage) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        member.updateInfo(dto.getName(), dto.getGender(), dto.getBirthday(), dto.getPhoneNum());

        if (profileImage != null && !profileImage.isEmpty()) {
            String imageUrl = s3Uploader.upload(profileImage, "profile");
            member.updateProfileImage(imageUrl);
        }
    }

    /**
     * Redis {@code @Cacheable}는 JSON 역직렬화 시 LinkedHashMap만 되어 ClassCastException이 나기 쉬워 캐시를 쓰지 않는다.
     */
    public MemberResponseDTO getMemberInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        return MemberResponseDTO.from(member);
    }

    /**
     * 채팅방 목록·상품 카드용 공개 프로필 (닉네임·이름·썸네일). 민감 필드 제외.
     */
    public Optional<MemberProfileView> getMemberProfileView(Long memberId) {
        return memberRepository.findById(memberId).map(MemberProfileView::from);
    }
}
