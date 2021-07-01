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
    @Autowired MessageRepository messageRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrdered_SendMessage(@Payload Ordered ordered){

        if(!ordered.validate()) return;
        // Get Methods


        // Sample Logic //
        System.out.println("\n\n##### listener SendMessage : " + ordered.toJson() + "\n\n");
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderCanceled_SendMessage(@Payload OrderCanceled orderCanceled){

        if(!orderCanceled.validate()) return;
        // Get Methods


        // Sample Logic //
        System.out.println("\n\n##### listener SendMessage : " + orderCanceled.toJson() + "\n\n");
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryStarted_SendMessage(@Payload DeliveryStarted deliveryStarted){

        if(!deliveryStarted.validate()) return;
        // Get Methods


        // Sample Logic //
        System.out.println("\n\n##### listener SendMessage : " + deliveryStarted.toJson() + "\n\n");
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryCanceled_SendMessage(@Payload DeliveryCanceled deliveryCanceled){

        if(!deliveryCanceled.validate()) return;
        // Get Methods


        // Sample Logic //
        System.out.println("\n\n##### listener SendMessage : " + deliveryCanceled.toJson() + "\n\n");
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverStockAdded_SendMessage(@Payload StockAdded stockAdded){

        if(!stockAdded.validate()) return;
        // Get Methods


        // Sample Logic //
        System.out.println("\n\n##### listener SendMessage : " + stockAdded.toJson() + "\n\n");
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverStockModified_SendMessage(@Payload StockModified stockModified){

        if(!stockModified.validate()) return;
        // Get Methods


        // Sample Logic //
        System.out.println("\n\n##### listener SendMessage : " + stockModified.toJson() + "\n\n");
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayApproved_SendMessage(@Payload PayApproved payApproved){

        if(!payApproved.validate()) return;
        // Get Methods


        // Sample Logic //
        System.out.println("\n\n##### listener SendMessage : " + payApproved.toJson() + "\n\n");
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCanceled_SendMessage(@Payload PayCanceled payCanceled){

        if(!payCanceled.validate()) return;
        // Get Methods


        // Sample Logic //
        System.out.println("\n\n##### listener SendMessage : " + payCanceled.toJson() + "\n\n");
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
