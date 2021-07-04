package convenientstore;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

import convenientstore.external.Delivery;

@Entity
@Table(name="Order_table")
public class Order {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    private int quantity;

    private String status = "Order";

    @PostPersist
    public void onPostPersist() {
        Delivery delivery = new Delivery(id, productId, quantity);
        OrderApplication.applicationContext.getBean(convenientstore.external.DeliveryService.class)
            .deliver(delivery);

        Ordered ordered = new Ordered(id, productId, quantity, status);
        ordered.publishAfterCommit();
    }

    @PreRemove
    public void onPreRemove() {
        OrderCanceled orderCanceled = new OrderCanceled();
        BeanUtils.copyProperties(this, orderCanceled);
        orderCanceled.publishAfterCommit();

        // Delivery delivery = new Delivery();
        // OrderApplication.applicationContext.getBean(convenientstore.external.DeliveryService.class)
        //     .cancelDelivery(delivery);
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
