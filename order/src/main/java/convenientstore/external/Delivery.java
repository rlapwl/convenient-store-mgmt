package convenientstore.external;

public class Delivery {

    private Long id;
    private Long orderId;
    private Long productId;
    private int quantity;

    public Delivery(final Long orderId, final Long productId, final int quantity) {
        this.orderId = orderId;
        this.productId = productId;
        this. quantity = quantity;
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

}
