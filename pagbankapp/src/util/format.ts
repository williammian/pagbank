export const formatCurrency = (value: number): string => {
  return new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL",
  }).format(value);
};

export const formatCardNumber = (value: string): string => {
  // Remove todos os caracteres não numéricos
  const cleaned = value.replace(/\D/g, "");
  // Adiciona espaços a cada 4 dígitos
  const formatted = cleaned.replace(/(\d{4})(?=\d)/g, "$1 ");
  return formatted;
};
