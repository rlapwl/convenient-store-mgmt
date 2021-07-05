package convenientstore;

import convenientstore.config.kafka.KafkaProcessor;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    private final MessageRepository messageRepository;

    public PolicyHandler(final MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrdered_SendMessage(@Payload Ordered ordered){

        if (!ordered.validate()) {
            return;
        }

        System.out.println("\n\n##### listener SendMessage : " + ordered.toJson() + "\n\n");

        String content = "상품 ID : " + ordered.getProductId() + ", 수량 : " + ordered.getQuantity() + " 발주 신청됨";
        messageRepository.save(new Message(content));
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderCanceled_SendMessage(@Payload OrderCanceled orderCanceled){

        if (!orderCanceled.validate()) {
            return;
        }

        System.out.println("\n\n##### listener SendMessage : " + orderCanceled.toJson() + "\n\n");

        String content = "상품 ID : " + orderCanceled.getProductId() + ", 수량 : " + orderCanceled.getQuantity() + " 발주 취소됨";
        messageRepository.save(new Message(content));
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryStarted_SendMessage(@Payload DeliveryStarted deliveryStarted){

        if (!deliveryStarted.validate()) {
            return;
        }

        System.out.println("\n\n##### listener SendMessage : " + deliveryStarted.toJson() + "\n\n");

        String content = "상품 ID : " + deliveryStarted.getProductId() + ", 수량 : " + deliveryStarted.getQuantity() + " 배송됨";
        messageRepository.save(new Message(content));
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryCanceled_SendMessage(@Payload DeliveryCanceled deliveryCanceled){

        if (!deliveryCanceled.validate()) {
            return;
        }

        System.out.println("\n\n##### listener SendMessage : " + deliveryCanceled.toJson() + "\n\n");

        String content = "상품 ID : " + deliveryCanceled.getProductId() + ", 수량 : " + deliveryCanceled.getQuantity() + " 배송 취소됨";
        messageRepository.save(new Message(content));
    }
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverStockModified_SendMessage(@Payload StockModified stockModified){

        if (!stockModified.validate()) {
            return;
        }

        System.out.println("\n\n##### listener SendMessage : " + stockModified.toJson() + "\n\n");

        String content = stockModified.getStatus() + " 상품 ID : " + stockModified.getId() + ", 총 수량 : " + stockModified.getQuantity();
        messageRepository.save(new Message(content));
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayApproved_SendMessage(@Payload PayApproved payApproved){

        if (!payApproved.validate()) {
            return;
        }

        System.out.println("\n\n##### listener SendMessage : " + payApproved.toJson() + "\n\n");

        String content = "상품 ID : " + payApproved.getProductId() + ", 금액 : " + payApproved.getPrice() + " 결제됨";
        messageRepository.save(new Message(content));
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCanceled_SendMessage(@Payload PayCanceled payCanceled){

        if (!payCanceled.validate()) {
            return;
        }

        System.out.println("\n\n##### listener SendMessage : " + payCanceled.toJson() + "\n\n");

        String content = "상품 ID : " + payCanceled.getProductId() + ", 금액 : " + payCanceled.getPrice() + " 결제 취소됨";
        messageRepository.save(new Message(content));
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}

}
