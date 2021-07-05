package convenientstore;

public class DeliveryCanceled extends AbstractEvent {

    private final Long id;
    private final Long orderId;
    private final Long productId;
    private final int quantity;
    private final String status;

    public DeliveryCanceled(final Long id, final Long orderId, final Long productId, final int quantity){
        super();
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.status = "cancel delivery";
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
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
