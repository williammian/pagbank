package br.com.wm.pagbankapi.test;

import lombok.Data;

@Data
public class FormData {
	private String customerName;
    private String customerEmail;
    private String customerTaxId;
    private String customerPhone;
    private String itemName;
    private Double itemPrice;
    private Integer itemQuantity;
    private String street;
    private String number;
    private String complement;
    private String locality;
    private String city;
    private String regionCode;
    private String postalCode;
    
    public static FormData createMockFormData() {
        FormData formData = new FormData();
        formData.setCustomerName("José da Silva");
        formData.setCustomerEmail("jose@email.com");
        formData.setCustomerTaxId("12345678909");
        formData.setCustomerPhone("11999999999");
        formData.setItemName("Produto Teste");
        formData.setItemPrice(29.90);
        formData.setItemQuantity(1);
        formData.setStreet("Avenida Brigadeiro Faria Lima");
        formData.setNumber("1384");
        formData.setComplement("apto 12");
        formData.setLocality("Pinheiros");
        formData.setCity("São Paulo");
        formData.setRegionCode("SP");
        formData.setPostalCode("01452002");
        return formData;
    }
}
