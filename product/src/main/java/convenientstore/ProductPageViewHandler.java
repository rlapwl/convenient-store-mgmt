package convenientstore;

import convenientstore.config.kafka.KafkaProcessor;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductPageViewHandler {

    private final ProductPageRepository productPageRepository;

    public ProductPageViewHandler(ProductPageRepository productPageRepository) {
        this.productPageRepository = productPageRepository;
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenStockModified_then_UPDATE(@Payload StockModified stockModified) {
        try {
            if (!stockModified.validate()) {
                return;
            }

            // view 객체 조회
            Optional<ProductPage> productPageOptional = productPageRepository.findById(stockModified.getId());

            if (productPageOptional.isPresent()) {
                ProductPage productPage = productPageOptional.get();
                    
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                productPage.setQuantity(stockModified.getQuantity());
                productPage.setStatus(stockModified.getStatus());
                
                // view 레파지토리에 save
                productPageRepository.save(productPage);

            } else {
                // view 레파지토리에 save
                productPageRepository.save(new ProductPage(stockModified.getQuantity(), stockModified.getPrice(), stockModified.getStatus()));
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

