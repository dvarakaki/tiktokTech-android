package com.aula.tiktoktech;

import android.content.Context;

/** Identificação local exigida pelo exercício de SharedPreferences: só um nome/login, sem senha. */
public final class UsuarioPrefs {
    private static final String SESSAO = "usuario";
    private static final String CHAVE_LOGIN = "login";

    private UsuarioPrefs() { }

    public static String obter(Context context) {
        String login = context.getSharedPreferences(SESSAO, Context.MODE_PRIVATE)
                .getString(CHAVE_LOGIN, "");
        return login == null ? "" : login.trim();
    }

    public static boolean estaLogado(Context context) {
        return !obter(context).isEmpty();
    }

    public static boolean entrar(Context context, String login) {
        if (login == null || login.trim().isEmpty()) return false;
        context.getSharedPreferences(SESSAO, Context.MODE_PRIVATE)
                .edit().putString(CHAVE_LOGIN, login.trim()).apply();
        return true;
    }

    public static void sair(Context context) {
        context.getSharedPreferences(SESSAO, Context.MODE_PRIVATE)
                .edit().remove(CHAVE_LOGIN).apply();
    }
}
