package br.com.wm.pagbankapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {
	
	@ExceptionHandler(ValidacaoException.class)
    public ProblemDetail handlePicPayException(ValidacaoException e) {
		ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
		pb.setTitle("Validation Error");
        return pb;
    }
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        var fieldErros = e.getFieldErrors()
                .stream()
                .map(f -> new InvalidParam(f.getField(), f.getDefaultMessage()))
                .toList();

        var pb = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);

        pb.setTitle("Your request parameters didn't validate.");
        pb.setProperty("invalid-params", fieldErros);

        return pb;
    }

    private record InvalidParam(String name, String reason){}
    
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleInternalServerError(Exception e) {
    	ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		pb.setTitle("Internal Server Error");
        return pb;
    }

}
