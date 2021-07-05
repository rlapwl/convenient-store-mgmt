package convenientstore;

public class StockModified extends AbstractEvent {

    private final Long id;
    private final int quantity;
    private final int price;
    private final String status;

    public StockModified(final Long id, final int quantity, final int price, final String status) {
        super();
        this.id = id;
        this.quantity = quantity;
        this.price = price;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }
}
