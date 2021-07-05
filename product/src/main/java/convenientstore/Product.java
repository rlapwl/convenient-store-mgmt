package convenientstore;

import javax.persistence.*;

@Entity
@Table(name="Product_table")
public class Product {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private int quantity;
    private int price;
    private String status = "register";

    @PostUpdate
    public void onPostUpdate(){
        StockModified stockModified = new StockModified(id, quantity, price, status);
        stockModified.publishAfterCommit();
    }

    public void addStock(int quantity) {
        this.quantity += quantity;
        this.status = "add";
    }

    public void subtractStock(int quantity) {
        this.quantity -= quantity;
        this.status = "subtract";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
