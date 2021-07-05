package convenientstore;

import convenientstore.config.kafka.KafkaProcessor;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    private final OrderRepository orderRepository;

    public PolicyHandler(final OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryStarted_ModifyStatus(@Payload DeliveryStarted deliveryStarted){

        if(!deliveryStarted.validate()) {
            return;
        }

        System.out.println("\n\n##### listener ModifyStatus : " + deliveryStarted.toJson() + "\n\n");

        Order order = orderRepository.findById(deliveryStarted.getOrderId()).get();
        order.setStatus(deliveryStarted.getStatus());
        orderRepository.save(order);
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryCanceled_ModifyStatus(@Payload DeliveryCanceled deliveryCanceled){

        if(!deliveryCanceled.validate()) {
            return;
        }
        
        System.out.println("\n\n##### listener ModifyStatus : " + deliveryCanceled.toJson() + "\n\n");

        Order order = orderRepository.findById(deliveryCanceled.getOrderId()).get();
        order.setStatus(deliveryCanceled.getStatus());
        orderRepository.save(order);
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}

}
