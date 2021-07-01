package convenientstore;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Product_table")
public class Product {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Integer quantity;
    private Integer price;

    @PostPersist
    public void onPostPersist(){
        StockAdded stockAdded = new StockAdded();
        BeanUtils.copyProperties(this, stockAdded);
        stockAdded.publishAfterCommit();

        StockModified stockModified = new StockModified();
        BeanUtils.copyProperties(this, stockModified);
        stockModified.publishAfterCommit();

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }




}
