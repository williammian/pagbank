export interface OrderRequest {
  reference_id: string;
  customer: Customer;
  items: Item[];
  qr_codes?: QrCode[];
  shipping?: Shipping;
  notification_urls?: string[];
  charges?: Charge[];
}

export interface Customer {
  name: string;
  email: string;
  tax_id: string;
  phones?: Phone[];
}

export interface Phone {
  country: string;
  area: string;
  number: string;
  type: string;
}

export interface Item {
  reference_id?: string;
  name: string;
  quantity: number;
  unit_amount: number;
}

export interface QrCode {
  amount: Amount;
  expiration_date: string; // ISO 8601 datetime format: 2023-02-08T15:15:11.881-03:00
}

export interface Amount {
  value: number;
  currency?: string;
}

export interface Shipping {
  address: Address;
}

export interface Address {
  street: string;
  number: string;
  complement?: string;
  region?: string;
  locality: string;
  city: string;
  region_code: string;
  country: string;
  postal_code: string;
}

export interface Charge {
  reference_id?: string;
  description?: string;
  amount?: Amount;
}

export interface PaymentMethod {
  type?: string;
  boleto?: Boleto;
  installments?: number;
  capture?: boolean;
  card?: Card;
  holder?: Holder;
}

export interface Boleto {
  due_date?: string; // ISO date format: YYYY-MM-DD
  instruction_lines?: InstructionLines;
  holder?: Holder;
}

export interface InstructionLines {
  line_1?: string;
  line_2?: string;
}

export interface Card {
  encrypted?: string;
  store?: boolean;
}

export interface Holder {
  name?: string;
  tax_id?: string;
  email?: string;
  address?: Address;
}