package br.com.wm.pagbankapi.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pagbank_order_id", unique = true)
    private String pagbankOrderId;

    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "total_amount")
    private Integer totalAmount;

    @Column(name = "qr_code_id")
    private String qrCodeId;

    @Column(name = "qr_code_text", length = 1000)
    private String qrCodeText;

    @Column(name = "qr_code_image_url")
    private String qrCodeImageUrl;
    
    @Column(name = "boleto_id")
    private String boletoId;
    
    @Column(name = "boleto_barcode")
    private String boletoBarcode;
    
    @Column(name = "boleto_formatted_barcode")
    private String boletoFormattedBarcode;
    
    @Column(name = "boleto_url")
    private String boletoUrl;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = OrderStatus.WAITING;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum OrderStatus {
        AUTHORIZED, PAID, IN_ANALYSIS, DECLINED, CANCELED, WAITING
    }
    
	public enum Type {
        PIX, BOLETO, CREDIT_CARD
	}
}
