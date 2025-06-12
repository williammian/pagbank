package br.com.wm.pagbankapi.test;

import lombok.Data;

@Data
public class CreditCardData {
	
	private String number;
	private String holderName;
	private String expiryMonth;
	private String expiryYear;
	private String cvv;
	private Integer installments;
	
	public static CreditCardData createMockCreditCardData() {
		CreditCardData creditCard = new CreditCardData();
		creditCard.setNumber("4539620659922097");
		creditCard.setHolderName("Jose da Silva");
		creditCard.setExpiryMonth("12");
		creditCard.setExpiryYear("2026");
		creditCard.setCvv("123");
		creditCard.setInstallments(1);
		return creditCard;
	}

}
