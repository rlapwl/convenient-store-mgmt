
package convenientstore.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "delivery", url = "${api.url.delivery}")
@RequestMapping("/deliveries")
public interface DeliveryService {

    @GetMapping
    public Delivery findDelivery(@RequestParam("orderId") Long orderId);

    @PostMapping
    public void deliver(@RequestBody Delivery delivery);

    @DeleteMapping(path = "/{deliveryId}")
    public void cancelDelivery(@PathVariable Long deliveryId);

}

