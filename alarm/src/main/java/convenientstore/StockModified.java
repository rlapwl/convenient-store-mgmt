package convenientstore;

public class StockModified extends AbstractEvent {

    private final Long id;
    private final int quantity;
    private final String status;

    public StockModified(final Long id, final int quantity, final String status) {
        super();
        this.id = id;
        this.quantity = quantity;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getStatus() {
        return status;
    }
}
