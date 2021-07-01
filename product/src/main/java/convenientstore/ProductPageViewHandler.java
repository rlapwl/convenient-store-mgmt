package convenientstore;

import convenientstore.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ProductPageViewHandler {


    @Autowired
    private ProductPageRepository productPageRepository;



    @StreamListener(KafkaProcessor.INPUT)
    public void whenStockAdded_then_UPDATE_1(@Payload StockAdded stockAdded) {
        try {
            if (!stockAdded.validate()) return;
                // view 객체 조회
            Optional<ProductPage> productPageOptional = productPageRepository.findById(stockAdded.getId());

            if( productPageOptional.isPresent()) {
                 ProductPage productPage = productPageOptional.get();
            // view 객체에 이벤트의 eventDirectValue 를 set 함
                 productPage.setQuantity(productPage.getQuantity() + stockAdded.getQuantity());
                // view 레파지 토리에 save
                 productPageRepository.save(productPage);
                }


        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenStockModified_then_UPDATE_2(@Payload StockModified stockModified) {
        try {
            if (!stockModified.validate()) return;
                // view 객체 조회
            Optional<ProductPage> productPageOptional = productPageRepository.findById(stockModified.getId());

            if( productPageOptional.isPresent()) {
                 ProductPage productPage = productPageOptional.get();
            // view 객체에 이벤트의 eventDirectValue 를 set 함
                 productPage.setQuantity(productPage.getQuantity() - stockModified.getQuantity());
                // view 레파지 토리에 save
                 productPageRepository.save(productPage);
                }


        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenStockModified_then_UPDATE_3(@Payload StockModified stockModified) {
        try {
            if (!stockModified.validate()) return;
                // view 객체 조회
            Optional<ProductPage> productPageOptional = productPageRepository.findById(stockModified.getId());

            if( productPageOptional.isPresent()) {
                 ProductPage productPage = productPageOptional.get();
            // view 객체에 이벤트의 eventDirectValue 를 set 함
                 productPage.setQuantity(productPage.getQuantity() + stockModified.getQuantity());
                // view 레파지 토리에 save
                 productPageRepository.save(productPage);
                }


        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

