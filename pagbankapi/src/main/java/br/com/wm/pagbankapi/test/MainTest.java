package br.com.wm.pagbankapi.test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import org.springframework.http.ResponseEntity;

import br.com.wm.pagbankapi.dto.OrderRequest;
import br.com.wm.pagbankapi.dto.OrderResponse;
import br.com.wm.pagbankapi.model.Order;

public class MainTest {
	
	public static void main(String[] args) {
		try {
			System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
			
			FormData formData = FormData.createMockFormData();
			
			Order.Type orderType = Order.Type.PIX; // Pode ser PIX, BOLETO ou CREDIT_CARD
			
			OrderResponse orderResponse = null;
			if (orderType == Order.Type.PIX) {
				orderResponse = createPixOrder(formData);
			} else if (orderType == Order.Type.BOLETO) {
				orderResponse = createBoletoOrder(formData);
			} else if (orderType == Order.Type.CREDIT_CARD) {
				CreditCardData creditCardData = CreditCardData.createMockCreditCardData();
				orderResponse = createCreditCardOrder(formData, creditCardData);
			}
			
			System.out.println("Pedido criado com sucesso!");
            System.out.println("ID do Pedido: " + orderResponse.getId());
            System.out.println("Reference ID: " + orderResponse.getReferenceId());
            System.out.println("Criado em: " + orderResponse.getCreatedAt());
            
			if (orderType == Order.Type.PIX) {
				System.out.println("Tipo de Pedido: PIX");
				
	            if (orderResponse.getQrCodes() != null && !orderResponse.getQrCodes().isEmpty()) {
	                OrderResponse.QrCodeResponse qrCode = orderResponse.getQrCodes().get(0);
	                System.out.println("QR Code ID: " + qrCode.getId());
	                System.out.println("QR Code Text: " + qrCode.getText());
	                System.out.println("Expira em: " + qrCode.getExpirationDate());
	            }
	            
			} else if (orderType == Order.Type.BOLETO) {
				System.out.println("Tipo de Pedido: BOLETO");
				
				if (orderResponse.getCharges() != null && !orderResponse.getCharges().isEmpty()) {
					OrderResponse.Charge charge = orderResponse.getCharges().get(0);
					System.out.println("ID do Charge: " + charge.getId());
					System.out.println("Valor do Charge: " + charge.getAmount().getValue() / 100.0);
					System.out.println("Vencimento: " + charge.getPaymentMethod().getBoleto().getDueDate());
					System.out.println(
							"Boleto URL: " + charge.getLinks().stream()
							.filter(link -> "application/pdf".equals(link.getMedia())).findFirst()
							.map(link -> link.getHref()).orElse("N/A"));
				}
				
			} else if(orderType == Order.Type.CREDIT_CARD) {
				System.out.println("Tipo de Pedido: CARTÃO DE CRÉDITO");
				
				if (orderResponse.getCharges() != null && !orderResponse.getCharges().isEmpty()) {
					OrderResponse.Charge charge = orderResponse.getCharges().get(0);
					System.out.println("ID do Charge: " + charge.getId());
					System.out.println("Valor do Charge: " + charge.getAmount().getValue() / 100.0);
					System.out.println("Parcelas: " + charge.getPaymentMethod().getInstallments());
				}
			}
			
		} catch (Exception e) {
            System.err.println("Erro ao criar pedido de pagamento: " + e.getMessage());
            e.printStackTrace();
        }
	}

	private static OrderResponse createPixOrder(FormData formData) throws Exception {       
        // Gerar reference ID
        String referenceId = "REF-" + System.currentTimeMillis();
        
        // Calcular valor em centavos
        Integer amountInCents = (int) Math.round(formData.getItemPrice() * 100);
        
        // Criar data de expiração (1 hora a partir de agora)
        LocalDateTime expirationDate = LocalDateTime.now().plusHours(1);
        String expirationDateStr = toLocalISOString(expirationDate);
        
        // Construir objeto de requisição
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setReferenceId(referenceId);
        
        // Customer
        OrderRequest.Customer customer = new OrderRequest.Customer();
        customer.setName(formData.getCustomerName());
        customer.setEmail(formData.getCustomerEmail());
        customer.setTaxId(formData.getCustomerTaxId());
        
        // Phone
        OrderRequest.Phone phone = new OrderRequest.Phone();
        phone.setCountry("55");
        phone.setArea(formData.getCustomerPhone().substring(0, 2));
        phone.setNumber(formData.getCustomerPhone().substring(2));
        phone.setType("MOBILE");
        customer.setPhones(Arrays.asList(phone));
        
        orderRequest.setCustomer(customer);
        
        // Items
        OrderRequest.Item item = new OrderRequest.Item();
        item.setReferenceId("item-" + referenceId);
        item.setName(formData.getItemName());
        item.setQuantity(formData.getItemQuantity());
        item.setUnitAmount(amountInCents);
        orderRequest.setItems(Arrays.asList(item));
        
        // QR Codes
        OrderRequest.QrCode qrCode = new OrderRequest.QrCode();
        OrderRequest.Amount qrAmount = new OrderRequest.Amount();
        qrAmount.setValue(amountInCents * formData.getItemQuantity());
        qrCode.setAmount(qrAmount);
        qrCode.setExpirationDate(expirationDateStr);
        orderRequest.setQrCodes(Arrays.asList(qrCode));
        
        // Shipping
        OrderRequest.Shipping shipping = new OrderRequest.Shipping();
        OrderRequest.Address address = new OrderRequest.Address();
        address.setStreet(formData.getStreet());
        address.setNumber(formData.getNumber());
        address.setComplement(formData.getComplement());
        address.setLocality(formData.getLocality());
        address.setCity(formData.getCity());
        address.setRegionCode(formData.getRegionCode());
        address.setCountry("BRA");
        address.setPostalCode(formData.getPostalCode());
        shipping.setAddress(address);
        orderRequest.setShipping(shipping);
        
        // Notification URLs
        orderRequest.setNotificationUrls(Arrays.asList("https://meusite.com/api/payments/notifications/" + referenceId));
        
        ResponseEntity<OrderResponse> response = RestRequest.createOrder(orderRequest);
        
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Erro HTTP: " + response.getStatusCode());
        }
        
        return response.getBody();
    }
	
	private static OrderResponse createBoletoOrder(FormData formData) throws Exception {
		// Gerar reference ID
	    String referenceId = "REF-" + System.currentTimeMillis();
	    
	    // Calcular valor em centavos
	    Integer amountInCents = (int) Math.round(formData.getItemPrice() * 100);
	    
	    // Criar data de vencimento (7 dias a partir de agora)
	    LocalDateTime dueDateTime = LocalDateTime.now().plusDays(7);
	    String dueDateString = dueDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	    
	    // Construir objeto de requisição
	    OrderRequest orderRequest = new OrderRequest();
	    orderRequest.setReferenceId(referenceId);
	    
	    // Customer
	    OrderRequest.Customer customer = new OrderRequest.Customer();
	    customer.setName(formData.getCustomerName());
	    customer.setEmail(formData.getCustomerEmail());
	    customer.setTaxId(formData.getCustomerTaxId());
	    
	    // Phone
	    OrderRequest.Phone phone = new OrderRequest.Phone();
	    phone.setCountry("55");
	    phone.setArea(formData.getCustomerPhone().substring(0, 2));
	    phone.setNumber(formData.getCustomerPhone().substring(2));
	    phone.setType("MOBILE");
	    customer.setPhones(Arrays.asList(phone));
	    
	    orderRequest.setCustomer(customer);
	    
	    // Items
	    OrderRequest.Item item = new OrderRequest.Item();
	    item.setReferenceId("item-" + referenceId);
	    item.setName(formData.getItemName());
	    item.setQuantity(formData.getItemQuantity());
	    item.setUnitAmount(amountInCents);
	    orderRequest.setItems(Arrays.asList(item));
	    
	    // Shipping
	    OrderRequest.Shipping shipping = new OrderRequest.Shipping();
	    OrderRequest.Address address = new OrderRequest.Address();
	    address.setStreet(formData.getStreet());
	    address.setNumber(formData.getNumber());
	    address.setComplement(formData.getComplement());
	    address.setLocality(formData.getLocality());
	    address.setCity(formData.getCity());
	    address.setRegionCode(formData.getRegionCode());
	    address.setCountry("BRA");
	    address.setPostalCode(formData.getPostalCode());
	    shipping.setAddress(address);
	    orderRequest.setShipping(shipping);
	    
	    // Notification URLs
	    orderRequest.setNotificationUrls(Arrays.asList("https://meusite.com/api/payments/notifications/" + referenceId));
	    
	    // Charges (específico para Boleto)
	    OrderRequest.Charge charge = new OrderRequest.Charge();
	    charge.setReferenceId("charge-" + referenceId);
	    charge.setDescription("Pagamento de " + formData.getItemName());
	    
	    // Amount para o charge
	    OrderRequest.Amount chargeAmount = new OrderRequest.Amount();
	    chargeAmount.setValue(amountInCents * formData.getItemQuantity());
	    chargeAmount.setCurrency("BRL");
	    charge.setAmount(chargeAmount);
	    
	    // Payment Method - Boleto
	    OrderRequest.PaymentMethod paymentMethod = new OrderRequest.PaymentMethod();
	    paymentMethod.setType("BOLETO");
	    
	    // Boleto específico
	    OrderRequest.Boleto boleto = new OrderRequest.Boleto();
	    boleto.setDueDate(dueDateString);
	    
	    // Instruction lines
	    OrderRequest.Boleto.InstructionLines instructionLines = new OrderRequest.Boleto.InstructionLines();
	    instructionLines.setLine1("Pagamento processado para DESC Fatura");
	    instructionLines.setLine2("Via PagSeguro");
	    boleto.setInstructionLines(instructionLines);
	    
	    // Holder do boleto
	    OrderRequest.Holder boletoHolder = new OrderRequest.Holder();
	    boletoHolder.setName(formData.getCustomerName());
	    boletoHolder.setTaxId(formData.getCustomerTaxId());
	    boletoHolder.setEmail(formData.getCustomerEmail());
	    
	    // Address do holder
	    OrderRequest.Address holderAddress = new OrderRequest.Address();
	    holderAddress.setCountry("Brasil");
	    holderAddress.setRegion(formData.getCity());
	    holderAddress.setRegionCode(formData.getRegionCode());
	    holderAddress.setCity(formData.getCity());
	    holderAddress.setPostalCode(formData.getPostalCode());
	    holderAddress.setStreet(formData.getStreet());
	    holderAddress.setNumber(formData.getNumber());
	    holderAddress.setLocality(formData.getLocality());
	    boletoHolder.setAddress(holderAddress);
	    
	    boleto.setHolder(boletoHolder);
	    paymentMethod.setBoleto(boleto);
	    charge.setPaymentMethod(paymentMethod);
	    
	    orderRequest.setCharges(Arrays.asList(charge));
	    
	    ResponseEntity<OrderResponse> response = RestRequest.createOrder(orderRequest);
	    
	    if (!response.getStatusCode().is2xxSuccessful()) {
	        throw new RuntimeException("Erro HTTP: " + response.getStatusCode());
	    }
	    
	    return response.getBody();
	}
	
	private static OrderResponse createCreditCardOrder(FormData formData, CreditCardData creditCardData) throws Exception {
		PagSeguroCardEncryption encryption = null;
		try {
			encryption = new PagSeguroCardEncryption();
			
			EncryptionResult encryptionResult = encryption.encryptCard(
				PagSeguroCardEncryption.CHAVE_PUBLICA_PAGBANK,
            	creditCardData.getHolderName(),
            	creditCardData.getNumber(),
            	creditCardData.getExpiryMonth(),
            	creditCardData.getExpiryYear(), 
            	creditCardData.getCvv()
            );
			
			if(encryptionResult.hasErrors()) throw new RuntimeException("Erro ao criptografar dados do cartão de crédito.");
			
			String encryptedCard = encryptionResult.getEncryptedCard();
					
			// Gerar reference ID
	        String referenceId = "REF-" + System.currentTimeMillis();
	        
	        // Calcular valor em centavos
	        Integer amountInCents = (int) Math.round(formData.getItemPrice() * 100);
	        
	        // Construir objeto de requisição
	        OrderRequest orderRequest = new OrderRequest();
	        orderRequest.setReferenceId(referenceId);
	        
	        // Customer
	        OrderRequest.Customer customer = new OrderRequest.Customer();
	        customer.setName(formData.getCustomerName());
	        customer.setEmail(formData.getCustomerEmail());
	        customer.setTaxId(formData.getCustomerTaxId());
	        
	        // Phone
	        OrderRequest.Phone phone = new OrderRequest.Phone();
	        phone.setCountry("55");
	        phone.setArea(formData.getCustomerPhone().substring(0, 2));
	        phone.setNumber(formData.getCustomerPhone().substring(2));
	        phone.setType("MOBILE");
	        customer.setPhones(Arrays.asList(phone));
	        
	        orderRequest.setCustomer(customer);
	        
	        // Items
	        OrderRequest.Item item = new OrderRequest.Item();
	        item.setReferenceId("item-" + referenceId);
	        item.setName(formData.getItemName());
	        item.setQuantity(formData.getItemQuantity());
	        item.setUnitAmount(amountInCents);
	        orderRequest.setItems(Arrays.asList(item));
	        
	        // Shipping
	        OrderRequest.Shipping shipping = new OrderRequest.Shipping();
	        OrderRequest.Address address = new OrderRequest.Address();
	        address.setStreet(formData.getStreet());
	        address.setNumber(formData.getNumber());
	        address.setComplement(formData.getComplement());
	        address.setLocality(formData.getLocality());
	        address.setCity(formData.getCity());
	        address.setRegionCode(formData.getRegionCode());
	        address.setCountry("BRA");
	        address.setPostalCode(formData.getPostalCode());
	        shipping.setAddress(address);
	        orderRequest.setShipping(shipping);
	        
	        // Notification URLs
	        orderRequest.setNotificationUrls(Arrays.asList("https://meusite.com/api/payments/notifications/" + referenceId));
	        
	        // Charges (específico para Cartão de Crédito)
	        OrderRequest.Charge charge = new OrderRequest.Charge();
	        charge.setReferenceId("charge-" + referenceId);
	        charge.setDescription("Pagamento de " + formData.getItemName());
	        
	        // Amount para o charge
	        OrderRequest.Amount chargeAmount = new OrderRequest.Amount();
	        chargeAmount.setValue(amountInCents * formData.getItemQuantity());
	        chargeAmount.setCurrency("BRL");
	        charge.setAmount(chargeAmount);
	        
	        // Payment Method - Credit Card
	        OrderRequest.PaymentMethod paymentMethod = new OrderRequest.PaymentMethod();
	        paymentMethod.setType("CREDIT_CARD");
	        paymentMethod.setInstallments(creditCardData.getInstallments());
	        paymentMethod.setCapture(true);
	        
	        // Card
	        OrderRequest.Card card = new OrderRequest.Card();
	        card.setEncrypted(encryptedCard);
	        card.setStore(false);
	        paymentMethod.setCard(card);
	        
	        // Holder do cartão
	        OrderRequest.Holder cardHolder = new OrderRequest.Holder();
	        cardHolder.setName(creditCardData.getHolderName());
	        cardHolder.setTaxId(formData.getCustomerTaxId());
	        paymentMethod.setHolder(cardHolder);
	        
	        charge.setPaymentMethod(paymentMethod);
	        orderRequest.setCharges(Arrays.asList(charge));
	        
	        ResponseEntity<OrderResponse> response = RestRequest.createOrder(orderRequest);
	        
	        if (!response.getStatusCode().is2xxSuccessful()) {
	            throw new RuntimeException("Erro HTTP: " + response.getStatusCode());
	        }
	        
	        return response.getBody();
		}catch (Exception e) {
			throw e;
		}finally {
			if(encryption != null) encryption.close();
		}
	}
	
	private static String toLocalISOString(LocalDateTime dateTime) {
        // Formato: 2023-02-08T15:15:11.881-03:00
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'-03:00'");
        return dateTime.format(formatter);
    }
	
}
