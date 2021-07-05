package convenientstore;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class DeliveryService {
    
    private final DeliveryRepository deliveryRepository;

    public DeliveryService(final DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }
    
    public Delivery findDelivery(final Long orderId) {
        List<Delivery> deliverys = (List<Delivery>) deliveryRepository.findAll();
        return deliverys.stream()
                        .filter(delivery -> delivery.getOrderId() == orderId)
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("해당 Delivery가 없습니다."));
    }

    public void deliver(final Delivery delivery) {
        deliveryRepository.save(delivery);
    }

    public void cancelDelivery(final Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId).get();
        deliveryRepository.delete(delivery);
    }
}
