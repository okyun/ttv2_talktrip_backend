package com.talktrip.talktrip.domain.like.repository;

import com.talktrip.talktrip.domain.like.entity.Like;
import com.talktrip.talktrip.domain.member.entity.Member;
import com.talktrip.talktrip.domain.member.enums.MemberRole;
import com.talktrip.talktrip.domain.member.enums.MemberState;
import com.talktrip.talktrip.domain.product.entity.Product;
import com.talktrip.talktrip.global.config.QuerydslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.talktrip.talktrip.global.TestConst.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({QuerydslConfig.class, LikeRepositoryTest.AuditingTestConfig.class})
class LikeRepositoryTest {

    @Autowired LikeRepository likeRepository;
    @Autowired EntityManager em;

    @TestConfiguration @EnableJpaAuditing
    static class AuditingTestConfig {}

    private Member user() {
        Member m = Member.builder()
                .accountEmail(USER_EMAIL)
                .memberRole(MemberRole.U)
                .memberState(MemberState.A)
                .build();
        em.persist(m);
        return m;
    }

    private Member seller() {
        Member m = Member.builder()
                .accountEmail(SELLER_EMAIL)
                .memberRole(MemberRole.A)
                .memberState(MemberState.A)
                .build();
        em.persist(m);
        return m;
    }

    private Product product(Member s, String name) {
        Product p = Product.builder()
                .member(s)
                .productName(name)
                .description(DESC)
                .deleted(false)
                .build();
        em.persist(p);
        return p;
    }

    private void like(Member member, Product product) {
        Like l = Like.builder().member(member).product(product).build();
        em.persist(l);
    }

    @AfterEach
    void clearPersistence() {
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("findAllByMember_IdWithProduct: 해당 회원 좋아요만, product 로드됨")
    void findAllByMember_IdWithProduct_returnsJoinedProduct() {
        Member u = user();
        Member s = seller();
        Product p1 = product(s, PRODUCT_NAME_1);
        Product p2 = product(s, PRODUCT_NAME_2);
        like(u, p1);
        like(u, p2);

        List<Like> list = likeRepository.findAllByMember_IdWithProduct(u.getId());

        assertThat(list).hasSize(2);
        assertThat(list).extracting(l -> l.getProduct().getId()).containsExactlyInAnyOrder(p1.getId(), p2.getId());
    }

    @Test
    @DisplayName("findAllByMember_IdWithProduct: 다른 회원 데이터 제외")
    void findAllByMember_IdWithProduct_isolatedByMember() {
        Member u1 = user();
        Member s = seller();
        Member u2 = Member.builder()
                .accountEmail(USER2_EMAIL)
                .memberRole(MemberRole.U)
                .memberState(MemberState.A)
                .build();
        em.persist(u2);

        Product p1 = product(s, PRODUCT_NAME_1);
        Product p2 = product(s, PRODUCT_NAME_2);
        like(u1, p1);
        like(u2, p2);

        List<Like> list = likeRepository.findAllByMember_IdWithProduct(u1.getId());

        assertThat(list).hasSize(1);
        assertThat(list.getFirst().getProduct().getId()).isEqualTo(p1.getId());
    }

    @Test
    @DisplayName("findAllByMember_IdWithProduct: 좋아요 없으면 빈 리스트")
    void findAllByMember_IdWithProduct_empty() {
        Member u = user();

        List<Like> list = likeRepository.findAllByMember_IdWithProduct(u.getId());

        assertThat(list).isEmpty();
    }
}
