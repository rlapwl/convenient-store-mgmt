package convenientstore;

public class PayApproved extends AbstractEvent {

    private final Long id;
    private final Long productId;
    private final int  quantity;
    private final int price;

    public PayApproved(Long id, Long productId, int quantity, int price) {
        super();
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getPrice() {
        return price;
    }
}
