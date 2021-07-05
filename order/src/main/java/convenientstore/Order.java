package convenientstore;

import javax.persistence.*;

import convenientstore.external.Delivery;

@Entity
@Table(name = "Order_table")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    private int quantity;

    private String status;  // order: 상품 발주, cancel: 발주 취소

    @PostPersist
    public void onPostPersist() {
        if ("order".equals(this.status)) {
            Delivery delivery = new Delivery(id, productId, quantity, "delivery");
            OrderApplication.applicationContext.getBean(convenientstore.external.DeliveryService.class).deliver(delivery);

            Ordered ordered = new Ordered(id, productId, quantity, "order");
            ordered.publishAfterCommit();
        }
    }

    @PreRemove
    public void onPreRemove() {
        Delivery delivery = OrderApplication.applicationContext.getBean(convenientstore.external.DeliveryService.class)
                    .findDelivery(getId());

        OrderApplication.applicationContext.getBean(convenientstore.external.DeliveryService.class)
                .cancelDelivery(delivery.getId());

        OrderCanceled orderCanceled = new OrderCanceled(id, productId, quantity, "cancel");
        orderCanceled.publishAfterCommit();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
