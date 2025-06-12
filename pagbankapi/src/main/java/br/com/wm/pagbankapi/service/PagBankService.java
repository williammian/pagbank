package br.com.wm.pagbankapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.wm.pagbankapi.dto.OrderRequest;
import br.com.wm.pagbankapi.dto.OrderResponse;
import br.com.wm.pagbankapi.dto.WebhookResponse;
import br.com.wm.pagbankapi.exception.ValidacaoException;
import br.com.wm.pagbankapi.model.Order;
import br.com.wm.pagbankapi.model.Order.OrderStatus;
import br.com.wm.pagbankapi.repository.OrderRepository;
import br.com.wm.pagbankapi.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class PagBankService {
	
	@Value("${pagbank.api.token}")
    private String apiToken;

    private final WebClient pagBankWebClient;
    
    private final OrderRepository orderRepository;

    public Mono<OrderResponse> createOrder(OrderRequest request) {
        log.info("Creating order with reference_id: {}", request.getReferenceId());

        return pagBankWebClient
                .post()
                .uri("/orders")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OrderResponse.class)
                .doOnSuccess(response -> {
                    log.info("Order created successfully with ID: {}", response.getId());
                    saveOrder(request, response);
                })
                .doOnError(error -> {
                	log.error("Error creating order: {}", error.getMessage());
                });

    }

    private void saveOrder(OrderRequest request, OrderResponse response) {
        try {
            Order order = new Order();
            order.setPagbankOrderId(response.getId());
            order.setReferenceId(response.getReferenceId());
            order.setCustomerName(request.getCustomer().getName());
            order.setCustomerEmail(request.getCustomer().getEmail());
            
            // Calculate total amount from items
            int totalAmount = request.getItems().stream()
                    .mapToInt(item -> item.getUnitAmount() * item.getQuantity())
                    .sum();
            order.setTotalAmount(totalAmount);

            // Save QR Code info if available PIX
            if (response.getQrCodes() != null && !response.getQrCodes().isEmpty()) {
            	order.setType(Order.Type.PIX);
            	
                OrderResponse.QrCodeResponse qrCode = response.getQrCodes().get(0);
                order.setQrCodeId(qrCode.getId());
                order.setQrCodeText(qrCode.getText());
                
                // Get PNG image URL
                qrCode.getLinks().stream()
                        .filter(link -> "QRCODE.PNG".equals(link.getRel()))
                        .findFirst()
                        .ifPresent(link -> order.setQrCodeImageUrl(link.getHref()));
                
            } else if (response.getCharges() != null && !response.getCharges().isEmpty()) {
				OrderResponse.Charge charge = response.getCharges().get(0);
				
				if ("BOLETO".equals(charge.getPaymentMethod().getType())) {
					order.setType(Order.Type.BOLETO);
					order.setBoletoId(charge.getId());
					order.setBoletoBarcode(charge.getPaymentMethod().getBoleto().getBarcode());
					order.setBoletoFormattedBarcode(charge.getPaymentMethod().getBoleto().getFormattedBarcode());
					
					charge.getLinks().stream()
						.filter(link -> "application/pdf".equals(link.getMedia())).findFirst()
						.ifPresent(link -> order.setBoletoUrl(link.getHref()));
					
				}else if("CREDIT_CARD".equals(charge.getPaymentMethod().getType())) {
					order.setType(Order.Type.CREDIT_CARD);
				}
			}

            orderRepository.save(order);
            log.info("Order saved to database with ID: {}", order.getId());
        } catch (Exception e) {
            log.error("Error saving order to database: {}", e.getMessage());
        }
    }

    public Mono<Order> getOrderByReferenceId(String referenceId) {
        return Mono.fromCallable(() -> 
            orderRepository.findByReferenceId(referenceId)
                .orElse(null)
        );
    }
    
	public void webhook(String referenceId, String payload, String signature) {
		log.info("Processing webhook for reference_id: {}", referenceId);

		// Validate signature
		String expectedSignature = Util.convertAssinaturaToHex(apiToken, payload);
		if (!signature.equals(expectedSignature)) {
			log.error("Invalid signature for reference_id: {}", referenceId);
			throw new ValidacaoException("Invalid signature");
		}

		// Parse payload
		WebhookResponse payloadResponse = WebhookResponse.fromJson(payload);
		if (payloadResponse.getCharges().isEmpty()) {
			log.error("No charges found in payload for reference_id: {}", referenceId);
			throw new ValidacaoException("No charges found in payload");
		}
    	
		String newStatusFromPayload = payloadResponse.getCharges().get(0).getStatus();
    			
		Order order = orderRepository.findByReferenceId(referenceId).orElseThrow(() -> new ValidacaoException("Order reference_id " + referenceId + " not found"));
		order.setStatus(OrderStatus.valueOf(newStatusFromPayload));
		orderRepository.save(order);

		log.info("Webhook processed successfully for reference_id: {}", referenceId);
    }
    
    public Mono<Order.OrderStatus> getOrderStatusByReferenceId(String referenceId) {
		return Mono.fromCallable(() ->
			orderRepository.findStatusByReferenceId(referenceId)
            .orElseThrow(() -> new ValidacaoException("Order reference_id " + referenceId + " not found"))
		);
    }
    
    public Mono<OrderResponse> getOrderResponseByReferenceId(String referenceId) {
        return pagBankWebClient
                .get()
                .uri("/orders/{referenceId}", referenceId)
                .retrieve()
                .bodyToMono(OrderResponse.class)
                .doOnSuccess(response -> {
                    log.info("Get Order Response successfully with ID: {}", referenceId);
                })
                .doOnError(error -> {
                	log.error("Error Get Order Response: {}", error.getMessage());
                });

    }
    
}
