package convenientstore;

public class PayCanceled extends AbstractEvent {

    private final Long id;
    private final Long productId;
    private final int quantity;
    private final int price;

    public PayCanceled(final Long id, final Long productId, final int quantity, final int price){
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
