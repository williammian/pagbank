# Integração com API PagBank

Este projeto visa demonstrar a integração com a API PagBank para processamento de pedidos e pagamentos, abrangendo diferentes métodos como PIX, Boleto e Cartão de Crédito. Desenvolvido com tecnologias modernas no backend e frontend, oferece uma solução completa para gerenciar transações financeiras através da plataforma PagBank.



## Tecnologias Utilizadas

Este projeto é composto por duas partes principais: um backend desenvolvido em Java com Spring Boot e um frontend em React com TypeScript. Abaixo estão as principais tecnologias utilizadas:

### Backend (pagbankapi)

*   **Java**: Linguagem de programação principal.
*   **Spring Boot**: Framework para construção de aplicações Java robustas e escaláveis.
*   **Spring WebFlux**: Framework reativo para construção de APIs não bloqueantes.
*   **Spring Data JPA**: Para persistência de dados com o banco de dados H2.
*   **RestTemplate**: Para consumo da API PagBank.
*   **H2 Database**: Banco de dados em memória para desenvolvimento e testes.

### Frontend (pagbankapp)

*   **React**: Biblioteca JavaScript para construção de interfaces de usuário.
*   **TypeScript**: Superset do JavaScript que adiciona tipagem estática.
*   **HTML/CSS**: Para a estrutura e estilização da aplicação web.



## Funcionalidades

O projeto oferece as seguintes funcionalidades de integração com a API PagBank:

*   **Criação de Pedidos**: Permite a criação de pedidos com diferentes formas de pagamento.
*   **Pagamento PIX**: Geração de QR Code para pagamentos via PIX.
*   **Pagamento Boleto**: Geração e processamento de pagamentos via Boleto.
*   **Pagamento Cartão de Crédito**: Processamento de pagamentos via Cartão de Crédito, incluindo a criptografia dos dados do cartão.



## Casos de Uso Implementados

Os seguintes casos de uso foram implementados e testados neste projeto:

1.  **Criar pedido com QR Code (PIX)**: Integração para geração de QR Code para pagamentos via PIX. Mais detalhes podem ser encontrados na documentação oficial do PagBank: [Criar pedido com QR Code](https://developer.pagbank.com.br/reference/criar-pedido-pedido-com-qr-code)
2.  **Criar e pagar pedido com Boleto**: Funcionalidade para criar e processar pagamentos via Boleto. Para simulação, utilize o [Simulador PIX e Boleto](https://developer.pagbank.com.br/docs/simulador).
3.  **Criar e pagar um pedido com Cartão**: Implementação para processar pagamentos com Cartão de Crédito. É necessário criptografar os dados do cartão antes do envio. Consulte a documentação sobre [Cartões de teste](https://developer.pagbank.com.br/docs/cartoes-de-teste) e [Chaves públicas](https://developer.pagbank.com.br/docs/chaves-publicas).



## Configuração do Ambiente (PagBank Sandbox)

Para utilizar o ambiente de testes (sandbox) do PagBank, é necessário configurar o arquivo `application.properties` no módulo `pagbankapi` com as seguintes propriedades:

```properties
pagbank.api.url=https://sandbox.api.pagseguro.com
pagbank.api.token=SEU_TOKEN_PAGBANK_AQUI
```

O `SEU_TOKEN_PAGBANK_AQUI` é uma String obtida a partir do cadastro no PagBank para autenticação na API.



## Instalação e Execução (Backend - `pagbankapi`)

Para configurar e executar o backend da aplicação (`pagbankapi`), siga os passos abaixo:

1.  **Clone o repositório:**
    ```bash
    git clone https://github.com/williammian/pagbank.git
    cd pagbank/pagbankapi
    ```
2.  **Configure o `application.properties`:**
    Edite o arquivo `src/main/resources/application.properties` e insira seu token do PagBank conforme descrito na seção [Configuração do Ambiente (PagBank Sandbox)](#configuracao-do-ambiente-pagbank-sandbox).
3.  **Compile e execute a aplicação:**
    Você pode usar o Maven ou sua IDE (IntelliJ IDEA, Eclipse, etc.) para executar o projeto. Via Maven, execute o seguinte comando no diretório `pagbankapi`:
    ```bash
    ./mvnw spring-boot:run
    ```
    A aplicação estará disponível em `http://localhost:8080` (ou na porta configurada).



## Instalação e Execução (Frontend - `pagbankapp`)

Para configurar e executar o frontend da aplicação (`pagbankapp`), siga os passos abaixo:

1.  **Navegue até o diretório do frontend:**
    ```bash
    cd ../pagbankapp
    ```
2.  **Instale as dependências:**
    ```bash
    npm install
    ```
3.  **Configure as variáveis de ambiente:**
    Edite o arquivo `src/constants.ts` e configure as variáveis `API_URL` (URL do seu backend, ex: `http://localhost:8080`) e `CHAVE_PUBLICA_PAGBANK` (chave pública do PagBank para criptografia de dados de cartão).
4.  **Execute a aplicação:**
    ```bash
    npm start
    ```
    A aplicação estará disponível em `http://localhost:3000` (ou na porta padrão do React).



## Cartões de Teste e Chaves Públicas

Para testar a funcionalidade de pagamento com cartão, utilize os [Cartões de teste](https://developer.pagbank.com.br/docs/cartoes-de-teste) fornecidos pelo PagBank.

A chave pública padrão para o ambiente Sandbox, utilizada na criptografia dos dados do cartão, é:

```
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAr+ZqgD892U9/HXsa7XqBZUayPquAfh9xx4iwUbTSUAvTlmiXFQNTp0Bvt/5vK2FhMj39qSv1zi2OuBjvW38q1E374nzx6NNBL5JosV0+SDINTlCG0cmigHuBOyWzYmjgca+mtQu4WczCaApNaSuVqgb8u7Bd9GCOL4YJotvV5+81frlSwQXralhwRzGhj/A57CGPgGKiuPT+AOGmykIGEZsSD9RKkyoKIoc0OS8CPIzdBOtTQCIwrLn2FxI83Clcg55W8gkFSOS6rWNbG5qFZWMll6yl02HtunalHmUlRUL66YeGXdMDC2PuRcmZbGO5a/2tbVppW6mfSWG3NPRpgwIDAQAB
```

Para mais informações sobre chaves públicas, consulte a documentação oficial: [Chaves públicas](https://developer.pagbank.com.br/docs/chaves-publicas).



## Links Úteis

*   [Documentação Oficial PagBank](https://developer.pagbank.com.br/docs/o-pagbank)
*   [API de Pedidos - Criar pedido com QR Code](https://developer.pagbank.com.br/reference/criar-pedido-pedido-com-qr-code)
*   [API de Pedidos - Criar e pagar pedido com Boleto](https://developer.pagbank.com.br/reference/criar-pagar-pedido-com-boleto)
*   [API de Pedidos - Criar e pagar pedido com Cartão](https://developer.pagbank.com.br/reference/criar-pagar-pedido-com-cartao)
*   [Simulador PIX e Boleto](https://developer.pagbank.com.br/docs/simulador)
*   [Cartões de Teste](https://developer.pagbank.com.br/docs/cartoes-de-teste)
*   [Chaves Públicas](https://developer.pagbank.com.br/docs/chaves-publicas)



## Licença

Este projeto está licenciado sob a licença MIT.

