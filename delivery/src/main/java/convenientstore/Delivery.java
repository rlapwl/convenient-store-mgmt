package convenientstore;

import javax.persistence.*;

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

    @PreRemove
    public void onPreRemove() {
        DeliveryCanceled deliveryCanceled = new DeliveryCanceled(id, orderId, productId, quantity);
        deliveryCanceled.publishAfterCommit();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
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

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

}
