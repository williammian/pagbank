import React, { useState, useEffect, useRef } from 'react';
import { QrCode, CreditCard, User, MapPin, Phone, Mail, Package, Clock, CheckCircle, XCircle, FileText } from 'lucide-react';

import { FormData, CreditCardData } from '../model/FormData';
//import { OrderRequest } from '../api/model/OrderRequest';
import { OrderResponse, Link } from '../model/OrderResponse';
import { OrderStatus } from '../model/OrderStatus';
import { toLocalISOString } from '../util/date'
import { formatCardNumber, formatCurrency } from '../util/format';
import { API_URL, CHAVE_PUBLICA_PAGBANK } from '../constants'

type PaymentMethod = 'PIX' | 'BOLETO' | 'CREDIT_CARD';

const PagBankPayment: React.FC = () => {
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>('PIX');
  const [formData, setFormData] = useState<FormData>({
    customerName: 'José da Silva',
    customerEmail: 'jose@email.com',
    customerTaxId: '12345678909',
    customerPhone: '11999999999',
    itemName: 'Produto Teste',
    itemQuantity: 1,
    itemPrice: 29.90,
    street: 'Avenida Brigadeiro Faria Lima',
    number: '1384',
    complement: 'apto 12',
    locality: 'Pinheiros',
    city: 'São Paulo',
    regionCode: 'SP',
    postalCode: '01452002'
  });

  const [creditCardData, setCreditCardData] = useState<CreditCardData>({
    number: '',
    holderName: '',
    expiryMonth: '',
    expiryYear: '',
    cvv: '',
    installments: 1
  });

  const [orderResponse, setOrderResponse] = useState<OrderResponse | null>(null);
  const orderResponseRef = useRef<OrderResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [orderStatus, setOrderStatus] = useState<OrderStatus>('WAITING');

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    const { name, value, type } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'number' ? parseFloat(value) || 0 : value
    }));
  };

  const handleCreditCardChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>): void => {
    const { name, value, type } = e.target;
    setCreditCardData(prev => ({
      ...prev,
      [name]: type === 'number' ? parseInt(value) || 1 : value
    }));
  };

  const handleCardNumberChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    const { value } = e.target;
    const formatted = formatCardNumber(value);
    setCreditCardData(prev => ({
      ...prev,
      number: formatted
    }));
  };

  useEffect(() => {
    orderResponseRef.current = orderResponse;
  }, [orderResponse]);

  const [sdkReady, setSdkReady] = useState(false);

  // Carrega o SDK do PagBank quando o componente é montado
  useEffect(() => {
    const script = document.createElement("script");
    script.src = "https://assets.pagseguro.com.br/checkout-sdk-js/rc/dist/browser/pagseguro.min.js";
    script.async = true;
    script.onload = () => {
      setSdkReady(true); // Marca como carregado
    };
    document.body.appendChild(script);

    return () => {
      document.body.removeChild(script);
    };
  }, []);
  
  const encryptCardData = (cardData: CreditCardData): string => {
    if (!sdkReady || !window.PagSeguro) {
      console.error("SDK ainda não carregada");
      return "";
    }

    const card = window.PagSeguro.encryptCard({
      publicKey: CHAVE_PUBLICA_PAGBANK,
      holder: cardData.holderName,
      number: cardData.number.replace(/\s/g, ''),
      expMonth: cardData.expiryMonth,
      expYear: cardData.expiryYear,
      securityCode: cardData.cvv
    });

    if (card.hasErrors) {
      console.error("Erros ao criptografar o cartão:", card.errors);
    } else {
      console.log("Cartão criptografado com sucesso:", card.encryptedCard);
    }

    return card.encryptedCard;
  };

  const createOrder = async (): Promise<void> => {
    setLoading(true);
    setError(null);

    // Generate reference ID
    const referenceId: string = `REF-${Date.now()}`;

    // Alterar meusite.com para o seu domínio (não use localhost nem para teste)
    const notificationUrl: string = `https://meusite.com/api/payments/notifications/${referenceId}`;

    // Calculate amount in cents
    const amountInCents: number = Math.round(formData.itemPrice * 100);
    
    let orderData: any;

    if (paymentMethod === 'PIX') {
      // Create expiration date (1 hour from now)
      const expirationDate: Date = new Date();
      expirationDate.setHours(expirationDate.getHours() + 1);

      orderData = {
        reference_id: referenceId,
        customer: {
          name: formData.customerName,
          email: formData.customerEmail,
          tax_id: formData.customerTaxId,
          phones: [{
            country: "55",
            area: formData.customerPhone.substring(0, 2),
            number: formData.customerPhone.substring(2),
            type: "MOBILE"
          }]
        },
        items: [{
          reference_id: `item-${referenceId}`,
          name: formData.itemName,
          quantity: formData.itemQuantity,
          unit_amount: amountInCents
        }],
        qr_codes: [{
          amount: {
            value: amountInCents * formData.itemQuantity
          },
          expiration_date: toLocalISOString(expirationDate)
        }],
        shipping: {
          address: {
            street: formData.street,
            number: formData.number,
            complement: formData.complement,
            locality: formData.locality,
            city: formData.city,
            region_code: formData.regionCode,
            country: "BRA",
            postal_code: formData.postalCode
          }
        },
        notification_urls: [
          notificationUrl
        ]
      };
    } else if (paymentMethod === 'BOLETO') {
      // BOLETO payment
      // Create due date (7 days from now)
      const dueDate: Date = new Date();
      dueDate.setDate(dueDate.getDate() + 7);
      const dueDateString = dueDate.toISOString().split('T')[0]; // Format: YYYY-MM-DD

      orderData = {
        reference_id: referenceId,
        customer: {
          name: formData.customerName,
          email: formData.customerEmail,
          tax_id: formData.customerTaxId,
          phones: [{
            country: "55",
            area: formData.customerPhone.substring(0, 2),
            number: formData.customerPhone.substring(2),
            type: "MOBILE"
          }]
        },
        items: [{
          reference_id: `item-${referenceId}`,
          name: formData.itemName,
          quantity: formData.itemQuantity,
          unit_amount: amountInCents
        }],
        shipping: {
          address: {
            street: formData.street,
            number: formData.number,
            complement: formData.complement,
            locality: formData.locality,
            city: formData.city,
            region_code: formData.regionCode,
            country: "BRA",
            postal_code: formData.postalCode
          }
        },
        notification_urls: [
          notificationUrl
        ],
        charges: [{
          reference_id: `charge-${referenceId}`,
          description: `Pagamento de ${formData.itemName}`,
          amount: {
            value: amountInCents * formData.itemQuantity,
            currency: "BRL"
          },
          payment_method: {
            type: "BOLETO",
            boleto: {
              due_date: dueDateString,
              instruction_lines: {
                line_1: "Pagamento processado para DESC Fatura",
                line_2: "Via PagSeguro"
              },
              holder: {
                name: formData.customerName,
                tax_id: formData.customerTaxId,
                email: formData.customerEmail,
                address: {
                  country: "Brasil",
                  region: formData.city,
                  region_code: formData.regionCode,
                  city: formData.city,
                  postal_code: formData.postalCode,
                  street: formData.street,
                  number: formData.number,
                  locality: formData.locality
                }
              }
            }
          }
        }]
      };
    } else if (paymentMethod === 'CREDIT_CARD') {
      // CREDIT CARD payment
      orderData = {
        reference_id: referenceId,
        customer: {
          name: formData.customerName,
          email: formData.customerEmail,
          tax_id: formData.customerTaxId,
          phones: [{
            country: "55",
            area: formData.customerPhone.substring(0, 2),
            number: formData.customerPhone.substring(2),
            type: "MOBILE"
          }]
        },
        items: [{
          reference_id: `item-${referenceId}`,
          name: formData.itemName,
          quantity: formData.itemQuantity,
          unit_amount: amountInCents
        }],
        shipping: {
          address: {
            street: formData.street,
            number: formData.number,
            complement: formData.complement,
            locality: formData.locality,
            city: formData.city,
            region_code: formData.regionCode,
            country: "BRA",
            postal_code: formData.postalCode
          }
        },
        notification_urls: [
          notificationUrl
        ],
        charges: [{
          reference_id: `charge-${referenceId}`,
          description: `Pagamento de ${formData.itemName}`,
          amount: {
            value: amountInCents * formData.itemQuantity,
            currency: "BRL"
          },
          payment_method: {
            type: "CREDIT_CARD",
            installments: creditCardData.installments,
            capture: true,
            card: {
              encrypted: encryptCardData(creditCardData),
              store: false
            },
            holder: {
              name: creditCardData.holderName,
              tax_id: formData.customerTaxId
            }
          }
        }]
      };
    }

    try {
      const response: Response = await fetch(`${API_URL}/api/payments/create-order`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(orderData)
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const result: OrderResponse = await response.json();
      setOrderResponse(result);
      
      // Start polling for payment status
      if (paymentMethod !== 'BOLETO') {
        startPaymentStatusPolling(result.reference_id);
      }

    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Erro desconhecido';
      setError('Erro ao criar pedido: ' + errorMessage);
      console.error('Error:', err);
    } finally {
      setLoading(false);
    }
  };

  const startPaymentStatusPolling = (referenceId: string): void => {
    let pollCount = 0;
    const maxPolls = 72; // Máximo 72 tentativas (6 minutos se polling a cada 5s)

    const pollStatus = async (): Promise<void> => {
      try {
        if (pollCount >= maxPolls || !orderResponseRef.current) {
          console.log('Polling limit reached, stopping further requests');
          return;
        }

        pollCount++;
        
        const response = await fetch(`${API_URL}/api/payments/order/status/${referenceId}`);
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const status = await response.text();
        console.log(`Payment status (attempt ${pollCount}):`, status);
        
        setOrderStatus(status as OrderStatus);
        
        // Se o status mudou ou atingiu o limite, para o polling
        if (status !== 'WAITING' || pollCount >= maxPolls || !orderResponseRef.current) {
          if (pollCount >= maxPolls && status === 'WAITING') {
            console.log('Polling timeout reached');
          }
          return;
        }
        
        // Continue polling
        setTimeout(pollStatus, 5000);
        
      } catch (error) {
        console.error('Erro ao buscar status do pagamento:', error);
        
        // Retry em caso de erro, mas com limite
        if (pollCount < maxPolls) {
          setTimeout(pollStatus, 5000);
        }
      }
    };
    
    // Delay inicial de 1 segundo antes de começar o polling
    setTimeout(pollStatus, 1000);
  };

  const copyPixCode = async (): Promise<void> => {
    if (orderResponse?.qr_codes?.[0]?.text) {
      try {
        await navigator.clipboard.writeText(orderResponse.qr_codes[0].text);
        alert('Código PIX copiado para a área de transferência!');
      } catch (err) {
        console.error('Erro ao copiar código PIX:', err);
        alert('Erro ao copiar código PIX');
      }
    }
  };

  const copyBoletoCode = async (): Promise<void> => {
    const boletoCode = orderResponse?.charges?.[0]?.payment_method?.boleto?.barcode;
    if (boletoCode) {
      try {
        await navigator.clipboard.writeText(boletoCode);
        alert('Código de barras do boleto copiado para a área de transferência!');
      } catch (err) {
        console.error('Erro ao copiar código do boleto:', err);
        alert('Erro ao copiar código do boleto');
      }
    }
  };

  const getStatusIcon = (): React.JSX.Element => {
    switch (orderStatus) {
      case 'PAID':
        return <CheckCircle className="w-6 h-6 text-green-500" />;
      case 'CANCELED':
      case 'DECLINED':
        return <XCircle className="w-6 h-6 text-red-500" />;
      default:
        return <Clock className="w-6 h-6 text-yellow-500" />;
    }
  };

  const getStatusText = (): string => {
    switch (orderStatus) {
      case 'PAID':
        return 'Pagamento Confirmado!';
      case 'CANCELED':
        return 'Pagamento Cancelado!';
      case 'DECLINED':
        return 'Pagamento Recusado!';
      default:
        return 'Aguardando Pagamento...';
    }
  };

  const resetForm = (): void => {
    setOrderResponse(null);
    setOrderStatus('WAITING');
    setError(null);
  };

  const validateCreditCardForm = (): boolean => {
    if (paymentMethod !== 'CREDIT_CARD') return true;
    
    const { number, holderName, expiryMonth, expiryYear, cvv } = creditCardData;
    
    if (!number || number.replace(/\s/g, '').length < 13) {
      setError('Número do cartão inválido');
      return false;
    }
    
    if (!holderName.trim()) {
      setError('Nome do portador é obrigatório');
      return false;
    }
    
    if (!expiryMonth || !expiryYear) {
      setError('Data de validade é obrigatória');
      return false;
    }
    
    if (!cvv || cvv.length < 3) {
      setError('CVV inválido');
      return false;
    }
    
    return true;
  };

  const handleSubmit = (): void => {
    if (!validateCreditCardForm()) {
      return;
    }
    createOrder();
  };

  if (orderResponse) {
    const qrCodePngLink = orderResponse.qr_codes?.[0]?.links?.find(
      (link: Link) => link.rel === 'QRCODE.PNG'
    );

    const boletoPdfLink = orderResponse.charges?.[0]?.links?.find(
      (link: Link) => link.media === 'application/pdf'
    );

    const boletoData = orderResponse.charges?.[0]?.payment_method?.boleto;

    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 p-4">
        <div className="max-w-2xl mx-auto">
          <div className="bg-white rounded-2xl shadow-xl p-8">
            <div className="text-center mb-8">
              <div className="flex items-center justify-center gap-3 mb-4">
                {getStatusIcon()}
                <h1 className="text-3xl font-bold text-gray-800">
                  {getStatusText()}
                </h1>
              </div>
              <p className="text-gray-600">
                Pedido: {orderResponse.reference_id}
              </p>
            </div>

            {orderStatus === 'WAITING' && paymentMethod === 'PIX' && (
              <div className="bg-blue-50 border-2 border-blue-200 rounded-xl p-6 mb-6">
                <div className="text-center mb-6">
                  <QrCode className="w-16 h-16 mx-auto text-blue-600 mb-4" />
                  <h2 className="text-xl font-semibold text-gray-800 mb-2">
                    Escaneie o QR Code ou copie o código PIX
                  </h2>
                  <p className="text-gray-600">
                    Use o app do seu banco para pagar
                  </p>
                </div>

                {/* QR Code Image */}
                {qrCodePngLink && (
                  <div className="text-center mb-6">
                    <img 
                      src={qrCodePngLink.href}
                      alt="QR Code PIX"
                      className="mx-auto border-4 border-white shadow-lg rounded-lg"
                      style={{ maxWidth: '250px' }}
                    />
                  </div>
                )}

                {/* PIX Code */}
                <div className="bg-white border-2 border-dashed border-blue-300 rounded-lg p-4 mb-4">
                  <p className="text-sm text-gray-600 mb-2">Código PIX:</p>
                  <p className="font-mono text-sm bg-gray-50 p-2 rounded break-all">
                    {orderResponse.qr_codes?.[0]?.text}
                  </p>
                </div>

                <button
                  onClick={copyPixCode}
                  className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 px-6 rounded-lg transition duration-200 flex items-center justify-center gap-2"
                >
                  <CreditCard className="w-5 h-5" />
                  Copiar Código PIX
                </button>
              </div>
            )}

            {orderStatus === 'WAITING' && paymentMethod === 'BOLETO' && boletoData && (
              <div className="bg-orange-50 border-2 border-orange-200 rounded-xl p-6 mb-6">
                <div className="text-center mb-6">
                  <FileText className="w-16 h-16 mx-auto text-orange-600 mb-4" />
                  <h2 className="text-xl font-semibold text-gray-800 mb-2">
                    Boleto Bancário Gerado
                  </h2>
                  <p className="text-gray-600">
                    Pague até {new Date(boletoData.due_date).toLocaleDateString('pt-BR')}
                  </p>
                </div>

                {/* Boleto Details */}
                <div className="bg-white border-2 border-dashed border-orange-300 rounded-lg p-4 mb-4">
                  <p className="text-sm text-gray-600 mb-2">Código de Barras:</p>
                  <p className="font-mono text-sm bg-gray-50 p-2 rounded break-all">
                    {boletoData.barcode}
                  </p>
                </div>

                <div className="bg-white border-2 border-dashed border-orange-300 rounded-lg p-4 mb-4">
                  <p className="text-sm text-gray-600 mb-2">Linha Digitável:</p>
                  <p className="font-mono text-sm bg-gray-50 p-2 rounded break-all">
                    {boletoData.formatted_barcode}
                  </p>
                </div>

                <div className="space-y-3">
                  <button
                    onClick={copyBoletoCode}
                    className="w-full bg-orange-600 hover:bg-orange-700 text-white font-semibold py-3 px-6 rounded-lg transition duration-200 flex items-center justify-center gap-2"
                  >
                    <FileText className="w-5 h-5" />
                    Copiar Código de Barras
                  </button>

                  {boletoPdfLink && (
                    <a
                      href={boletoPdfLink.href}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="w-full bg-gray-600 hover:bg-gray-700 text-white font-semibold py-3 px-6 rounded-lg transition duration-200 flex items-center justify-center gap-2"
                    >
                      <FileText className="w-5 h-5" />
                      Baixar Boleto PDF
                    </a>
                  )}
                </div>
              </div>
            )}

            {orderStatus === 'WAITING' && paymentMethod === 'CREDIT_CARD' && (
              <div className="bg-green-50 border-2 border-green-200 rounded-xl p-6 mb-6">
                <div className="text-center mb-6">
                  <CreditCard className="w-16 h-16 mx-auto text-green-600 mb-4" />
                  <h2 className="text-xl font-semibold text-gray-800 mb-2">
                    Processando Pagamento
                  </h2>
                  <p className="text-gray-600">
                    Aguarde a confirmação do pagamento com cartão de crédito
                  </p>
                </div>
              </div>
            )}

            {orderStatus === 'PAID' && (
              <div className="bg-green-50 border-2 border-green-200 rounded-xl p-6 text-center">
                <CheckCircle className="w-20 h-20 mx-auto text-green-500 mb-4" />
                <h3 className="text-xl font-semibold text-green-800 mb-2">
                  Pagamento Aprovado!
                </h3>
                <p className="text-green-600">
                  Seu pedido foi processado com sucesso.
                </p>
              </div>
            )}

            {(orderStatus === 'CANCELED' || orderStatus === 'DECLINED') && (
              <div className="bg-red-50 border-2 border-red-200 rounded-xl p-6 text-center">
                <XCircle className="w-20 h-20 mx-auto text-red-500 mb-4" />
                <h3 className="text-xl font-semibold text-red-800 mb-2">
                  {orderStatus === 'CANCELED' ? 'Pagamento Cancelado' : 'Pagamento Recusado'}
                </h3>
                <p className="text-red-600">
                  {orderStatus === 'CANCELED' 
                    ? 'O pagamento foi cancelado.' 
                    : 'O pagamento foi recusado. Verifique os dados do cartão e tente novamente.'
                  }
                </p>
              </div>
            )}

            {/* Order Details */}
            <div className="bg-gray-50 rounded-xl p-6 mt-6">
              <h3 className="text-lg font-semibold text-gray-800 mb-4">Detalhes do Pedido</h3>
              
              <div className="space-y-3">
                <div className="flex justify-between">
                  <span className="text-gray-600">Produto:</span>
                  <span className="font-medium">{formData.itemName}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Quantidade:</span>
                  <span className="font-medium">{formData.itemQuantity}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Valor unitário:</span>
                  <span className="font-medium">{formatCurrency(formData.itemPrice)}</span>
                </div>
                <div className="flex justify-between border-t pt-3">
                  <span className="text-lg font-semibold text-gray-800">Total:</span>
                  <span className="text-lg font-bold text-blue-600">
                    {formatCurrency(formData.itemPrice * formData.itemQuantity)}
                  </span>
                </div>
                {paymentMethod === 'CREDIT_CARD' && creditCardData.installments > 1 && (
                  <div className="flex justify-between">
                    <span className="text-gray-600">Parcelas:</span>
                    <span className="font-medium">
                      {creditCardData.installments}x de {formatCurrency((formData.itemPrice * formData.itemQuantity) / creditCardData.installments)}
                    </span>
                  </div>
                )}
              </div>
            </div>

            <button
              onClick={resetForm}
              className="w-full mt-6 bg-gray-600 hover:bg-gray-700 text-white font-semibold py-3 px-6 rounded-lg transition duration-200"
            >
              Fazer Novo Pedido
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 p-4">
      <div className="max-w-4xl mx-auto">
        <div className="bg-white rounded-2xl shadow-xl p-8">
          <div className="text-center mb-8">
            <h1 className="text-3xl font-bold text-gray-800 mb-2">
              Pagamento PagBank
            </h1>
            <p className="text-gray-600">
              Escolha sua forma de pagamento preferida
            </p>
          </div>

          {/* Payment Method Selection */}
          <div className="mb-8">
            <h2 className="text-xl font-semibold text-gray-800 mb-4">Forma de Pagamento</h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <button
                onClick={() => setPaymentMethod('PIX')}
                className={`p-4 rounded-xl border-2 transition duration-200 ${
                  paymentMethod === 'PIX'
                    ? 'border-blue-500 bg-blue-50 text-blue-700'
                    : 'border-gray-200 hover:border-gray-300'
                }`}
              >
                <QrCode className="w-8 h-8 mx-auto mb-2" />
                <div className="font-semibold">PIX</div>
                <div className="text-sm text-gray-600">Pagamento instantâneo</div>
              </button>

              <button
                onClick={() => setPaymentMethod('BOLETO')}
                className={`p-4 rounded-xl border-2 transition duration-200 ${
                  paymentMethod === 'BOLETO'
                    ? 'border-orange-500 bg-orange-50 text-orange-700'
                    : 'border-gray-200 hover:border-gray-300'
                }`}
              >
                <FileText className="w-8 h-8 mx-auto mb-2" />
                <div className="font-semibold">Boleto</div>
                <div className="text-sm text-gray-600">Vencimento em 7 dias</div>
              </button>

              <button
                onClick={() => setPaymentMethod('CREDIT_CARD')}
                className={`p-4 rounded-xl border-2 transition duration-200 ${
                  paymentMethod === 'CREDIT_CARD'
                    ? 'border-green-500 bg-green-50 text-green-700'
                    : 'border-gray-200 hover:border-gray-300'
                }`}
              >
                <CreditCard className="w-8 h-8 mx-auto mb-2" />
                <div className="font-semibold">Cartão de Crédito</div>
                <div className="text-sm text-gray-600">Parcelamento disponível</div>
              </button>
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* Customer Information */}
            <div className="space-y-6">
              <div>
                <h2 className="text-xl font-semibold text-gray-800 mb-4 flex items-center gap-2">
                  <User className="w-5 h-5" />
                  Dados do Cliente
                </h2>
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Nome Completo
                    </label>
                    <input
                      type="text"
                      name="customerName"
                      value={formData.customerName}
                      onChange={handleInputChange}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      <Mail className="w-4 h-4 inline mr-1" />
                      Email
                    </label>
                    <input
                      type="email"
                      name="customerEmail"
                      value={formData.customerEmail}
                      onChange={handleInputChange}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      CPF
                    </label>
                    <input
                      type="text"
                      name="customerTaxId"
                      value={formData.customerTaxId}
                      onChange={handleInputChange}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      <Phone className="w-4 h-4 inline mr-1" />
                      Telefone
                    </label>
                    <input
                      type="text"
                      name="customerPhone"
                      value={formData.customerPhone}
                      onChange={handleInputChange}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      required
                    />
                  </div>
                </div>
              </div>

              {/* Product Information */}
              <div>
                <h2 className="text-xl font-semibold text-gray-800 mb-4 flex items-center gap-2">
                  <Package className="w-5 h-5" />
                  Produto
                </h2>
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Nome do Produto
                    </label>
                    <input
                      type="text"
                      name="itemName"
                      value={formData.itemName}
                      onChange={handleInputChange}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      required
                    />
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Quantidade
                      </label>
                      <input
                        type="number"
                        name="itemQuantity"
                        value={formData.itemQuantity}
                        onChange={handleInputChange}
                        min="1"
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        required
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Preço Unitário (R$)
                      </label>
                      <input
                        type="number"
                        name="itemPrice"
                        value={formData.itemPrice}
                        onChange={handleInputChange}
                        min="0"
                        step="0.01"
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        required
                      />
                    </div>
                  </div>

                  <div className="bg-gray-50 p-4 rounded-lg">
                    <div className="flex justify-between items-center">
                      <span className="text-lg font-semibold text-gray-800">Total:</span>
                      <span className="text-xl font-bold text-blue-600">
                        {formatCurrency(formData.itemPrice * formData.itemQuantity)}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {/* Address and Credit Card Information */}
            <div className="space-y-6">
              {/* Address Information */}
              <div>
                <h2 className="text-xl font-semibold text-gray-800 mb-4 flex items-center gap-2">
                  <MapPin className="w-5 h-5" />
                  Endereço de Entrega
                </h2>
                <div className="space-y-4">
                  <div className="grid grid-cols-3 gap-4">
                    <div className="col-span-2">
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Rua
                      </label>
                      <input
                        type="text"
                        name="street"
                        value={formData.street}
                        onChange={handleInputChange}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        required
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Número
                      </label>
                      <input
                        type="text"
                        name="number"
                        value={formData.number}
                        onChange={handleInputChange}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        required
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Complemento
                    </label>
                    <input
                      type="text"
                      name="complement"
                      value={formData.complement}
                      onChange={handleInputChange}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Bairro
                      </label>
                      <input
                        type="text"
                        name="locality"
                        value={formData.locality}
                        onChange={handleInputChange}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        required
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        CEP
                      </label>
                      <input
                        type="text"
                        name="postalCode"
                        value={formData.postalCode}
                        onChange={handleInputChange}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        required
                      />
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Cidade
                      </label>
                      <input
                        type="text"
                        name="city"
                        value={formData.city}
                        onChange={handleInputChange}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        required
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Estado
                      </label>
                      <input
                        type="text"
                        name="regionCode"
                        value={formData.regionCode}
                        onChange={handleInputChange}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        required
                      />
                    </div>
                  </div>
                </div>
              </div>

              {/* Credit Card Information */}
              {paymentMethod === 'CREDIT_CARD' && (
                <div>
                  <h2 className="text-xl font-semibold text-gray-800 mb-4 flex items-center gap-2">
                    <CreditCard className="w-5 h-5" />
                    Dados do Cartão de Crédito
                  </h2>
                  <div className="space-y-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Número do Cartão
                      </label>
                      <input
                        type="text"
                        name="number"
                        value={creditCardData.number}
                        onChange={handleCardNumberChange}
                        placeholder="0000 0000 0000 0000"
                        maxLength={19}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                        required
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Nome do Portador
                      </label>
                      <input
                        type="text"
                        name="holderName"
                        value={creditCardData.holderName}
                        onChange={handleCreditCardChange}
                        placeholder="Nome como está no cartão"
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                        required
                      />
                    </div>

                    <div className="grid grid-cols-3 gap-4">
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                          Mês
                        </label>
                        <select
                          name="expiryMonth"
                          value={creditCardData.expiryMonth}
                          onChange={handleCreditCardChange}
                          className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                          required
                        >
                          <option value="">Mês</option>
                          {Array.from({ length: 12 }, (_, i) => (
                            <option key={i + 1} value={String(i + 1).padStart(2, '0')}>
                              {String(i + 1).padStart(2, '0')}
                            </option>
                          ))}
                        </select>
                      </div>

                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                          Ano
                        </label>
                        <select
                          name="expiryYear"
                          value={creditCardData.expiryYear}
                          onChange={handleCreditCardChange}
                          className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                          required
                        >
                          <option value="">Ano</option>
                          {Array.from({ length: 10 }, (_, i) => {
                            const year = new Date().getFullYear() + i;
                            return (
                              <option key={year} value={year}>
                                {year}
                              </option>
                            );
                          })}
                        </select>
                      </div>

                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                          CVV
                        </label>
                        <input
                          type="text"
                          name="cvv"
                          value={creditCardData.cvv}
                          onChange={handleCreditCardChange}
                          placeholder="000"
                          maxLength={4}
                          className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                          required
                        />
                      </div>
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Parcelas
                      </label>
                      <select
                        name="installments"
                        value={creditCardData.installments}
                        onChange={handleCreditCardChange}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                      >
                        {Array.from({ length: 12 }, (_, i) => {
                          const installment = i + 1;
                          const installmentValue = (formData.itemPrice * formData.itemQuantity) / installment;
                          return (
                            <option key={installment} value={installment}>
                              {installment}x de {formatCurrency(installmentValue)}
                              {installment === 1 ? ' à vista' : ''}
                            </option>
                          );
                        })}
                      </select>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Error Message */}
          {error && (
            <div className="mt-6 bg-red-50 border border-red-200 rounded-lg p-4">
              <div className="flex items-center">
                <XCircle className="w-5 h-5 text-red-500 mr-2" />
                <span className="text-red-700">{error}</span>
              </div>
            </div>
          )}

          {/* Submit Button */}
          <div className="mt-8">
            <button
              onClick={handleSubmit}
              disabled={loading}
              className={`w-full py-4 px-6 rounded-lg font-semibold text-lg transition duration-200 ${
                loading
                  ? 'bg-gray-400 cursor-not-allowed'
                  : paymentMethod === 'PIX'
                  ? 'bg-blue-600 hover:bg-blue-700 text-white'
                  : paymentMethod === 'BOLETO'
                  ? 'bg-orange-600 hover:bg-orange-700 text-white'
                  : 'bg-green-600 hover:bg-green-700 text-white'
              }`}
            >
              {loading ? (
                <div className="flex items-center justify-center">
                  <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-white mr-2"></div>
                  Processando...
                </div>
              ) : (
                `Pagar com ${
                  paymentMethod === 'PIX' ? 'PIX' : 
                  paymentMethod === 'BOLETO' ? 'Boleto' : 
                  'Cartão de Crédito'
                }`
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PagBankPayment;

