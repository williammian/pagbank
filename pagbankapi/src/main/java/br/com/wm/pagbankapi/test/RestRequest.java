package br.com.wm.pagbankapi.test;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import br.com.wm.pagbankapi.dto.OrderRequest;
import br.com.wm.pagbankapi.dto.OrderResponse;

public class RestRequest {
	
	private static final String API_URL = "http://localhost:8089";
	
	public static ResponseEntity<OrderResponse> createOrder(OrderRequest orderRequest) {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<OrderRequest> requestEntity = new HttpEntity<>(orderRequest, headers);

		ResponseEntity<OrderResponse> response = restTemplate.exchange(API_URL + "/api/payments/create-order", 
				HttpMethod.POST, requestEntity, OrderResponse.class);

		return response;
	}

}
