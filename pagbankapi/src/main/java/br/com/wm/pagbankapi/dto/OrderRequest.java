package br.com.wm.pagbankapi.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRequest {
    @JsonProperty("reference_id")
    @NotBlank
    private String referenceId;

    @Valid
    @NotNull
    private Customer customer;

    @Valid
    @NotEmpty
    private List<Item> items;

    @JsonProperty("qr_codes")
    @Valid
    private List<QrCode> qrCodes;

    @Valid
    private Shipping shipping;

    @JsonProperty("notification_urls")
    private List<String> notificationUrls;
    
    @Valid
    private List<Charge> charges;

    @Data
    public static class Customer {
        @NotBlank
        private String name;

        @Email
        @NotBlank
        private String email;

        @JsonProperty("tax_id")
        @NotBlank
        private String taxId;

        @Valid
        private List<Phone> phones;
    }

    @Data
    public static class Phone {
        @NotBlank
        private String country;

        @NotBlank
        private String area;

        @NotBlank
        private String number;

        @NotBlank
        private String type;
    }

    @Data
    public static class Item {
    	@JsonProperty("reference_id")
        private String referenceId;
    	
        @NotBlank
        private String name;

        @NotNull
        private Integer quantity;

        @JsonProperty("unit_amount")
        @NotNull
        private Integer unitAmount;
    }

    @Data
    public static class QrCode {
        @Valid
        @NotNull
        private Amount amount;

        @JsonProperty("expiration_date")
        @NotNull
        private String expirationDate; //Datetime 2023-02-08T15:15:11.881-03:00
    }

    @Data
    public static class Amount {
        @NotNull
        private Integer value;
        
        private String currency;
    }

    @Data
    public static class Shipping {
        @Valid
        @NotNull
        private Address address;
    }

    @Data
    public static class Address {
        @NotBlank
        private String street;

        @NotBlank
        private String number;

        private String complement;
        
        private String region;

        @NotBlank
        private String locality;

        @NotBlank
        private String city;

        @JsonProperty("region_code")
        @NotBlank
        private String regionCode;

        @NotBlank
        private String country;

        @JsonProperty("postal_code")
        @NotBlank
        private String postalCode;
    }
    
    @Data
    public static class Charge {
    	@JsonProperty("reference_id")
        private String referenceId;
        
        private String description;
        
        private Amount amount;
        
        @JsonProperty("payment_method")
        private PaymentMethod paymentMethod;
    }
    
    @Data
    public static class PaymentMethod {
        private String type;
        private Boleto boleto;
        private Integer installments;
        private Boolean capture;
        private Card card;
        private Holder holder;
    }
    
    @Data
    public static class Boleto {
        @JsonProperty("due_date")
        private String dueDate; //Date 2023-02-08 YYYY-MM-DD
        
        @JsonProperty("instruction_lines")
        private InstructionLines instructionLines;
        
        private Holder holder;
        
        @Data
        public static class InstructionLines {
            @JsonProperty("line_1")
            private String line1;
            
            @JsonProperty("line_2")
            private String line2;
        }
    }
    
    @Data
    public static class Card {
        private String encrypted;
        private Boolean store;
    }
    
    @Data
    public static class Holder {
        private String name;
        
        @JsonProperty("tax_id")
        private String taxId;
        
        private String email;
        
        private Address address;
    }
}