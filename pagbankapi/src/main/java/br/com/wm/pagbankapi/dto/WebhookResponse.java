package br.com.wm.pagbankapi.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

@Data
public class WebhookResponse {
    private String id;
    
    @JsonProperty("reference_id")
    private String referenceId;
    
    @JsonProperty("created_at")
    private String createdAt; //Datetime 2023-02-08T15:15:11.881-03:00
    
    private Shipping shipping;
    private List<Item> items;
    private Customer customer;
    private List<Charge> charges;
    
    @JsonProperty("qr_code")
    private List<QrCode> qrCode;
    
    private List<Link> links;
    
    @Data
    public static class Shipping {
        private Address address;
        
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
    }
    
    @Data
    public static class Item {
        @JsonProperty("reference_id")
        private String referenceId;
        
        private String name;
        private Integer quantity;
        
        @JsonProperty("unit_amount")
        private Integer unitAmount;
    }
    
    @Data
    public static class Customer {
        private String name;
        private String email;
        
        @JsonProperty("tax_id")
        private String taxId;
        
        private List<Phone> phones;
        
        @Data
        public static class Phone {
            private String country;
            private String area;
            private String number;
            private String type;
        }
    }
    
    @Data
    public static class Charge {
        private String id;
        
        @JsonProperty("reference_id")
        private String referenceId;
        
        private String status;
        
        @JsonProperty("created_at")
        private String createdAt; //Datetime 2023-02-08T15:15:11.881-03:00
        
        @JsonProperty("paid_at")
        private String paidAt; //Datetime 2023-02-08T15:15:11.881-03:00
        
        private String description;
        
        private Amount amount;
        
        @JsonProperty("payment_response")
        private PaymentResponse paymentResponse;
        
        @JsonProperty("payment_method")
        private PaymentMethod paymentMethod;
        
        private List<Link> links;
        
        @Data
        public static class Amount {
            private Integer value;
            private String currency;
            private Summary summary;
            
            @Data
            public static class Summary {
                private Integer total;
                private Integer paid;
                private Integer refunded;
            }
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
            private Pix pix;
            
            @Data
            public static class Pix {
                @JsonProperty("notification_id")
                private String notificationId;
                
                @JsonProperty("end_to_end_id")
                private String endToEndId;
                
                private Holder holder;
                
                @Data
                public static class Holder {
                    private String name;
                    
                    @JsonProperty("tax_id")
                    private String taxId;
                }
            }
        }
    }
    
    @Data
    public static class QrCode {
        private String id;
        private Amount amount;
        private String text;
        private List<Link> links;
        
        @Data
        public static class Amount {
            private Integer value;
        }
    }
    
    @Data
    public static class Link {
        private String rel;
        private String href;
        private String media;
        private String type;
    }

	public static WebhookResponse fromJson(String payload) {
		try {
			return new ObjectMapper().readValue(payload, WebhookResponse.class);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to parse webhook payload", e);
		}
	}
}
