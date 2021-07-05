package convenientstore;

import javax.persistence.*;

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

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

}
