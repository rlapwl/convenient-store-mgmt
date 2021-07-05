package convenientstore.external;

public class Delivery {

    private Long id;
    private Long orderId;
    private Long productId;
    private int quantity;
    private String status;  // delivery: 배송, cancel: 배송 취소

    public Delivery(final Long orderId, final Long productId, final int quantity, final String status) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.status = status;
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
