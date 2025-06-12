export interface FormData {
  customerName: string;
  customerEmail: string;
  customerTaxId: string;
  customerPhone: string;
  itemName: string;
  itemQuantity: number;
  itemPrice: number;
  street: string;
  number: string;
  complement: string;
  locality: string;
  city: string;
  regionCode: string;
  postalCode: string;
}

export interface CreditCardData {
  number: string;
  holderName: string;
  expiryMonth: string;
  expiryYear: string;
  cvv: string;
  installments: number;
}