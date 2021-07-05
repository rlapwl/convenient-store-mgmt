package convenientstore;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

 @RestController
 @RequestMapping("/deliveries")
 public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(final DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping
    public Delivery findDelivery(@RequestParam("orderId") Long orderId) {
        return deliveryService.findDelivery(orderId);
    }

    @PostMapping
    public void deliver(@RequestBody Delivery delivery) {
        deliveryService.deliver(delivery);
    }

    @DeleteMapping(path = "/{deliveryId}")
    public void cancelDelivery(@PathVariable Long deliveryId) {
        deliveryService.cancelDelivery(deliveryId);
    }
 }
