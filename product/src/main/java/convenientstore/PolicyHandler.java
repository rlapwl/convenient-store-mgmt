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
    @Autowired ProductRepository productRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryStarted_AddStock(@Payload DeliveryStarted deliveryStarted){

        if(!deliveryStarted.validate()) return;
        // Get Methods


        // Sample Logic //
        System.out.println("\n\n##### listener AddStock : " + deliveryStarted.toJson() + "\n\n");
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayApproved_ModifyStock(@Payload PayApproved payApproved){

        if(!payApproved.validate()) return;
        // Get Methods


        // Sample Logic //
        System.out.println("\n\n##### listener ModifyStock : " + payApproved.toJson() + "\n\n");
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCanceled_ModifyStock(@Payload PayCanceled payCanceled){

        if(!payCanceled.validate()) return;
        // Get Methods


        // Sample Logic //
        System.out.println("\n\n##### listener ModifyStock : " + payCanceled.toJson() + "\n\n");
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
