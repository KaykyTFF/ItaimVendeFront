package org.example;

import org.example.model.UsuarioDTO;

/**
 * Gerenciador de Contexto de Usuário (Session State).
 * <p>
 * Armazena globalmente a referência do usuário autenticado durante o ciclo de vida da aplicação.
 * Utiliza membros estáticos para permitir acesso a partir de qualquer tela.
 *
 * @author Kayky Terles
 * @version 0.0.1
 */
public class Sessao {

    /**
     * Referência ao usuário autenticado atualmente.
     * Se null, indica que não há sessão ativa (usuário deslogado).
     */
    private static UsuarioDTO usuarioLogado;

    /**
     * Registra o usuário na sessão após uma autenticação bem-sucedida.
     *
     * @param usuario O DTO contendo os dados do usuário retornado pela API.
     */
    public static void setUsuario(UsuarioDTO usuario) {
        usuarioLogado = usuario;
    }

    /**
     * Recupera os dados do usuário da sessão atual.
     *
     * @return O objeto {@link UsuarioDTO} se houver login, ou {@code null} caso contrário.
     */
    public static UsuarioDTO getUsuario() {
        return usuarioLogado;
    }

    /**
     * Invalida a sessão atual.
     * Deve ser chamado durante o processo de logout para limpar dados sensíveis da memória.
     */
    public static void limpar() {
        usuarioLogado = null;
    }
}