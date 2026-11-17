package com.prefeitura.conecta.shared;
import com.prefeitura.conecta.shared.exception.ConflitoException;
import com.prefeitura.conecta.shared.exception.CredenciaisInvalidasException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.util.*;
/**
 * Formato padronizado de erro: {"erro": "mensagem"} e, para validação, {"erro": "Dados inválidos", "detalhes": [{"campo","mensagem"}]}.
 * Mensagens internas (stack trace, SQL) nunca são devolvidas ao cliente.
 */
@RestControllerAdvice
public class ApiExceptionHandler {
 private static final Logger log=LoggerFactory.getLogger(ApiExceptionHandler.class);

 @ExceptionHandler(NoSuchElementException.class) @ResponseStatus(HttpStatus.NOT_FOUND)
 public Map<String,String> notFound(Exception e){return Map.of("erro",e.getMessage());}

 @ExceptionHandler(ConflitoException.class) @ResponseStatus(HttpStatus.CONFLICT)
 public Map<String,String> conflict(Exception e){return Map.of("erro",e.getMessage());}

 /** Corrida entre duas requisições de cadastro com o mesmo CPF/e-mail: a constraint UNIQUE do banco é a última barreira. */
 @ExceptionHandler(DataIntegrityViolationException.class) @ResponseStatus(HttpStatus.CONFLICT)
 public Map<String,String> integrity(DataIntegrityViolationException e){
  log.warn("Violação de integridade: {}",e.getMostSpecificCause().getMessage());
  return Map.of("erro","Registro duplicado ou inválido.");
 }

 @ExceptionHandler(IllegalArgumentException.class) @ResponseStatus(HttpStatus.BAD_REQUEST)
 public Map<String,String> badRequest(Exception e){return Map.of("erro",e.getMessage());}

 @ExceptionHandler(CredenciaisInvalidasException.class) @ResponseStatus(HttpStatus.UNAUTHORIZED)
 public Map<String,String> unauthorized(Exception e){return Map.of("erro",e.getMessage());}

 @ExceptionHandler(AccessDeniedException.class) @ResponseStatus(HttpStatus.FORBIDDEN)
 public Map<String,String> forbidden(Exception e){return Map.of("erro",e.getMessage());}

 @ExceptionHandler(MethodArgumentNotValidException.class) @ResponseStatus(HttpStatus.BAD_REQUEST)
 public Map<String,Object> invalid(MethodArgumentNotValidException e){
  var errors=e.getBindingResult().getFieldErrors().stream().map(x->Map.of("campo",x.getField(),"mensagem",Objects.toString(x.getDefaultMessage(),"Inválido"))).toList();
  return Map.of("erro","Dados inválidos","detalhes",errors);
 }

 @ExceptionHandler(HttpMessageNotReadableException.class) @ResponseStatus(HttpStatus.BAD_REQUEST)
 public Map<String,String> unreadable(Exception e){return Map.of("erro","Corpo da requisição ausente ou em formato JSON inválido.");}

 @ExceptionHandler(MethodArgumentTypeMismatchException.class) @ResponseStatus(HttpStatus.BAD_REQUEST)
 public Map<String,String> typeMismatch(MethodArgumentTypeMismatchException e){return Map.of("erro","Parâmetro inválido: "+e.getName()+".");}

 @ExceptionHandler(NoResourceFoundException.class) @ResponseStatus(HttpStatus.NOT_FOUND)
 public Map<String,String> noResource(Exception e){return Map.of("erro","Recurso não encontrado.");}

 @ExceptionHandler(HttpRequestMethodNotSupportedException.class) @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
 public Map<String,String> method(Exception e){return Map.of("erro","Método HTTP não suportado neste endpoint.");}

 @ExceptionHandler(Exception.class) @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
 public Map<String,String> generic(Exception e){
  log.error("Erro não tratado",e);
  return Map.of("erro","Erro interno. Tente novamente mais tarde.");
 }
}
