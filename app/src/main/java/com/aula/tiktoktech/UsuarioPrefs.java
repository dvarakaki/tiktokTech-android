package com.aula.tiktoktech;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Patterns;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/** Cadastro e sessão locais exigidos pelo exercício de SharedPreferences. */
public final class UsuarioPrefs {
    private static final String SESSAO = "usuario";
    private static final String CONTAS = "contas";
    private static final String CHAVE_LOGIN = "login";

    private UsuarioPrefs() { }

    public static String obter(Context context) {
        String email = context.getSharedPreferences(SESSAO, Context.MODE_PRIVATE)
                .getString(CHAVE_LOGIN, "");
        email = normalizarEmail(email);
        return contaExiste(context, email) ? email : "";
    }

    public static boolean estaLogado(Context context) {
        return !obter(context).isEmpty();
    }

    public static boolean emailValido(String email) {
        String normalizado = normalizarEmail(email);
        return !normalizado.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(normalizado).matches();
    }

    public static boolean contaExiste(Context context, String email) {
        String normalizado = normalizarEmail(email);
        return !normalizado.isEmpty() && contas(context).contains(chaveConta(normalizado));
    }

    public static boolean cadastrar(Context context, String email, String senha) {
        String normalizado = normalizarEmail(email);
        if (!emailValido(normalizado) || senha == null || senha.length() < 6
                || contaExiste(context, normalizado)) return false;
        contas(context).edit().putString(chaveConta(normalizado), hash(normalizado, senha)).apply();
        iniciarSessao(context, normalizado);
        return true;
    }

    public static boolean autenticar(Context context, String email, String senha) {
        String normalizado = normalizarEmail(email);
        String esperado = contas(context).getString(chaveConta(normalizado), null);
        if (esperado == null || senha == null) return false;
        boolean confere = MessageDigest.isEqual(
                esperado.getBytes(StandardCharsets.UTF_8),
                hash(normalizado, senha).getBytes(StandardCharsets.UTF_8));
        if (confere) iniciarSessao(context, normalizado);
        return confere;
    }

    public static void sair(Context context) {
        context.getSharedPreferences(SESSAO, Context.MODE_PRIVATE)
                .edit().remove(CHAVE_LOGIN).apply();
    }

    private static void iniciarSessao(Context context, String email) {
        context.getSharedPreferences(SESSAO, Context.MODE_PRIVATE)
                .edit().putString(CHAVE_LOGIN, normalizarEmail(email)).apply();
    }

    private static SharedPreferences contas(Context context) {
        return context.getSharedPreferences(CONTAS, Context.MODE_PRIVATE);
    }

    private static String normalizarEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String chaveConta(String email) {
        return "conta_" + Base64.encodeToString(
                email.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP | Base64.URL_SAFE);
    }

    private static String hash(String email, String senha) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((email + ":TikTokTech:" + senha)
                    .getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException erro) {
            throw new IllegalStateException("SHA-256 indisponível", erro);
        }
    }
}
