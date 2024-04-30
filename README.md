## N + 1 문제 해결을 위한 JPA 가 제공하는 여러 JOIN 방식의 동작 원리 분석

JPA Interface (CrudRepository) 가 제공하는 메서드를 사용할 때 N + 1 문제를 살펴보고 해결해보자

그리고 객체 연관관계에 따른 N + 1 문제도 만나보고 해결해본다.

### 테스트 환경
- Java 17
- Spring Boot 3.2
- H2 DB

### 도메인 구조

- Team 1 - Member N (양방향 매핑)
- Member 1 - MemberOption 1 (양방향 매핑)

Member, MemberOption 1:1 관계를 JPA 양방향 매핑한 이유
- Member는 사용자 정보, MemberOption은 사용자의 추가적인 정보를 관리하는 테이블
- Member가 주 테이블이고 MemberOption이 대상 테이블임
- MemberOption 테이블이 외래키를 관리하는것을 선호 (전통적인 테이블 설계 지향)
  - JPA 에서는 OneToOne 관계에선 주 테이블 (사용자가 주)이 외래키를 관리해야함
  - 1:1 관계 매핑을 위해 객체지향 방식에선 Member가 MemberOption 필드를 갖고있는게 맞음
  - 하지만 MemberOption 객체가 Nullable 함
- 따라서 OneToOne 방식을 대상 테이블에서 관리하기 위해선 양방향 매핑을 해야한다
  - 대상 테이블이 연관관계의 주인이 되므로 Nullable 하지 않음
  - 1:1 -> 1:N 으로 확장 시 테이블 구조를 유지할 수 있음 (애플리케이션 코드만 변경)

### FetchType 비교

- FetchType.Lazy
  - 조회할려는 객체를 Proxy 객체로 감싸 지연 로딩을 제공
  - 장점 : 빠른 클래스 로딩, 적은 메모리 소비
  - 단점 : LazyInitlizaed 로 인한 추가적인 조회 쿼리 (N + 1) 로 성능영향, LazyInitializationException 발생 가능 (Session 내부가 아닐 시) 
  - OneToOne 에서 사용할려면 Non-Null or 단방향관계 (Nullable -> 프록시 객체 X)
- FetchType.Eager
  - 데이터 조회 시 JOIN 을 통해 데이터를 한꺼번에 로딩해 제공
  - 장점 : 지연로딩과 관련된 성능 영향이 없음
  - 단점 : 클래스 로드 시간이 길어짐(추가적인 객체 오버헤드 발생), OOM Heap 발생으로 인한 성능 영향 (많은 데이터 조회 시)

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
![image](https://github.com/Hoon9901/join-fetchjoin-compare-for-n-1/assets/5029567/97d411f6-f37f-4433-84db-a1d5bf845318)

- 연관관계 엔티티 조회 시 `LazyInitializationException` 예외 발생
  - 이를 통해 JPA는 SELECT 절에 포함된 속성(엔티티 데이터)을 통해 영속성 처리, JOIN 으로 지정한 연관 엔티티는 영속성 관여를 하지 않음을 알 수 있다.
  - 즉 일반 JOIN 을 이용해 연관관계 엔티티를 조회하지 말고 다른 방식을 사용해야한다.

### 결론
- N + 1 문제 방지를 위해선 Fetch Join, Entity Graph 방식을 사용한다
- JPA는 만능이 아니다 일반 JOIN으로 연관 조회로는 N + 1 문제 해결은 커녕 데이터 조회가 발생하지 않는다.
- Fetch Join, Entity Graph 뿐만 아니라 BatchSize 여러 기능을 제공하니 이를 적절히 사용해 N + 1를 해결하고 성능 최적화를 진행하자.
