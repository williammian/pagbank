export interface OrderResponse {
  id?: string;
  reference_id: string; // ISO 8601 datetime format: 2023-02-08T15:15:11.881-03:00
  created_at: string; // ISO 8601 datetime format: 2023-02-08T15:15:11.881-03:00
  customer?: Customer;
  items?: Item[];
  amount?: Amount;
  shipping?: Shipping;
  qr_codes?: QrCodeResponse[];
  charges?: Charge[];
  notification_urls?: string[];
  links?: Link[];
}

export interface Customer {
  name?: string;
  email?: string;
  tax_id?: string;
  phones?: Phone[];
}

export interface Phone {
  country?: string;
  area?: string;
  number?: string;
  type?: string;
}

export interface Item {
  reference_id?: string;
  name?: string;
  quantity?: string;
  unit_amount?: string;
}

export interface Amount {
  value?: number;
  currency?: string;
  additional?: number;
  discount?: number;
  summary?: Summary;
}

export interface Summary {
  total?: number;
  paid?: number;
  refunded?: number;
}

export interface Shipping {
  address?: Address;
}

export interface Address {
  street?: string;
  number?: string;
  complement?: string;
  locality?: string;
  city?: string;
  region_code?: string;
  country?: string;
  postal_code?: string;
}

export interface QrCodeResponse {
  id?: string;
  expiration_date?: string; // ISO 8601 datetime format: 2023-02-08T15:15:11.881-03:00
  amount?: Amount;
  text?: string;
  links?: Link[];
}

export interface Link {
  rel?: string;
  href?: string;
  media?: string;
  type?: string;
}

export interface Charge {
  id?: string;
  reference_id?: string;
  status?: string;
  created_at?: string; // ISO 8601 datetime format: 2023-02-08T15:15:11.881-03:00
  paid_at?: string; // ISO 8601 datetime format: 2023-02-08T15:15:11.881-03:00
  description?: string;
  amount?: Amount;
  payment_response?: PaymentResponse;
  payment_method?: PaymentMethod;
  links?: Link[];
}

export interface PaymentResponse {
  code?: string;
  message?: string;
  reference?: string;
}

export interface PaymentMethod {
  type?: string;
  installments?: number;
  capture?: boolean;
  card?: Card;
  boleto?: Boleto;
  soft_descriptor?: string;
}

export interface Card {
  brand?: string;
  first_digits?: string;
  last_digits?: string;
  exp_month?: string;
  exp_year?: string;
  holder?: CardHolder;
  store?: boolean;
}

export interface CardHolder {
  name?: string;
  tax_id?: string;
}

export interface Boleto {
  id?: string;
  barcode?: string;
  formatted_barcode?: string;
  due_date: string;
  instruction_lines?: InstructionLines;
  holder?: BoletoHolder;
}

export interface InstructionLines {
  line_1?: string;
  line_2?: string;
}

export interface BoletoHolder {
  name?: string;
  tax_id?: string;
  email?: string;
  address?: Address;
}