## N + 1 문제 해결을 위한 JPA 가 제공하는 여러 JOIN 방식의 동작 원리 분석

### 테스트 환경
- Java 17
- Spring Boot 3.2
- H2 DB

### Fetch Join

```java
    @Query("SELECT t FROM Team t JOIN FETCH t.members")
    List<Team> findAllFetchJoin();

    // 발생하는 쿼리
    select t1_0.id,m1_0.team_id,m1_0.id,m1_0.name,t1_0.name
    from team t1_0 join member m1_0
          on t1_0.id=m1_0.team_id
```
- JPQL을 이용해 Fetch Join 사용 (Jpa 쿼리메소드 X)
- INNER JOIN 발생
- 하나의 연관관계만 매핑해서 조회

### Entity Graph

```java
    @EntityGraph(attributePaths = {"members"})
    @Query("SELECT t FROM Team t")
    List<Team> findAllEntityGraph();

    // 발생하는 쿼리
    select t1_0.id,m1_0.team_id,m1_0.id,m1_0.name,t1_0.name
    from team t1_0 left join member m1_0
          on t1_0.id=m1_0.team_id
```
- JPQL + @EntityGraph 사용
- LEFT JOIN 발생
- attributePaths 옵션을 통해 복수의 연관관계를 지정할 수 있다

### 일반 Join

```java
    @Query("SELECT t FROM Team t JOIN t.members")
    List<Team> findAllJoin();

    // 발생하는 쿼리
    select t1_0.id,t1_0.name  // Team 필드만 조회
    from team t1_0 join member m1_0
      on t1_0.id=m1_0.team_id
```
- 연관관계 엔티티 조회 시 `LazyInitializationException` 예외 발생
  - 이를 통해 JPA는 SELECT 절에 포함된 속성(엔티티 데이터)을 통해 영속성 처리, JOIN 으로 지정한 연관 엔티티는 영속성 관여를 하지 않음을 알 수 있다.
  - 즉 일반 JOIN 을 이용해 연관관계 엔티티를 조회하지 말고 다른 방식을 사용해야한다.

### 결론
- N + 1 문제 방지를 위해선 Fetch Join, Entity Graph 방식을 사용한다
- JPA는 만능이 아니다 일반 JOIN으로 연관 조회로는 N + 1 문제 해결은 커녕 데이터 조회가 발생하지 않는다.
- Fetch Join, Entity Graph 뿐만 아니라 BatchSize 여러 기능을 제공하니 최적화를 통해 N + 1 문제를 해결하고 성능 최적화를 진행하자.
