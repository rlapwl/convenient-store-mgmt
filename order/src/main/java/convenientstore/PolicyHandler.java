package convenientstore;

import convenientstore.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired OrderRepository orderRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryStarted_ModifyStatus(@Payload DeliveryStarted deliveryStarted){

        if(!deliveryStarted.validate()) return;
        // Get Methods


        // Sample Logic //
        System.out.println("\n\n##### listener ModifyStatus : " + deliveryStarted.toJson() + "\n\n");
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryCanceled_ModifyStatus(@Payload DeliveryCanceled deliveryCanceled){

        if(!deliveryCanceled.validate()) return;
        // Get Methods


        // Sample Logic //
        System.out.println("\n\n##### listener ModifyStatus : " + deliveryCanceled.toJson() + "\n\n");
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
