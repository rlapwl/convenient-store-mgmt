
package convenientstore.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

//@FeignClient(name="delivery", url="http://delivery:8080/deliveries")
@FeignClient(name="delivery", url="http://localhost:8082/deliveries")
public interface DeliveryService {

    @PostMapping
    public void deliver(@RequestBody Delivery delivery);

    @PutMapping
    public void cancelDelivery(@RequestBody Delivery delivery);

}

