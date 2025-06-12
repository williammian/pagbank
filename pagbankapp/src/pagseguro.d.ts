// src/pagseguro.d.ts
interface EncryptedCardResult {
  encryptedCard: string;
  hasErrors: boolean;
  errors: string[];
}

interface PagSeguroSDK {
  encryptCard(data: {
    publicKey: string;
    holder: string;
    number: string;
    expMonth: string;
    expYear: string;
    securityCode: string;
  }): EncryptedCardResult;
}

interface Window {
  PagSeguro: PagSeguroSDK;
}
