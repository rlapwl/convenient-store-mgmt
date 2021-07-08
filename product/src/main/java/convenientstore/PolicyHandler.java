package convenientstore;

import convenientstore.config.kafka.KafkaProcessor;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    private final ProductRepository productRepository;

    public PolicyHandler(final ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryStarted_ModifyStock(@Payload DeliveryStarted deliveryStarted){

        if(!deliveryStarted.validate()) {
            return;
        }

        System.out.println("\n\n##### listener AddStock : " + deliveryStarted.toJson() + "\n\n");
        
        Product product = productRepository.findById(deliveryStarted.getProductId()).get();
        product.addStock(deliveryStarted.getQuantity());
        productRepository.save(product);
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryCanceled_ModifyStock(@Payload DeliveryCanceled deliveryCanceled){

        if(!deliveryCanceled.validate()) {
            return;
        }

        System.out.println("\n\n##### listener SubtractStock : " + deliveryCanceled.toJson() + "\n\n");
        
        Product product = productRepository.findById(deliveryCanceled.getProductId()).get();
        product.subtractStock(deliveryCanceled.getQuantity());
        productRepository.save(product);
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayApproved_ModifyStock(@Payload PayApproved payApproved){

        if(!payApproved.validate()) {
            return;
        }

        System.out.println("\n\n##### listener ModifyStock : " + payApproved.toJson() + "\n\n");

        Product product = productRepository.findById(payApproved.getProductId()).get();
        product.subtractStock(payApproved.getQuantity());
        productRepository.save(product);
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCanceled_ModifyStock(@Payload PayCanceled payCanceled){

        if(!payCanceled.validate()) {
            return;
        }

        System.out.println("\n\n##### listener ModifyStock : " + payCanceled.toJson() + "\n\n");

        Product product = productRepository.findById(payCanceled.getProductId()).get();
        product.addStock(payCanceled.getQuantity());
        productRepository.save(product);
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}

}
