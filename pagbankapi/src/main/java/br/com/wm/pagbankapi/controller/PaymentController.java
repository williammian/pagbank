package br.com.wm.pagbankapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.wm.pagbankapi.dto.OrderRequest;
import br.com.wm.pagbankapi.dto.OrderResponse;
import br.com.wm.pagbankapi.model.Order;
import br.com.wm.pagbankapi.service.PagBankService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PagBankService pagBankService;

    @PostMapping("/create-order")
    public Mono<ResponseEntity<OrderResponse>> createOrder(@Valid @RequestBody OrderRequest request) {
        log.info("Received create order request for reference_id: {}", request.getReferenceId());
        
        return pagBankService.createOrder(request)
                .map(ResponseEntity::ok)
                .onErrorResume(throwable -> {
                    log.error("Error in controller: {}", throwable.getMessage(), throwable);
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }
    
    @GetMapping("/order/response/{referenceId}")
    public Mono<ResponseEntity<OrderResponse>> getOrderResponseByReferenceId(@PathVariable String referenceId) {
    	log.info("Geting order response by reference_id: {}", referenceId);
    	
        return pagBankService.getOrderResponseByReferenceId(referenceId)
                .map(orderResponse -> orderResponse != null ? ResponseEntity.ok(orderResponse) : ResponseEntity.notFound().build());
    }

    @GetMapping("/order/{referenceId}")
    public Mono<ResponseEntity<Order>> getOrderByReferenceId(@PathVariable String referenceId) {
    	log.info("Fetching order by reference_id: {}", referenceId);
    	
        return pagBankService.getOrderByReferenceId(referenceId)
                .map(order -> order != null ? ResponseEntity.ok(order) : ResponseEntity.notFound().build());
    }

    @PostMapping("/notifications/{referenceId}")
    public ResponseEntity<String> webhook(@PathVariable String referenceId, 
    		@RequestBody String payload, @RequestHeader("x-authenticity-token") String assinatura) {
        log.info("Received webhook: {}", payload);
        
        pagBankService.webhook(referenceId, payload, assinatura);
        
        return ResponseEntity.ok("Webhook received successfully");
    }
    
    @GetMapping("/order/status/{referenceId}")
	public Mono<ResponseEntity<String>> getOrderStatusByReferenceId(@PathVariable String referenceId) {
    	log.info("Fetching order status for reference_id: {}", referenceId);
    	
		return pagBankService.getOrderStatusByReferenceId(referenceId).map(status -> ResponseEntity.ok(status.name()))
				.onErrorResume(throwable -> {
					log.error("Error fetching order status: {}", throwable.getMessage(), throwable);
					return Mono.just(ResponseEntity.badRequest().build());
				});
	}
}
