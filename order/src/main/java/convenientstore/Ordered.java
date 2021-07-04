package convenientstore;

public class Ordered extends AbstractEvent {

    private final Long id;
    private final Long productId;
    private final int quantity;
    private final String status;

    public Ordered(final Long id, final Long productId, final int quantity, final String status) {
        super();
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.status = status;
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

    public String getStatus() {
        return status;
    }
}
