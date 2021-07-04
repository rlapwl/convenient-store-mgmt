![image](https://user-images.githubusercontent.com/19424600/89319390-01769b00-d6bb-11ea-8c18-242889a63e44.png)

# 편의점 관리 시스템

# Table of contents

- [편의점 관리](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석설계)
  - [구현:](#구현-)
    - [DDD 의 적용](#ddd-의-적용)
    - [폴리글랏 퍼시스턴스](#폴리글랏-퍼시스턴스)
    - [폴리글랏 프로그래밍](#폴리글랏-프로그래밍)
    - [동기식 호출 과 Fallback 처리](#동기식-호출-과-Fallback-처리)
    - [비동기식 호출 과 Eventual Consistency](#비동기식-호출-과-Eventual-Consistency)
  - [운영](#운영)
    - [CI/CD 설정](#cicd설정)
    - [동기식 호출 / 서킷 브레이킹 / 장애격리](#동기식-호출-서킷-브레이킹-장애격리)
    - [오토스케일 아웃](#오토스케일-아웃)
    - [무정지 재배포](#무정지-재배포)
  - [신규 개발 조직의 추가](#신규-개발-조직의-추가)

# 서비스 시나리오

기능적 요구사항

1. 편의점주가 상품을 발주한다.
2. 발주 내용이 본사에 전달되어 상품을 배송한다.
3. 배송된 상품을 입고 처리한다.
4. 고객이 상품을 구매한다.
5. 구매 상품에 대한 상품 재고를 변경 처리한다.
6. 편의점주는 상품 발주를 취소할 수 있다.
7. 고객이 상품 구매를 취소하면 재고가 변경된다.
8. 편의점주는 발주 내용, 상품 재고 현황을 조회할 수 있다.
9. 편의점주는 발주, 배송, 상품 재고, 구매 현황들을 알림으로 받을 수 있다.

비기능적 요구사항

1. 트랜잭션
   1) 배송 취소되지 않으면 발주 취소를 할 수 없어야 한다.  Sync 호출 
2. 장애격리
   1) 재고변경 처리가 지연되더라도 고객의 구매는 처리할 수 있도록 유도한다.  Async (event-driven), Eventual Consistency
   2) 배송 시스템에 장애가 발생하면 발주취소는 잠시뒤에 처리될 수 있도록 한다. (Citcuit breaker, fallback)
3. 성능
   1. 편의점주는 발주, 상품 재고 현황에 대해 확인할 수 있어야 한다.  CQRS
   2. 편의점주는 발주, 배송, 상품 재고, 구매 현황들을 알림으로 받을 수 있어야 한다. Event driven


# 체크포인트

- 분석 설계


  - 이벤트스토밍: 
    - 스티커 색상별 객체의 의미를 제대로 이해하여 헥사고날 아키텍처와의 연계 설계에 적절히 반영하고 있는가?
    - 각 도메인 이벤트가 의미있는 수준으로 정의되었는가?
    - 어그리게잇: Command와 Event 들을 ACID 트랜잭션 단위의 Aggregate 로 제대로 묶었는가?
    - 기능적 요구사항과 비기능적 요구사항을 누락 없이 반영하였는가?    

  - 서브 도메인, 바운디드 컨텍스트 분리
    - 팀별 KPI 와 관심사, 상이한 배포주기 등에 따른  Sub-domain 이나 Bounded Context 를 적절히 분리하였고 그 분리 기준의 합리성이 충분히 설명되는가?
      - 적어도 3개 이상 서비스 분리
    - 폴리글랏 설계: 각 마이크로 서비스들의 구현 목표와 기능 특성에 따른 각자의 기술 Stack 과 저장소 구조를 다양하게 채택하여 설계하였는가?
    - 서비스 시나리오 중 ACID 트랜잭션이 크리티컬한 Use 케이스에 대하여 무리하게 서비스가 과다하게 조밀히 분리되지 않았는가?
  - 컨텍스트 매핑 / 이벤트 드리븐 아키텍처 
    - 업무 중요성과  도메인간 서열을 구분할 수 있는가? (Core, Supporting, General Domain)
    - Request-Response 방식과 이벤트 드리븐 방식을 구분하여 설계할 수 있는가?
    - 장애격리: 서포팅 서비스를 제거 하여도 기존 서비스에 영향이 없도록 설계하였는가?
    - 신규 서비스를 추가 하였을때 기존 서비스의 데이터베이스에 영향이 없도록 설계(열려있는 아키택처)할 수 있는가?
    - 이벤트와 폴리시를 연결하기 위한 Correlation-key 연결을 제대로 설계하였는가?

  - 헥사고날 아키텍처
    - 설계 결과에 따른 헥사고날 아키텍처 다이어그램을 제대로 그렸는가?

- 구현
  - [DDD] 분석단계에서의 스티커별 색상과 헥사고날 아키텍처에 따라 구현체가 매핑되게 개발되었는가?
    - Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 데이터 접근 어댑터를 개발하였는가
    - [헥사고날 아키텍처] REST Inbound adaptor 이외에 gRPC 등의 Inbound Adaptor 를 추가함에 있어서 도메인 모델의 손상을 주지 않고 새로운 프로토콜에 기존 구현체를 적응시킬 수 있는가?
    - 분석단계에서의 유비쿼터스 랭귀지 (업무현장에서 쓰는 용어) 를 사용하여 소스코드가 서술되었는가?
  - Request-Response 방식의 서비스 중심 아키텍처 구현
    - 마이크로 서비스간 Request-Response 호출에 있어 대상 서비스를 어떠한 방식으로 찾아서 호출 하였는가? (Service Discovery, REST, FeignClient)
    - 서킷브레이커를 통하여  장애를 격리시킬 수 있는가?
  - 이벤트 드리븐 아키텍처의 구현
    - 카프카를 이용하여 PubSub 으로 하나 이상의 서비스가 연동되었는가?
    - Correlation-key:  각 이벤트 건 (메시지)가 어떠한 폴리시를 처리할때 어떤 건에 연결된 처리건인지를 구별하기 위한 Correlation-key 연결을 제대로 구현 하였는가?
    - Message Consumer 마이크로서비스가 장애상황에서 수신받지 못했던 기존 이벤트들을 다시 수신받아 처리하는가?
    - Scaling-out: Message Consumer 마이크로서비스의 Replica 를 추가했을때 중복없이 이벤트를 수신할 수 있는가
    - CQRS: Materialized View 를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 내 서비스의 화면 구성과 잦은 조회가 가능한가?

  - 폴리글랏 플로그래밍
    - 각 마이크로 서비스들이 하나이상의 각자의 기술 Stack 으로 구성되었는가?
    - 각 마이크로 서비스들이 각자의 저장소 구조를 자율적으로 채택하고 각자의 저장소 유형 (RDB, NoSQL, File System 등)을 선택하여 구현하였는가?
  - API 게이트웨이
    - API GW를 통하여 마이크로 서비스들의 집입점을 통일할 수 있는가?
    - 게이트웨이와 인증서버(OAuth), JWT 토큰 인증을 통하여 마이크로서비스들을 보호할 수 있는가?
- 운영
  - SLA 준수
    - 셀프힐링: Liveness Probe 를 통하여 어떠한 서비스의 health 상태가 지속적으로 저하됨에 따라 어떠한 임계치에서 pod 가 재생되는 것을 증명할 수 있는가?
    - 서킷브레이커, 레이트리밋 등을 통한 장애격리와 성능효율을 높힐 수 있는가?
    - 오토스케일러 (HPA) 를 설정하여 확장적 운영이 가능한가?
    - 모니터링, 앨럿팅: 
  - 무정지 운영 CI/CD (10)
    - Readiness Probe 의 설정과 Rolling update을 통하여 신규 버전이 완전히 서비스를 받을 수 있는 상태일때 신규버전의 서비스로 전환됨을 siege 등으로 증명 
    - Contract Test :  자동화된 경계 테스트를 통하여 구현 오류나 API 계약위반를 미리 차단 가능한가?


# 분석/설계


## AS-IS 조직 (Horizontally-Aligned)

  ![image](https://user-images.githubusercontent.com/487999/79684144-2a893200-826a-11ea-9a01-79927d3a0107.png)

## TO-BE 조직 (Vertically-Aligned)

  ![image](https://user-images.githubusercontent.com/487999/79684159-3543c700-826a-11ea-8d5f-a3fc0c4cad87.png)


## Event Storming 결과

* MSAEz 로 모델링한 이벤트스토밍 결과:  http://msaez.io/#/storming/e9LJG8q1WXc9bg367c14BBHjiMW2/every/e042a9db3688e173aa4e7f266564151f/-MDslrtvSz9qJIM4UShJ

### 이벤트 도출

<img width="871" alt="스크린샷 2021-06-27 오후 5 19 58" src="https://user-images.githubusercontent.com/14067833/123537754-ff527a80-d76b-11eb-95c3-f87358bb97cf.png">

### 부적격 이벤트 탈락

<img width="870" alt="스크린샷 2021-06-27 오후 5 24 34" src="https://user-images.githubusercontent.com/14067833/123537841-93244680-d76c-11eb-81c1-b0615ff415f7.png">

```
- 과정중 도출된 잘못된 도메인 이벤트들을 걸러내는 작업을 수행함
  - 발주 내용이 본사에 전달됨, 상품 조회됨, 결제버튼 클릭됨, 주문 정보 조회됨, 구매 정보 조회됨 : UI 의 이벤트, 업무적인 의미의 이벤트가 아니라서 제외
```

### 액터, 커맨드 부착하여 읽기 좋게

<img width="1234" alt="스크린샷 2021-06-27 오후 6 08 48" src="https://user-images.githubusercontent.com/14067833/123539053-cbc71e80-d772-11eb-93cf-32e487758b28.png">

### 어그리게잇으로 묶기

<img width="1223" alt="스크린샷 2021-06-27 오후 6 12 57" src="https://user-images.githubusercontent.com/14067833/123539175-560f8280-d773-11eb-83d5-27bb6b480436.png">

### 바운디드 컨텍스트로 묶기

<img width="1220" alt="스크린샷 2021-06-29 오후 10 59 22" src="https://user-images.githubusercontent.com/14067833/123810814-aaf6f880-d92d-11eb-836f-dec9f7c6e222.png">

```
- 도메인 서열 분리 
    - Core Domain: order, delivery, product : 없어서는 안될 핵심 서비스이며, 연견 Up-time SLA 수준을 99.999% 목표, 배포주기는 order, delivery 의 경우 1주일 1회 미만, product 의 경우 1개월 1회 미만
    - Supporting Domain: marketing, customer : 경쟁력을 내기위한 서비스이며, SLA 수준은 연간 60% 이상 uptime 목표, 배포주기는 각 팀의 자율이나 표준 스프린트 주기가 1주일 이므로 1주일 1회 이상을 기준으로 함.
    - General Domain: payment : 결제서비스로 3rd Party 외부 서비스를 사용하는 것이 경쟁력이 높음 (핑크색으로 이후 전환할 예정)
```

### 폴리시 부착 (괄호는 수행주체, 폴리시 부착을 둘째단계에서 해놔도 상관 없음. 전체 연계가 초기에 드러남)

<img width="735" alt="스크린샷 2021-06-30 오전 12 38 36" src="https://user-images.githubusercontent.com/14067833/123827419-902b8080-d93b-11eb-9f4d-7cb49ee8d918.png">

### 폴리시의 이동과 컨텍스트 매핑 (점선은 Pub/Sub, 실선은 Req/Resp)

<img width="1171" alt="스크린샷 2021-06-30 오전 12 57 48" src="https://user-images.githubusercontent.com/14067833/123830413-38424900-d93e-11eb-82e2-a50f1e04f7b7.png">

### 완성된 1차 모형

<img width="1009" alt="스크린샷 2021-07-04 오전 9 31 02" src="https://user-images.githubusercontent.com/14067833/124369784-a5414000-dcaa-11eb-8584-e94bf91b64c3.png">

```
- View Model 추가
```

### 1차 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증



```
- 편의점주가 상품을 발주 한다. (OK)
- 본사가 발주된 상품을 배송 한다. (OK)
- 배송이 완료되면 상품을 입고처리 한다. (OK)
- 상품 발주시 편의점주는 view를 통해 발주 상세 내역, 상품 재고현황을 조회할 수 있다. (OK)
```

### 비기능 요구사항에 대한 검증



```
- 마이크로 서비스를 넘나드는 시나리오에 대한 트랜잭션 처리
   - 발주 취소시 배송 취소처리: ACID 트랜잭션 적용. 발주 취소시 배송 취소 처리에 대해서는 Request-Response 방식 처리
   - 배송 완료시 상품 입고처리: delivery 에서 product 마이크로서비스로 주문요청이 전달되는 과정에 있어서 product 마이크로 서비스가 별도의 배포주기를 가지기 때문에 Eventual Consistency 방식으로 트랜잭션 처리함.
   - 나머지 모든 inter-microservice 트랜잭션: 배달상태, 재고현황 등 모든 이벤트에 대해 카톡을 처리하는 등, 데이터 일관성의 시점이 크리티컬하지 않은 모든 경우가 대부분이라 판단, Eventual Consistency 를 기본으로 채택함.
```

### 최종 모델링



## 헥사고날 아키텍처 다이어그램 도출

![image](https://user-images.githubusercontent.com/19424600/89313080-020b3380-d6b3-11ea-9f29-c78495db4ebb.png)


    - Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
    - 호출관계에서 PubSub 과 Req/Resp 를 구분함
    - 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐


# 구현:

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 808n 이다)

```
- Local
cd order
mvn spring-boot:run

cd delivery
mvn spring-boot:run 

cd product
mvn spring-boot:run  

cd purchase
mvn spring-boot:run  

cd mypage
mvn spring-boot:run  

cd gateway
mvn spring-boot:run  

```

## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다: (purchase 마이크로 서비스). 

이때 가능한 현업에서 사용하는 언어 (유비쿼터스 랭귀지)를 그대로 사용하려고 노력했다. 

하지만, 일부 구현에 있어서 영문이 아닌 경우는 실행이 불가능한 경우가 있기 때문에 

편의점 업무인 발주처리, 입고처리 등을 한글로 설계 후 영문으로 구현하였다.

```
package PEJ;

...

@Entity
@Table(name="Purchase_table")
public class Purchase {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
...

    @PostPersist
    public void onPostPersist(){
        Purchased purchased = new Purchased();
        BeanUtils.copyProperties(this, purchased);
        purchased.publishAfterCommit();
    }

    @PreRemove
    public void onPreRemove(){
        Cancelled cancelled = new Cancelled();
        BeanUtils.copyProperties(this, cancelled);
        cancelled.setPurchaseStatus("취소됨");
        cancelled.publishAfterCommit();
    }

```

- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다

```
package PEJ;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface PurchaseRepository extends PagingAndSortingRepository<Purchase, Long>{

    Optional<Purchase> findAllByPurchaseIdEquals(String purchaseId);

}
```

- 적용 후 REST API 의 테스트
- 발주
  ![image](https://user-images.githubusercontent.com/19424600/89304692-b5225f80-d6a8-11ea-802b-926b77f2e737.png)
  ![image](https://user-images.githubusercontent.com/19424600/89304762-cb302000-d6a8-11ea-9ac9-6bee1b295652.png)
  ![image](https://user-images.githubusercontent.com/19424600/89304808-dc792c80-d6a8-11ea-8547-c8ec4ceae8bc.png)
  ![image](https://user-images.githubusercontent.com/19424600/89304886-f450b080-d6a8-11ea-8c10-5798d8eb0adb.png)
  ![image](https://user-images.githubusercontent.com/19424600/89304974-07638080-d6a9-11ea-9288-9cb3cceb8069.png)
  ![image](https://user-images.githubusercontent.com/19424600/89305031-1cd8aa80-d6a9-11ea-92c4-b00759e51e1a.png)


- 발주취소
  ![image](https://user-images.githubusercontent.com/19424600/89305107-2feb7a80-d6a9-11ea-8e12-6178605d3b04.png)
  ![image](https://user-images.githubusercontent.com/19424600/89305243-62957300-d6a9-11ea-97e1-a30d9b8f2f6c.png)
  ![image](https://user-images.githubusercontent.com/19424600/89305303-72ad5280-d6a9-11ea-93b0-79aa95b78ebb.png)
  ![image](https://user-images.githubusercontent.com/19424600/89305358-81940500-d6a9-11ea-9f60-52e290658565.png)


- 구매
  ![image](https://user-images.githubusercontent.com/19424600/89305411-91134e00-d6a9-11ea-844b-8034f6f11eab.png)
  ![image](https://user-images.githubusercontent.com/19424600/89305531-b6a05780-d6a9-11ea-8261-2ae42cf1bc04.png)
  ![image](https://user-images.githubusercontent.com/19424600/89305569-c5870a00-d6a9-11ea-90f5-2efc5298f994.png)

- 구매취소
  ![image](https://user-images.githubusercontent.com/19424600/89305637-dcc5f780-d6a9-11ea-9c70-627f41bfacf9.png)
  ![image](https://user-images.githubusercontent.com/19424600/89305669-e8b1b980-d6a9-11ea-8505-faebf06f5969.png)
  ![image](https://user-images.githubusercontent.com/19424600/89305716-f6ffd580-d6a9-11ea-8950-7a18a025ecd5.png)



## 동기식 호출 과 Fallback 처리

분석단계에서의 조건 중 하나로 발주취소(order)->배송취소(delivery) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 

- 배송서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현

```
# (order) DeliveryService.java

@FeignClient(name="Delivery", url="${api.url.delivery}")
public interface DeliveryService {

    @RequestMapping(method = RequestMethod.POST, path="/cancellations")
    void cancelDelivery(@RequestBody Delivery delivery);

}
```

- 발주취소 처리시(@PreUpdate) 배송취소를 요청하도록 처리

```
# Order.java (Entity)

    @PreUpdate
    public void onPreUpdate(){
        if("CANCELLED".equals(this.orderStatus)){
...
            PEJ.external.Delivery delivery = new PEJ.external.Delivery();
            delivery.setOrderId(orderCanceled.getOrderId());
            delivery.setDeliveryStatus("CANCELLED");

            OrderApplication.applicationContext.getBean(PEJ.external.DeliveryService.class)
                    .cancelDelivery(delivery);
        }
    }
```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 배송 시스템이 장애가 나면 주문도 못받는다는 것을 확인:



# 배송 (delivery) 서비스를 잠시 내려놓음

#발주취소처리
http PATCH http://localhost:8088/orders/1 orderStatus="CANCELLED" #Fail
  ![image](https://user-images.githubusercontent.com/19424600/89369385-e9cdff80-d718-11ea-9d12-cd0013969658.png)

#배송서비스 재기동
cd delivery
mvn spring-boot:run

#주문처리
http PATCH http://localhost:8088/orders/1 orderStatus="CANCELLED" #Success
  ![image](https://user-images.githubusercontent.com/19424600/89369593-8e504180-d719-11ea-90c9-bd87b089c6de.png)

- 또한 과도한 요청시에 서비스 장애가 도미노 처럼 벌어질 수 있다. (서킷브레이커, 폴백 처리는 운영단계에서 설명한다.)




## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트


배송이 이루어진 후에 상품시스템으로 이를 알려주는 행위는 동기식이 아니라 비 동기식으로 처리하여, 배송 처리를 위하여 상품처리가 블로킹 되지 않아도록 처리한다.

- 이를 위하여 배송처리 기록을 남긴 후에 곧바로 상품입고 되었다는 도메인 이벤트를 카프카로 송출한다(Publish)

```
package PEJ;
...

@Entity
@Table(name="Delivery_table")
public class Delivery {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long orderId;
    private String deliveryStatus;
    private String prdId;
    private Integer prdQty;
    private Integer prdPrice;
    private String prdNm;

    @PostPersist
    public void onPostPersist(){
        if(!"CANCELLED".equals(this.deliveryStatus)){
            Shipped shipped = new Shipped();
            BeanUtils.copyProperties(this, shipped);
            shipped.publishAfterCommit();
        }
    }
    
```

- 상품 서비스에서는 입고처리 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다:

```
package PEJ;

@Service
public class PolicyHandler{

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverShipped_ReceiveProduct(@Payload Shipped shipped){

        if(shipped.isMe()){
            System.out.println("##### listener ReceiveProduct : " + shipped.toJson());
        }

        if(shipped.isMe()){
            Product product = new Product();
            product.setPrdId(shipped.getPrdId());
            product.setPrdQty(shipped.getPrdQty());
            product.setPrdNm(shipped.getPrdNm());
            product.setPrdPrice(shipped.getPrdPrice());

            productRepository.save(product);
        }

    }

```

배송시스템은 상품시스템과 완전히 분리되어있으며, 이벤트 수신에 따라 처리되기 때문에, 상품시스템이 유지보수로 인해 잠시 내려간 상태라도 배송처리를 하는데 문제가 없다:

- 상품시스템을 중단후 발주처리시 배송이벤트 발생되지만 처리되지 않다가 
  상품시스템 구동 후 배송이벤트를 수신하여 상품입고 처리가 정상완료 됨을 확인
  ![image](https://user-images.githubusercontent.com/19424600/89307687-552db800-d6ac-11ea-9d6f-4a08aa73a783.png)
  ![image](https://user-images.githubusercontent.com/19424600/89307753-68d91e80-d6ac-11ea-93b8-3f5245083935.png)
  ![image](https://user-images.githubusercontent.com/19424600/89307806-79899480-d6ac-11ea-8d61-363cb625f0e0.png)



# 운영


## 동기식 호출 / 서킷 브레이킹 / 장애격리

* 서킷 브레이킹 프레임워크의 선택: Spring FeignClient + Hystrix 옵션을 사용하여 구현함

시나리오는 발주(order)-->배송(delivery) 시의 연결을 RESTful Request/Response 로 연동하여 구현이 되어있고, 결제 요청이 과도할 경우 CB 를 통하여 장애격리.

- Hystrix 를 설정:  요청처리 쓰레드에서 처리시간이 610 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록 (요청을 빠르게 실패처리, 차단) 설정

```
# application.ymlhystrix:  command:    # 전역설정    default:      execution.isolation.thread.timeoutInMilliseconds: 610
```

- 피호출 서비스(결제:pay) 의 임의 부하 처리 - 400 밀리에서 증감 220 밀리 정도 왔다갔다 하게

```
# (delivery) Delivery.java (Entity)    @PreUpdate    public void onPreUpdate(){        if("CANCELLED".equals(this.deliveryStatus)){            DeliveryCancelled deliveryCancelled = new DeliveryCancelled();            BeanUtils.copyProperties(this, deliveryCancelled);            deliveryCancelled.publishAfterCommit();            try {                Thread.currentThread().sleep((long) (400 + Math.random() * 220));            } catch (InterruptedException e) {                e.printStackTrace();            }        }    }
```

* 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인:

- 동시사용자 100명
- 60초 동안 실시
  ![image](https://user-images.githubusercontent.com/19424600/89369124-29481c00-d718-11ea-90c9-ce6c2569e35b.PNG)
  ![image](https://user-images.githubusercontent.com/19424600/89369123-2816ef00-d718-11ea-9464-7d4f121d9f83.PNG)


siege 실행 결과  : Availability 96% 확인




### 오토스케일 아웃

- 배송 서비스에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 CPU 사용량이 15프로를 넘어서면 replica 를 10개까지 늘려준다:
  ![image](https://user-images.githubusercontent.com/19424600/89306585-07648000-d6ab-11ea-8f37-2585134c153c.png)

- CB 에서 했던 방식대로 워크로드를 50초 동안 걸어준다.
  ![image](https://user-images.githubusercontent.com/19424600/89314832-1bad7a80-d6b5-11ea-90da-8cb242d6c710.png)


- 오토스케일 발생하지 않음(siege 실행 결과 오류 없이 수행됨 : Availability 100%)

서비스에 복잡한 비즈니스 로직이 포함된 것이 아니어서, 

CPU 부하를 주지 못한 것으로 추정된다.


## 무정지 재배포

* 먼저 무정지 재배포가 100% 되는 것인지 확인하기 위해서 Autoscaler 이나 CB 설정을 제거함

- 러닝중 이미지 적용
  ![image](https://user-images.githubusercontent.com/19424600/89368557-cace6e00-d716-11ea-88e2-7394df9e1fb2.png)

  ![image](https://user-images.githubusercontent.com/19424600/89367812-f3edff00-d714-11ea-9607-2d08c1c351f0.png)
  배포기간중 Availability 가 평소 100%에서 99% 대로 떨어지는 것을 확인. 원인은 쿠버네티스가 성급하게 새로 올려진 서비스를 READY 상태로 인식하여 서비스 유입을 진행한 것이기 때문. 이를 막기위해 Readiness Probe 를 설정함:

- 동일한 시나리오로 재배포 한 후 Availability 확인:
  ![image](https://user-images.githubusercontent.com/19424600/89368185-d705fb80-d715-11ea-84e5-2079f6851a1d.png)

배포기간 동안 Availability 가 변화없기 때문에 무정지 재배포가 성공한 것으로 확인됨.
