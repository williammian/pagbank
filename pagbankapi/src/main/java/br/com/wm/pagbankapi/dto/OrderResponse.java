package br.com.wm.pagbankapi.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class OrderResponse {
    private String id;

    @JsonProperty("reference_id")
    private String referenceId; //Datetime 2023-02-08T15:15:11.881-03:00

    @JsonProperty("created_at")
    private String createdAt; //Datetime 2023-02-08T15:15:11.881-03:00

    private Customer customer;
    
    private List<Item> items;
    
    private Amount amount;
    
    private Shipping shipping;

    @JsonProperty("qr_codes")
    private List<QrCodeResponse> qrCodes;

    private List<Charge> charges;
    
    @JsonProperty("notification_urls")
    private List<String> notificationUrls;
    
    private List<Link> links;

    @Data
    public static class Customer {
        private String name;
        private String email;
        @JsonProperty("tax_id")
        private String taxId;
        private List<Phone> phones;
    }

    @Data
    public static class Phone {
        private String country;
        private String area;
        private String number;
        private String type;
    }

    @Data
    public static class Item {
        @JsonProperty("reference_id")
        private String referenceId;
        private String name;
        private String quantity;
        @JsonProperty("unit_amount")
        private String unitAmount;
    }

    @Data
    public static class Amount {
    	private Integer value;
        private String currency;
        private Integer additional;
        private Integer discount;
        private Summary summary;
    }
    
    @Data
    public static class Summary {
        private Integer total;
        private Integer paid;
        private Integer refunded;
    }

    @Data
    public static class Shipping {
        private Address address;
    }

    @Data
    public static class Address {
        private String street;
        private String number;
        private String complement;
        private String locality;
        private String city;
        @JsonProperty("region_code")
        private String regionCode;
        private String country;
        @JsonProperty("postal_code")
        private String postalCode;
    }

    @Data
    public static class QrCodeResponse {
        private String id;
        @JsonProperty("expiration_date")
        private String expirationDate; //Datetime 2023-02-08T15:15:11.881-03:00
        private Amount amount;
        private String text;
        private List<Link> links;
    }

    @Data
    public static class Link {
        private String rel;
        private String href;
        private String media;
        private String type;
    }
    
    @Data
	public static class Charge {
		private String id;
		
		@JsonProperty("reference_id")
		private String referenceId;
		
		private String status;
		
		@JsonProperty("created_at")
		private String createdAt; // Datetime 2023-02-08T15:15:11.881-03:00
		
		@JsonProperty("paid_at")
	    private String paidAt; // Datetime 2023-02-08T15:15:11.881-03:00
	    
	    private String description;
		
		private Amount amount;
		
		@JsonProperty("payment_response")
	    private PaymentResponse paymentResponse;
		
		@JsonProperty("payment_method")
	    private PaymentMethod paymentMethod;
		
		private List<Link> links;
	}
    
    @Data
    public static class PaymentResponse {
        private String code;
        private String message;
        private String reference;
    }
    
    @Data
    public static class PaymentMethod {
        private String type;
        private Integer installments;
        private Boolean capture;
        private Card card;
        private Boleto boleto;
        
        @JsonProperty("soft_descriptor")
        private String softDescriptor;
        
        @Data
        public static class Card {
            private String brand;
            
            @JsonProperty("first_digits")
            private String firstDigits;
            
            @JsonProperty("last_digits")
            private String lastDigits;
            
            @JsonProperty("exp_month")
            private String expMonth;
            
            @JsonProperty("exp_year")
            private String expYear;
            
            private Holder holder;
            
            private Boolean store;
            
            @Data
            public static class Holder {
                private String name;
                
                @JsonProperty("tax_id")
                private String taxId;
            }
        }
        
        @Data
        public static class Boleto {
            private String id;
            
            private String barcode;
            
            @JsonProperty("formatted_barcode")
            private String formattedBarcode;
            
            @JsonProperty("due_date")
            private String dueDate;
            
            @JsonProperty("instruction_lines")
            private InstructionLines instructionLines;
            
            private BoletoHolder holder;
            
            @Data
            public static class InstructionLines {
                @JsonProperty("line_1")
                private String line1;
                
                @JsonProperty("line_2")
                private String line2;
            }
            
            @Data
            public static class BoletoHolder {
                private String name;
                
                @JsonProperty("tax_id")
                private String taxId;
                
                private String email;
                
                private Address address;
            }
        }
    }
}
