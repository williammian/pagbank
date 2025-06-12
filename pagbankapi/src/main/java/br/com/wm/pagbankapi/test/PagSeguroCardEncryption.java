package br.com.wm.pagbankapi.test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

public class PagSeguroCardEncryption {
	
	public static String CHAVE_PUBLICA_PAGBANK = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAr+ZqgD892U9/HXsa7XqBZUayPquAfh9xx4iwUbTSUAvTlmiXFQNTp0Bvt/5vK2FhMj39qSv1zi2OuBjvW38q1E374nzx6NNBL5JosV0+SDINTlCG0cmigHuBOyWzYmjgca+mtQu4WczCaApNaSuVqgb8u7Bd9GCOL4YJotvV5+81frlSwQXralhwRzGhj/A57CGPgGKiuPT+AOGmykIGEZsSD9RKkyoKIoc0OS8CPIzdBOtTQCIwrLn2FxI83Clcg55W8gkFSOS6rWNbG5qFZWMll6yl02HtunalHmUlRUL66YeGXdMDC2PuRcmZbGO5a/2tbVppW6mfSWG3NPRpgwIDAQAB";
	
	private Context jsContext;
    private String jsLibraryCode;

    public PagSeguroCardEncryption() {
        initializeJavaScriptContext();
    }
    
    private void initializeJavaScriptContext() {
        try {
            // Cria o contexto JavaScript com configurações otimizadas
            jsContext = Context.newBuilder("js")
                    .allowAllAccess(true)
                    .option("js.console", "true")
                    .option("js.print", "true")
                    .option("js.load", "true")
                    .option("js.syntax-extensions", "true")
                    .build();
            
            // Configura ambiente browser completo
            setupCompleteEnvironment();
            
            // Baixa e carrega a biblioteca
            jsLibraryCode = downloadPagSeguroLibrary();
            jsContext.eval("js", jsLibraryCode);
            
        } catch (Exception e) {
            System.err.println("Detalhes do erro: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao inicializar contexto JavaScript", e);
        }
    }
    
    private void setupCompleteEnvironment() {
        String fullEnvironment = """
            // === CRYPTO API SIMULATION ===
            var Crypto = function() {
                this.getRandomValues = function(array) {
                    for (var i = 0; i < array.length; i++) {
                        array[i] = Math.floor(Math.random() * 256);
                    }
                    return array;
                };
                
                this.randomUUID = function() {
                    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
                        var r = Math.random() * 16 | 0;
                        var v = c == 'x' ? r : (r & 0x3 | 0x8);
                        return v.toString(16);
                    });
                };
            };
            
            // === LOCATION API ===
            var Location = function() {
                this.href = 'https://localhost/';
                this.origin = 'https://localhost';
                this.protocol = 'https:';
                this.host = 'localhost';
                this.hostname = 'localhost';
                this.port = '';
                this.pathname = '/';
                this.search = '';
                this.hash = '';
            };
            
            // === NAVIGATOR API ===
            var Navigator = function() {
                this.userAgent = 'Mozilla/5.0 (compatible; GraalVM/Java)';
                this.platform = 'Java';
                this.language = 'pt-BR';
                this.languages = ['pt-BR', 'en-US', 'en'];
                this.onLine = true;
                this.cookieEnabled = true;
                this.doNotTrack = null;
                this.hardwareConcurrency = 4;
                this.maxTouchPoints = 0;
                this.vendor = 'GraalVM';
                this.appName = 'Netscape';
                this.appVersion = '5.0 (compatible; GraalVM/Java)';
                this.product = 'Gecko';
            };
            
            // === DOM ELEMENTS SIMULATION ===
            var Element = function(tagName) {
                this.tagName = tagName ? tagName.toUpperCase() : 'DIV';
                this.style = {};
                this.attributes = {};
                this.dataset = {};
                this.classList = {
                    add: function() {},
                    remove: function() {},
                    contains: function() { return false; }
                };
                
                this.setAttribute = function(name, value) {
                    this.attributes[name] = value;
                };
                
                this.getAttribute = function(name) {
                    return this.attributes[name] || null;
                };
                
                this.appendChild = function(child) {
                    return child;
                };
                
                this.removeChild = function(child) {
                    return child;
                };
            };
            
            // === DOCUMENT API ===
            var Document = function() {
                this.documentElement = new Element('HTML');
                this.body = new Element('BODY');
                this.head = new Element('HEAD');
                
                this.createElement = function(tagName) {
                    return new Element(tagName);
                };
                
                this.getElementById = function(id) {
                    return new Element('DIV');
                };
                
                this.querySelector = function(selector) {
                    return new Element('DIV');
                };
                
                this.querySelectorAll = function(selector) {
                    return [new Element('DIV')];
                };
                
                this.addEventListener = function(event, handler) {};
                this.removeEventListener = function(event, handler) {};
            };
            
            // === WINDOW API ===
            var Window = function() {
                this.navigator = new Navigator();
                this.location = new Location();
                this.document = new Document();
                this.crypto = new Crypto();
                this.screen = {
                    width: 1920,
                    height: 1080,
                    availWidth: 1920,
                    availHeight: 1040
                };
                
                this.innerWidth = 1920;
                this.innerHeight = 1080;
                this.outerWidth = 1920;
                this.outerHeight = 1080;
                
                this.devicePixelRatio = 1;
                
                var self = this;
                
                this.setTimeout = function(fn, delay) {
                    if (typeof fn === 'function') {
                        fn();
                    }
                    return 1;
                };
                
                this.clearTimeout = function(id) {};
                this.setInterval = function(fn, delay) { return 1; };
                this.clearInterval = function(id) {};
                
                this.addEventListener = function(event, handler) {};
                this.removeEventListener = function(event, handler) {};
                
                this.btoa = function(str) {
                    return java.util.Base64.getEncoder().encodeToString(
                        java.lang.String.valueOf(str).getBytes('UTF-8')
                    );
                };
                
                this.atob = function(str) {
                    return java.lang.String.valueOf(
                        new java.lang.String(
                            java.util.Base64.getDecoder().decode(str), 'UTF-8'
                        )
                    );
                };
            };
            
            // === CONSOLE API ===
            var Console = function() {
                this.log = function() {
                    print('LOG: ' + Array.prototype.join.call(arguments, ' '));
                };
                this.warn = function() {
                    print('WARN: ' + Array.prototype.join.call(arguments, ' '));
                };
                this.error = function() {
                    print('ERROR: ' + Array.prototype.join.call(arguments, ' '));
                };
                this.info = function() {
                    print('INFO: ' + Array.prototype.join.call(arguments, ' '));
                };
                this.debug = function() {
                    print('DEBUG: ' + Array.prototype.join.call(arguments, ' '));
                };
            };
            
            // === GLOBAL SETUP ===
            var window = new Window();
            var document = window.document;
            var navigator = window.navigator;
            var location = window.location;
            var console = new Console();
            var crypto = window.crypto;
            
            // Global functions
            var setTimeout = window.setTimeout;
            var clearTimeout = window.clearTimeout;
            var setInterval = window.setInterval;
            var clearInterval = window.clearInterval;
            var btoa = window.btoa;
            var atob = window.atob;
            
            // Make them available globally
            this.window = window;
            this.document = document;
            this.navigator = navigator;
            this.location = location;
            this.console = console;
            this.crypto = crypto;
            this.setTimeout = setTimeout;
            this.clearTimeout = clearTimeout;
            this.setInterval = setInterval;
            this.clearInterval = clearInterval;
            this.btoa = btoa;
            this.atob = atob;
            
            // Additional globals that might be needed
            this.Element = Element;
            this.Document = Document;
            this.Window = Window;
            """;
        
        jsContext.eval("js", fullEnvironment);
    }
    
    private String downloadPagSeguroLibrary() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://assets.pagseguro.com.br/checkout-sdk-js/rc/dist/browser/pagseguro.min.js"))
                .timeout(Duration.ofSeconds(30))
                .header("User-Agent", "Mozilla/5.0 (compatible; GraalVM/Java)")
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("Erro ao baixar biblioteca PagSeguro: " + response.statusCode());
        }
        
        return response.body();
    }
    
    public EncryptionResult encryptCard(String publicKey, String holder, String number, 
                                      String expMonth, String expYear, String securityCode) {
        try {
            // Prepara o código JavaScript
            Map<String, String> params = new HashMap<>();
            params.put("publicKey", escapeJavaScript(publicKey));
            params.put("holder", escapeJavaScript(holder));
            params.put("number", escapeJavaScript(number));
            params.put("expMonth", escapeJavaScript(expMonth));
            params.put("expYear", escapeJavaScript(expYear));
            params.put("securityCode", escapeJavaScript(securityCode));
            
            String jsCode = String.format("""
                try {
                    console.log('Iniciando criptografia do cartão...');
                    
                    var cardData = {
                        publicKey: '%s',
                        holder: '%s',
                        number: '%s',
                        expMonth: '%s',
                        expYear: '%s',
                        securityCode: '%s'
                    };
                    
                    console.log('Dados do cartão preparados');
                    
                    var card = PagSeguro.encryptCard(cardData);
                    
                    console.log('Criptografia concluída');
                    
                    var result = {
                        encryptedCard: card.encryptedCard || null,
                        hasErrors: card.hasErrors || false,
                        errors: card.errors || null
                    };
                    
                    JSON.stringify(result);
                } catch (error) {
                    console.error('Erro na criptografia:', error);
                    JSON.stringify({
                        encryptedCard: null,
                        hasErrors: true,
                        errors: error.message || 'Erro desconhecido'
                    });
                }
                """,
                params.get("publicKey"), params.get("holder"), params.get("number"),
                params.get("expMonth"), params.get("expYear"), params.get("securityCode")
            );
            
            // Executa o JavaScript
            Value result = jsContext.eval("js", jsCode);
            String jsonResult = result.asString();
            
            System.out.println("Resultado da criptografia: " + jsonResult);
            
            return parseEncryptionResult(jsonResult);
            
        } catch (Exception e) {
            System.err.println("Erro detalhado: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao criptografar cartão", e);
        }
    }
    
    private String escapeJavaScript(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                   .replace("'", "\\'")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    private EncryptionResult parseEncryptionResult(String jsonResult) {
        try {
            String encryptedCard = extractJsonValue(jsonResult, "encryptedCard");
            boolean hasErrors = Boolean.parseBoolean(extractJsonValue(jsonResult, "hasErrors"));
            String errors = extractJsonValue(jsonResult, "errors");
            
            return new EncryptionResult(encryptedCard, hasErrors, errors);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao fazer parse do resultado: " + jsonResult, e);
        }
    }
    
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) return null;
        
        startIndex += searchKey.length();
        while (startIndex < json.length() && Character.isWhitespace(json.charAt(startIndex))) {
            startIndex++;
        }
        
        if (startIndex >= json.length()) return null;
        
        char firstChar = json.charAt(startIndex);
        
        if (firstChar == '"') {
            startIndex++;
            StringBuilder value = new StringBuilder();
            while (startIndex < json.length()) {
                char c = json.charAt(startIndex);
                if (c == '"') break;
                if (c == '\\' && startIndex + 1 < json.length()) {
                    startIndex++;
                    char escaped = json.charAt(startIndex);
                    switch (escaped) {
                        case 'n' -> value.append('\n');
                        case 't' -> value.append('\t');
                        case 'r' -> value.append('\r');
                        case '\\' -> value.append('\\');
                        case '"' -> value.append('"');
                        default -> value.append(escaped);
                    }
                } else {
                    value.append(c);
                }
                startIndex++;
            }
            return value.toString();
        } else if (firstChar == 'n' && json.substring(startIndex).startsWith("null")) {
            return null;
        } else if (firstChar == 't' && json.substring(startIndex).startsWith("true")) {
            return "true";
        } else if (firstChar == 'f' && json.substring(startIndex).startsWith("false")) {
            return "false";
        }
        
        return null;
    }
    
    public void close() {
        if (jsContext != null) {
            jsContext.close();
        }
    }
    
    
    
    // Exemplo de uso
    public static void main(String[] args) {
    	System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
    	
        PagSeguroCardEncryption encryption = new PagSeguroCardEncryption();
        
        CreditCardData creditCard = CreditCardData.createMockCreditCardData();
        
        try {
            EncryptionResult result = encryption.encryptCard(
            	CHAVE_PUBLICA_PAGBANK,
                creditCard.getHolderName(),
                creditCard.getNumber(),
                creditCard.getExpiryMonth(),
                creditCard.getExpiryYear(), 
                creditCard.getCvv()
            );
            
            System.out.println("Cartão criptografado: " + result.getEncryptedCard());
            System.out.println("Tem erros: " + result.hasErrors());
            System.out.println("Erros: " + result.getErrors());
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            encryption.close();
        }
    }
    
}
