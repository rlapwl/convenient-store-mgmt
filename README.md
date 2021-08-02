![image](https://user-images.githubusercontent.com/19424600/89319390-01769b00-d6bb-11ea-8c18-242889a63e44.png)

# 편의점 관리 시스템

- 체크포인트 : https://workflowy.com/s/assessment-check-po/T5YrzcMewfo4J6LW

# Table of contents

- [편의점 관리](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [Event-Storming](#Event-Storming)
  - [구현:](#구현:)
    - [CQRS](#cqrs)
    - [API 게이트웨이](#api-게이트웨이)
    - [Correlation](#correlation)
    - [DDD 의 적용](#ddd-의-적용)
    - [폴리글랏 퍼시스턴스](#폴리글랏-퍼시스턴스)
    - [동기식 호출과 Fallback 처리](#동기식-호출과-Fallback-처리)
    - [비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트](#비동기식-호출-/-시간적-디커플링-/-장애격리-/-최종-(eventual)-일관성-테스트)
  - [운영](#운영)
    - [CI/CD 설정](#ci/cd-설정)
    - [동기식 호출 / 서킷 브레이킹 / 장애격리](#동기식-호출-/-서킷-브레이킹-/-장애격리)
    - [오토스케일 아웃](#오토스케일-아웃)
    - [무정지 재배포(Readiness Probe)](#무정지-재배포(Readiness-Probe))
    - [Self-healing(Liveness Probe)](#Self-healing(Liveness-Probe))
    - [Config Map/Persistence Volume](#Config-Map/Persistence-Volume)

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


## Event Storming

### 1차 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증

<img width="1026" alt="41E979D4-F6E7-4DBD-956F-2C12BE2EB832" src="https://user-images.githubusercontent.com/14067833/124854479-59f89b80-dfe2-11eb-8f4b-c3be4f1aa165.png">

```
- 편의점주가 상품을 발주 한다. (OK)
- 본사가 발주된 상품을 배송 한다. (OK)
- 배송이 완료되면 상품을 입고처리 한다. (OK)
- 상품 발주시 편의점주는 view를 통해 발주 상세 내역, 상품 재고현황을 조회할 수 있다. (OK)
```

### 최종 모델링

<img width="1023" alt="스크린샷 2021-07-08 오후 4 17 17" src="https://user-images.githubusercontent.com/14067833/124879242-fd0fdc00-e007-11eb-8e98-ee63b79a7ed2.png">

## 헥사고날 아키텍처 다이어그램 도출

<img width="1009" alt="스크린샷 2021-07-09 오전 2 55 41" src="https://user-images.githubusercontent.com/14067833/124969014-2c066c00-e061-11eb-90d1-a9f31497843d.png">


    - Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
    - 호출관계에서 PubSub 과 Req/Resp 를 구분함
    - 서브 도메인과 바운디드 컨텍스트의 분리: 각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐


# 구현:

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 808n 이다)

```shell
cd gateway
mvn spring-boot:run

cd ../order
mvn spring-boot:run

cd ../delivery
mvn spring-boot:run 

cd ../product
mvn spring-boot:run  

cd ../payment
mvn spring-boot:run  

cd ../alarm
mvn spring-boot:run
```

## CQRS

발주(Order) 상세 내역, 상품(Product) 재고 현황 등 Status에 대하여 편의점주가 조회 할 수 있도록 CQRS 로 구현하였다.

- OrderStatus

  ```java
  @Entity
  @Table(name="OrderStatus_table")
  public class OrderStatus {
  
      @Id
      @GeneratedValue(strategy=GenerationType.IDENTITY)
      private Long id;
      private Long productId;
      private String status;
      private int quantity;
  }
  ```

- ProductPage

  ```java
  @Entity
  @Table(name="ProductPage_table")
  public class ProductPage {
  
      @Id
      @GeneratedValue(strategy=GenerationType.IDENTITY)
      private Long id;
      private int quantity;
      private int price;
      private String status;
  }
  ```

- ProductPageViewHandler 를 통해 구현

  - "StockModified" 이벤트 발생 시, Pub/Sub 기반으로 별도 ProductPage_table 테이블에 저장

  ```java
  @Service
  public class ProductPageViewHandler {
  
      @StreamListener(KafkaProcessor.INPUT)
      public void whenStockModified_then_UPDATE(@Payload StockModified stockModified) {
        ...
          if (productPageOptional.isPresent()) {
          	ProductPage productPage = productPageOptional.get();
                      
            // view 객체에 이벤트의 eventDirectValue 를 set 함
            productPage.setQuantity(stockModified.getQuantity());
            productPage.setStatus(stockModified.getStatus());
  
            // view 레파지토리에 save
            productPageRepository.save(productPage);
  
          } else {
            // view 레파지토리에 save
            productPageRepository.save(new ProductPage(stockModified.getQuantity(), stockModified.getPrice(), stockModified.getStatus()));
          }
        
        ...
      }
  }
  ```

- OrderStatusViewHandler 를 통해 구현(ProductPageViewHandler 구현 형태 비슷함)

  - "DeliveryStarted", "DeliveryCanceled" 이벤트 발생 시, Pub/Sub 기반으로 별도 OrderStatus_table 테이블에 저장

- View 페이지 조회 결과

  <img width="1029" alt="스크린샷 2021-07-08 오후 3 28 07" src="https://user-images.githubusercontent.com/14067833/124873201-1eb99500-e001-11eb-986e-6484f8c81235.png">

## API 게이트웨이

gateway App을 추가 후 application.yaml에 각 마이크로 서비스의 routes를 추가, 서버의 포트를 8080 으로 설정함

```yaml
spring:
  profiles: docker
  cloud:
    gateway:
      routes:
        - id: order
          uri: http://order:8080
          predicates:
            - Path=/orders/**, /orderStatuses/**
        - id: delivery
          uri: http://delivery:8080
          predicates:
            - Path=/deliveries/** 
        - id: product
          uri: http://product:8080
          predicates:
            - Path=/products/**, /productPages/**
        - id: payment
          uri: http://payment:8080
          predicates:
            - Path=/payments/** 
        - id: alarm
          uri: http://alarm:8080
          predicates:
            - Path=/messages/** 
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true

server:
  port: 8080
```

```shell
 kubectl apply -f deployment.yml
 kubectl apply -f service.yaml
```

- 배포후

  <img width="1093" alt="스크린샷 2021-07-08 오후 2 47 46" src="https://user-images.githubusercontent.com/14067833/124868969-853bb480-dffb-11eb-8e6a-89a1f5fbe58b.png">

# Correlation

PolicyHandler에서 처리 시 어떤 건에 대한 처리인지를 구별하기 위한 Correlation-key 구현을 
이벤트 클래스 안의 변수로 전달받아 처리하도록 구현하였다. 

- 상품(Order) 발주 시 배송(Delivery), 상품(Product) 등의 상태가 변경되는 걸 확인할 수 있다.
- 상품(Order) 발주 취소를 수행하면 다시 연관된 배송(Delivery), 상품(Product) 등의 상태가 변경되는 걸 확인할 수 있다.

상품 등록

<img width="1158" alt="스크린샷 2021-07-08 오후 2 06 43" src="https://user-images.githubusercontent.com/14067833/124869794-acdf4c80-dffc-11eb-95fd-218e7e5f1d43.png">

상품 발주 (Product 수량 변경 10 -> 13)

<img width="1256" alt="스크린샷 2021-07-08 오후 3 01 35" src="https://user-images.githubusercontent.com/14067833/124872829-b5d21d00-e000-11eb-953e-66af8559c91e.png">

<img width="1120" alt="스크린샷 2021-07-08 오후 3 24 31" src="https://user-images.githubusercontent.com/14067833/124872897-c6829300-e000-11eb-897c-fc19e9130d46.png">

<img width="1009" alt="스크린샷 2021-07-08 오후 3 26 34" src="https://user-images.githubusercontent.com/14067833/124873041-eca83300-e000-11eb-9e69-43c319c63376.png">

상품 발주 취소

- 발주 취소 실행하면 관련 Order, Delivery 데이터는 Delete가 되어 없어지고 관련 Product 수량은 13 -> 10으로 줄어든 것을 볼 수 있으며 OrderStatus, ProductPage에는 이력이 남게 된다. 

<img width="1037" alt="스크린샷 2021-07-08 오후 3 41 48" src="https://user-images.githubusercontent.com/14067833/124874941-4873bb80-e003-11eb-8637-a248c534f150.png">

<img width="1094" alt="스크린샷 2021-07-08 오후 3 42 22" src="https://user-images.githubusercontent.com/14067833/124874997-5c1f2200-e003-11eb-9b2d-13696db80cdc.png">

<img width="1015" alt="스크린샷 2021-07-08 오후 3 42 34" src="https://user-images.githubusercontent.com/14067833/124875047-693c1100-e003-11eb-9c9f-158f87147b94.png">

<img width="1038" alt="스크린샷 2021-07-08 오후 3 43 21" src="https://user-images.githubusercontent.com/14067833/124875081-735e0f80-e003-11eb-9fe0-65b3b8906051.png">

## DDD 의 적용

각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다: (예시는 Payment 마이크로 서비스). 이때 가능한 현업에서 사용하는 언어 (유비쿼터스 랭귀지)를 그대로 사용하려고 노력했다. 하지만, 일부 구현에 있어서 영문이 아닌 경우는 실행이 불가능한 경우가 있기 때문에 계속 사용할 방법은 아닌것 같다. (Maven pom.xml, Kafka의 topic id, FeignClient 의 서비스 id 등은 한글로 식별자를 사용하는 경우 오류가 발생하는 것을 확인하였다)

```java
@Entity
@Table(name="Payment_table")
public class Payment {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    private int quantity;
    
    private int price;

    @PostPersist
    public void onPostPersist() {
        PayApproved payApproved = new PayApproved(id, productId, quantity, price);
        payApproved.publishAfterCommit();

    }

    @PreRemove
    public void onPreRemove() {
        PayCanceled payCanceled = new PayCanceled(id, productId, quantity, price);
        payCanceled.publishAfterCommit();
    }

  // getter
...
		
  // setter
...
}
```

- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다

```java
package convenientstore;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="payments", path="payments")
public interface PaymentRepository extends PagingAndSortingRepository<Payment, Long>{
}
```

- 적용 후 REST API 의 테스트

```shell
# 상품 구매
http POST http://localhost:8088/payments productId=1 quantity=2 price=2000 

# 상품 재고 확인
http GET http://localhost:8088/products/1

# 상품 구매 내역 확인
http GET http://localhost:8088/payments/1
```

## 폴리글랏 퍼시스턴스

Alarm 서비스 특성상 규모가 크지 않고 데이터를 저장하고 빨리 가져올 수 있는 데이터 접근이 빠른 HSQL DB를 사용하기로 하였다. h2와 비슷하여 별다른 작업없이 데이터베이스 제품의 설정(pom.xml) 만으로 HSQL DB에 부착시켰다

```xml
<dependency>
  <groupId>org.hsqldb</groupId>
  <artifactId>hsqldb</artifactId>
  <version>2.5.2</version>
  <scope>runtime</scope>
</dependency>
```

- DB 적용 후

<img width="1018" alt="스크린샷 2021-07-09 오전 2 53 28" src="https://user-images.githubusercontent.com/14067833/124968857-fa8da080-e060-11eb-9be4-df166e99b3c1.png">

## 동기식 호출과 Fallback 처리

분석단계에서의 조건 중 하나로 발주취소(order) -> 배송취소(delivery) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 

- 배송서비스를 호출하기 위하여 Stub과 FeignClient를 이용하여 Service 대행 인터페이스 Proxy를 구현

  ```java
  @FeignClient(name = "delivery", url = "${api.url.delivery}")
  @RequestMapping("/deliveries")
  public interface DeliveryService {
  
      @DeleteMapping(path = "/{deliveryId}")
      public void cancelDelivery(@PathVariable Long deliveryId);
  
  }
  ```

- 발주취소 처리시(@PreRemove) 배송취소를 요청하도록 처리

  ```java
  @Entity
  @Table(name = "Order_table")
  public class Order {
  
      @PreRemove
      public void onPreRemove() {
          Delivery delivery = OrderApplication.applicationContext.getBean(convenientstore.external.DeliveryService.class)
                      .findDelivery(getId());
  
          OrderApplication.applicationContext.getBean(convenientstore.external.DeliveryService.class)
                  .cancelDelivery(delivery.getId());
  
          OrderCanceled orderCanceled = new OrderCanceled(id, productId, quantity, "cancel");
          orderCanceled.publishAfterCommit();
      }
  }
  ```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 배송 시스템이 장애가 나면 주문도 못받는다는 것을 확인:

  ```shell
  # 배송(Delivery)서비스를 잠시 내려놓음
  
  # 발주 취소
  http DELETE http://localhost:8088/orders/3	# Fail
  
  # 배송서비스 재기동
  
  # 발주 취소
  http DELETE http://localhost:8088/orders/3  #Success
  ```

- 또한 과도한 요청시에 서비스 장애가 도미노 처럼 벌어질 수 있다. (서킷브레이커, 폴백 처리는 운영단계에서 설명한다.)


## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트


배송이 이루어진 후에 상품시스템으로 이를 알려주는 행위는 동기식이 아니라 비동기식으로 처리하여, 배송 처리를 위하여 상품처리가 블로킹 되지 않아도록 처리한다.

- 이를 위하여 배송처리 기록을 남긴 후에 곧바로 상품입고 되었다는 도메인 이벤트를 카프카로 송출한다(Publish)

```java
@Entity
@Table(name = "Delivery_table")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long productId;

    private int quantity;

    private String status; // delivery: 배송, cancel: 배송 취소

    @PostPersist
    public void onPostPersist() {
        if ("delivery".equals(this.status)) {
            DeliveryStarted deliveryStarted = new DeliveryStarted(id, orderId, productId, quantity);
            deliveryStarted.publishAfterCommit();
        }
    }
  ...
}
```

- 상품 서비스에서는 입고처리 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다:

```java
@Service
public class PolicyHandler{
    private final ProductRepository productRepository;

    public PolicyHandler(final ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryStarted_ModifyStock(@Payload DeliveryStarted deliveryStarted){

        if(!deliveryStarted.validate()) {
            return;
        }
        
        Product product = productRepository.findById(deliveryStarted.getProductId()).get();
        product.addStock(deliveryStarted.getQuantity());
        productRepository.save(product);
    }
  ...
}
```

배송시스템은 상품시스템과 완전히 분리되어있으며, 이벤트 수신에 따라 처리되기 때문에, 상품시스템이 유지보수로 인해 잠시 내려간 상태라도 배송처리를 하는데 문제가 없다.

```shell
# 상품(Product)서비스를 잠시 내려놓음

# 발주 요청
http POST http://localhost:8088/orders productId=1 quantity=3 status="order"

# 배송 상태 확인
http GET http://localhost:8088/deliveries?orderId=1		#Success

# 상품(Product)서비스 재기동

# 상품 상태 확인
# 상품시스템 재기동 후 배송 이벤트를 수신하여 상품 입고 처리가 정상완료됨을 확인
http GET localhost:8088/products
```

# 운영

## CI/CD 설정

각 구현체들은 각자의 source repository 에 구성되었고, 사용한 CI/CD 플랫폼은 AWS를 사용하였으며, CodeBuild script 는 각 프로젝트 폴더 이하에 buildspec.yml 에 포함되었다.

- 적용 화면

<img width="1036" alt="스크린샷 2021-07-07 오전 12 29 57" src="https://user-images.githubusercontent.com/14067833/124627470-8c0ede00-deba-11eb-9e1c-49c0cf786d3b.png">

<img width="1048" alt="스크린샷 2021-07-08 오후 12 49 36" src="https://user-images.githubusercontent.com/14067833/124859501-076fad00-dfeb-11eb-9c83-7da7af2330ff.png">

## 동기식 호출 / 서킷 브레이킹 / 장애격리

* 서킷 브레이킹 프레임워크의 선택: istio-injection + DestinationRule

- 시나리오는 발주(order)-->배송(delivery) 시의 연결을 RESTful Request/Response 로 연동하여 구현이 되어있고, 발주 요청이 과도할 경우 CB 를 통하여 장애격리.

- DestinationRule 를 생성하여 circuit break 가 발생할 수 있도록 설정 최소 connection pool 설정

  ```yaml
  apiVersion: networking.istio.io/v1alpha3
  kind: DestinationRule
  metadata:
    name: dr-delivery
    namespace: convenientstore
  spec:
    host: delivery
    trafficPolicy:
      connectionPool:
        http:
          http1MaxPendingRequests: 1
          maxRequestsPerConnection: 1
  ```

- istio-injection 활성화 및 delivery pod container 확인

  ```shell
  kubectl get ns -L istio-injection
  kubectl label namespace convenientstore istio-injection=enabled 
  ```

  <img width="423" alt="스크린샷 2021-07-08 오후 9 39 09" src="https://user-images.githubusercontent.com/14067833/124922982-f9467e80-e034-11eb-8842-39f127b5b191.png">

  <img width="523" alt="스크린샷 2021-07-08 오후 9 45 08" src="https://user-images.githubusercontent.com/14067833/124923851-ce105f00-e035-11eb-9011-060b1fbea099.png">

- 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인:

  - siege 실행

  ```shell
  kubectl run siege --image=apexacme/siege-nginx -n convenientstore
  kubectl exec -it siege -c siege -n convenientstore -- /bin/bash
  ```

  - 동시사용자 1로 부하 생성 시 모두 정상

  ```shell
  siege -c1 -t10S -v --content-type "application/json" 'http://delivery:8080/deliveries POST {"orderId": 1, "productId": 1, "quantity": 3, "status": "delivery"}'
  ```

  <img width="1214" alt="스크린샷 2021-07-08 오후 9 58 06" src="https://user-images.githubusercontent.com/14067833/124925616-999da280-e037-11eb-8b7a-b2c78c8c2121.png">

  - 동시사용자 10로 부하 생성 시 503 에러 78개 발생

  ```shell
  siege -c10 -t10S -v --content-type "application/json" 'http://delivery:8080/deliveries POST {"orderId": 1, "productId": 1, "quantity": 3, "status": "delivery"}'
  ```

  <img width="1224" alt="스크린샷 2021-07-08 오후 10 04 56" src="https://user-images.githubusercontent.com/14067833/124926567-a2db3f00-e038-11eb-9da7-50cb5d04fece.png">

  <img width="577" alt="스크린샷 2021-07-08 오후 10 05 11" src="https://user-images.githubusercontent.com/14067833/124926612-b090c480-e038-11eb-8e79-d1c70fe0673a.png">

  - 다시 최소 Connection pool로 부하시 정상 확인

  <img width="837" alt="스크린샷 2021-07-08 오후 10 20 10" src="https://user-images.githubusercontent.com/14067833/124928638-bbe4ef80-e03a-11eb-8547-190e0bf40412.png">

- 운영시스템은 죽지 않고 지속적으로 CB 에 의하여 적절히 회로가 열림과 닫힘이 벌어지면서 자원을 보호하고 있음을 보여줌. 하지만, 75.24% 가 성공하였고, 25%가 실패했다는 것은 고객 사용성에 있어 좋지 않기 때문에 Retry 설정과 동적 Scale out (replica의 자동적 추가,HPA) 을 통하여 시스템을 확장 해주는 후속처리가 필요.
- Retry 의 설정 (istio)
- Availability 가 높아진 것을 확인 (siege)

## 오토스케일 아웃

앞서 CB 는 시스템을 안정되게 운영할 수 있게 해줬지만 사용자의 요청을 100% 받아들여주지 못했기 때문에 이에 대한 보완책으로 자동화된 확장 기능을 적용하고자 한다.

- (istio injection 적용한 경우) istio injection 적용 해제

  ```shell
  kubectl label namespace convenientstore istio-injection=disabled --overwrite
  ```

- Delivery deployment.yml 파일에 resources 설정을 추가한다

  <img width="695" alt="스크린샷 2021-07-08 오후 11 07 05" src="https://user-images.githubusercontent.com/14067833/124936706-bb9c2280-e041-11eb-867b-944c52f4b911.png">

- 배송 서비스에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 CPU 사용량이 15프로를 넘어서면 replica 를 10개까지 늘려준다.

  ```shell
  kubectl autoscale deploy delivery -n convenientstore --min=1 --max=10 --cpu-percent=15
  ```

​		<img width="866" alt="스크린샷 2021-07-08 오후 11 21 32" src="https://user-images.githubusercontent.com/14067833/124938476-4a5d6f00-e043-11eb-9624-be9114b74e5d.png">

- CB 에서 했던 방식대로 동시 사용자 50명, 워크로드를 1분 동안 걸어준다.

  ```shell
  siege -c50 -t60S -v --content-type "application/json" 'http://delivery:8080/deliveries POST {"orderId": 1, "productId": 1, "quantity": 3, "status": "delivery"}'
  ```

- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다.

  ```shell
  kubectl get deploy delivery -w -n convenientstore
  ```

- 어느정도 시간이 흐른 후 (약 30초) 스케일 아웃이 벌어지는 것을 확인할 수 있다.

  <img width="595" alt="스크린샷 2021-07-08 오후 11 30 17" src="https://user-images.githubusercontent.com/14067833/124940623-1be09380-e045-11eb-9ca8-fca9976dba56.png">

  <img width="593" alt="스크린샷 2021-07-08 오후 11 33 58" src="https://user-images.githubusercontent.com/14067833/124940942-5d713e80-e045-11eb-8d22-1167fa0915ea.png">

- siege 의 로그를 보아도 전체적인 성공률이 높아진 것을 확인 할 수 있다.

  <img width="684" alt="스크린샷 2021-07-08 오후 11 42 22" src="https://user-images.githubusercontent.com/14067833/124941978-2e0f0180-e046-11eb-8fd0-7ad310212374.png">


## 무정지 재배포(Readiness Probe)

* 먼저 무정지 재배포가 100% 되는 것인지 확인하기 위해서 Autoscaler 이나 CB 설정을 제거함

#### Readness probe 미설정 상태

- seige 로 배포작업 직전에 워크로드를 모니터링 함.

```shell
siege -c5 -t120S -v --content-type "application/json" 'http://delivery:8080/deliveries POST {"orderId": 1, "productId": 1, "quantity": 3, "status": "delivery"}'
```

- 새버전으로의 배포 시작

```
kubectl set image ...
```

- seige 의 화면으로 넘어가서 Availability 가 100% 미만으로 떨어졌는지 확인

  <img width="413" alt="스크린샷 2021-07-09 오전 12 13 51" src="https://user-images.githubusercontent.com/14067833/124947521-dc1caa80-e04a-11eb-9c83-64a7f22f4d86.png">

배포기간중 Availability 가 평소 100%에서 90% 대로 떨어지는 것을 확인. 원인은 쿠버네티스가 성급하게 새로 올려진 서비스를 READY 상태로 인식하여 서비스 유입을 진행한 것이기 때문. 이를 막기위해 Readiness Probe 를 설정함:

#### Readness probe 설정 상태

```yaml
# deployment.yml의 readiness probe 설정
# ...
readinessProbe:
  httpGet:
    path: '/actuator/health'
    port: 8080
  initialDelaySeconds: 10
  timeoutSeconds: 2
  periodSeconds: 5
  failureThreshold: 10
```

- 동일한 시나리오로 재배포 한 후 Availability 확인:

  <img width="462" alt="스크린샷 2021-07-09 오전 12 26 16" src="https://user-images.githubusercontent.com/14067833/124949193-4d109200-e04c-11eb-963a-a4ece38db190.png">

배포기간 동안 Availability 가 변화없기 때문에 무정지 재배포가 성공한 것으로 확인됨.

## Self-healing(Liveness Probe)

- Delivery deployment.yml 파일 수정

  ```
  # ...
  args:
    # /tmp/healthy 파일 생성하고 30초 후 삭제
    - /bin/sh
    - -c
    - touch /tmp/healthy; sleep 30; rm -rf /tmp/healthy; sleep 600
  livenessProbe:
    exec:
      command:
        - cat
        - /tmp/healthy
    initialDelaySeconds: 120
    timeoutSeconds: 2
    periodSeconds: 5
    failureThreshold: 5
  ```

- Delivery 기동후 확인

  ``` shell
  kubectl describe pod delivery -n convenientstore
  ```

kubelet이 5 초마다 livenessProbe를 수행해야 한다고 지정했다(periodSeconds). 하지만 수행하기 전에 120초정도 기다리고(initialDelaySeconds) 수행한다(`cat /tmp/healthy` 명령어 수행). 명령이 성공하면 0을 반환하고 kubelet은 컨테이너가 살아 있고 정상인 것으로 간주합니다. 명령이 0이 아닌 값을 반환하면 kubelet은 컨테이너를 종료하고 다시 시작합니다.

<img width="1117" alt="스크린샷 2021-07-09 오전 1 28 13" src="https://user-images.githubusercontent.com/14067833/124960071-e2188880-e056-11eb-94e1-ef8fcf524a41.png">

<img width="551" alt="스크린샷 2021-07-09 오전 1 28 34" src="https://user-images.githubusercontent.com/14067833/124960112-f2306800-e056-11eb-9b8a-47fce723027a.png">

## Config Map/Persistence Volume

1. configmap.yaml 파일 생성

   ```yaml
   apiVersion: v1
   kind: ConfigMap
   metadata:
     name: convenientstore-config
   data:
     api.url.delivery: http://delivery:8080
   ```

2. deployment.yml에 적용

   ```yaml
   # ...
             env:
               - name: api.url.delivery	# configmap.yaml에 있는 key-value
                 valueFrom:
                   configMapKeyRef:
                     name: convenientstore-config
                     key: api.url.delivery
   ```

3. 적용 소스

   ```java
   @FeignClient(name = "delivery", url = "${api.url.delivery}")
   @RequestMapping("/deliveries")
   public interface DeliveryService {
   }
   ```

   
