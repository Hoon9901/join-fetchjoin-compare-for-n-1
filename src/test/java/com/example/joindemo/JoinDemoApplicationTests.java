package com.example.joindemo;

import jakarta.persistence.EntityManager;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class JoinDemoApplicationTests {

    @Autowired
    private EntityManager em;

    @Autowired
    private TeamService teamService;

    @BeforeEach
    public void init() {
        teamService.dummyDataInit();
        em.clear();
    }

    @AfterEach
    public void clear() {
        teamService.clear();
    }

    private static void output(List<Team> teams) {
        System.out.println("========================");
        for (Team team : teams) {
            System.out.println(team);
            List<Member> members = team.getMembers();
            for (Member member : members) {
                System.out.println(member);
            }
        }
        System.out.println("=========== Test Done =============\n");
    }

    @DisplayName("연관관계 조회시 추가적인 쿼리가 발생한다")
    @Test
    @Transactional // LazyInitalizationException 방지
    public void test1() {
        List<Team> teams = teamService.findAllTeam();
        output(teams);
    }

    @DisplayName("FetchJoin 을 이용해 N + 1 방지")
    @Test
    public void test2() {
        List<Team> teams = teamService.findAllTeamUsingFetchJoin(); // OUTER JOIN
        output(teams);
    }

    @DisplayName("EntityGraph 을 이용해 N + 1 방지")
    @Test
    public void test3() {
        List<Team> teams = teamService.findAllTeamUsingEntityGraph(); // JEFT JOIN
        output(teams);
    }

    @DisplayName("일반 JOIN 을 이용해 연관관계 조회 시")
    @Test
    public void test4() {
        List<Team> teams = teamService.findAllTeamUsingJoin();
        assertThrows(LazyInitializationException.class, () -> output(teams));
        System.out.println("=========== Test Done =============\n");
        // 일반 조인 시 Team 엔티티와 Member 엔티티를 함께 조회하지만 실제 JPA 컨텍스트에는 Team만 영속화 되어있다.
        // 즉 JPQL (JPA) 로 SELECT 절에 포함된 엔티티만 영속화되고 나머지 데이터에 영속성은 관여하지 않음을 알 수 있다.
        // 이는 일반 조인 시 연관관계 엔티티는 별도로 조회가 필요함을 알 수 있다.
    }


}
