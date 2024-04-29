package com.example.joindemo;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public void dummyDataInit() {
        Member member1 = Member.builder()
                .name("Lee")
                .build();
        Member member2 = Member.builder()
                .name("Park")
                .build();
        Member member3 = Member.builder()
                .name("Cho")
                .build();

        Team team = Team.builder()
                .name("울산 현대 FC")
                .build();

        team.add(member1);
        team.add(member2);
        team.add(member3);

        Team team2 = Team.builder()
                .name("토트넘")
                .build();

        Member member4 = Member.builder()
                .name("Son")
                .build();
        Member member5 = Member.builder()
                .name("Richarlison")
                .build();

        team2.add(member4);
        team2.add(member5);

        teamRepository.save(team);
        teamRepository.save(team2);
    }

    public void clear() {
        teamRepository.deleteAll();
    }

    public List<Team> findAllTeam() {
        return teamRepository.findAll();
    }

    public List<Team> findAllTeamUsingFetchJoin() {
        return teamRepository.findAllFetchJoin();
    }

    public List<Team> findAllTeamUsingEntityGraph() {
        return teamRepository.findAllEntityGraph();
    }

    public List<Team> findAllTeamUsingJoin() {
        return teamRepository.findAllJoin();
    }
}
