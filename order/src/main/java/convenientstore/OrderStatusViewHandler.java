package convenientstore;

import convenientstore.config.kafka.KafkaProcessor;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrderStatusViewHandler {

    private final OrderStatusRepository orderStatusRepository;

    public OrderStatusViewHandler(OrderStatusRepository orderStatusRepository) {
        this.orderStatusRepository = orderStatusRepository;
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenDeliveryStarted_then_CREATE_1 (@Payload DeliveryStarted deliveryStarted) {
        try {

            if (!deliveryStarted.validate()) {
                return;
            }

            // view 객체 생성
            OrderStatus orderStatus = new OrderStatus();
            // view 객체에 이벤트의 Value 를 set 함
            orderStatus.setProductId(deliveryStarted.getProductId());
            orderStatus.setStatus(deliveryStarted.getStatus());
            orderStatus.setQuantity(deliveryStarted.getQuantity());
            // view 레파지 토리에 save
            orderStatusRepository.save(orderStatus);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenDeliveryCanceled_then_UPDATE_1(@Payload DeliveryCanceled deliveryCanceled) {
        try {
            if (!deliveryCanceled.validate()) {
                return;
            }
            
            // view 객체 조회
            Optional<OrderStatus> orderStatusOptional = orderStatusRepository.findById(deliveryCanceled.getOrderId());

            if (orderStatusOptional.isPresent()) {
                OrderStatus orderStatus = orderStatusOptional.get();
                
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                 orderStatus.setStatus(deliveryCanceled.getStatus());
                
                // view 레파지 토리에 save
                orderStatusRepository.save(orderStatus);
            }
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

