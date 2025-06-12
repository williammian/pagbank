package br.com.wm.pagbankapi.test;

public class EncryptionResult {

	private final String encryptedCard;
    private final boolean hasErrors;
    private final String errors;
    
    public EncryptionResult(String encryptedCard, boolean hasErrors, String errors) {
        this.encryptedCard = encryptedCard;
        this.hasErrors = hasErrors;
        this.errors = errors;
    }
    
    public String getEncryptedCard() {
        return encryptedCard;
    }
    
    public boolean hasErrors() {
        return hasErrors;
    }
    
    public String getErrors() {
        return errors;
    }
    
    @Override
    public String toString() {
        return String.format("EncryptionResult{encryptedCard='%s', hasErrors=%s, errors='%s'}", 
                           encryptedCard, hasErrors, errors);
    }
	
}
