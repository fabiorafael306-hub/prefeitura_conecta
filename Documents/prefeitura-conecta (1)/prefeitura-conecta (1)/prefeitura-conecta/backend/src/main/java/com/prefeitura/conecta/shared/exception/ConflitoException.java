package com.prefeitura.conecta.shared.exception;
/** Violação de unicidade de regra de negócio (ex.: CPF ou e-mail já cadastrado). Responde 409. */
public class ConflitoException extends RuntimeException {
 public ConflitoException(String mensagem){super(mensagem);}
}
