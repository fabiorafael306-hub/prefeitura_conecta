package com.prefeitura.conecta.shared.exception;
/** Login com credenciais inválidas. Responde 401 com mensagem genérica (não revela se o usuário existe). */
public class CredenciaisInvalidasException extends RuntimeException {
 public CredenciaisInvalidasException(){super("CPF/e-mail ou senha inválidos.");}
}
