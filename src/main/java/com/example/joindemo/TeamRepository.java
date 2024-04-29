package com.example.joindemo;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {

    @Query("SELECT t FROM Team t JOIN FETCH t.members")
    List<Team> findAllFetchJoin();

    @EntityGraph(attributePaths = {"members"})
    @Query("SELECT t FROM Team t")
    List<Team> findAllEntityGraph();

    @Query("SELECT t FROM Team t JOIN t.members")
    List<Team> findAllJoin();
}
