package com.aula.tiktoktech;

import android.app.Application;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

/** Inicializa o Cloudinary uma vez e mantém o envio independente da rotação da tela. */
public class TikTokApp extends Application {
    public Uri foto;
    public String url = "";
    public String status = "Selecione uma foto para começar.";
    public boolean enviando;
    public boolean publicado;
    public Runnable atualizarTela;
    private final Handler main = new Handler(Looper.getMainLooper());

    @Override public void onCreate() {
        super.onCreate();
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", getString(R.string.cloudinary_cloud_name));
        MediaManager.init(this, config);
    }

    private void atualizar(String mensagem, boolean ocupado) {
        main.post(() -> {
            status = mensagem;
            enviando = ocupado;
            if (atualizarTela != null) atualizarTela.run();
        });
    }

    public void enviar(String turma, String descricao) {
        if (!UsuarioPrefs.estaLogado(this)) {
            atualizar(getString(R.string.msg_login_obrigatorio), false);
            return;
        }
        if (foto == null || enviando || publicado || !turma.matches("[D-I]")) return;
        enviando = true;
        publicado = false;
        url = "";
        status = "Enviando para tiktoktech_sala" + turma + "…";
        if (atualizarTela != null) atualizarTela.run();
        try {
            MediaManager.get().upload(foto)
                    .unsigned(getString(R.string.cloudinary_upload_preset))
                    .option("resource_type", "image")
                    .option("folder", "tiktoktech_sala" + turma)
                    .callback(new UploadCallback() {
                        @Override public void onStart(String id) { }
                        @Override public void onProgress(String id, long bytes, long total) {
                            if (total > 0) atualizar("Enviando… " + (bytes * 100 / total) + "%", true);
                        }
                        @Override public void onSuccess(String id, Map result) {
                            String recebida = (String) result.get("secure_url");
                            if (recebida != null && recebida.startsWith("https://")) {
                                main.post(() -> {
                                    status = "Imagem enviada. Salvando a publicação…";
                                    if (atualizarTela != null) atualizarTela.run();
                                    salvarPost(recebida, descricao);
                                });
                            } else {
                                atualizar("O serviço não devolveu uma URL HTTPS. Tente novamente.", false);
                            }
                        }
                        @Override public void onError(String id, ErrorInfo error) {
                            atualizar("Erro ao enviar: " + error.getDescription()
                                    + "\nConfira a conexão e tente novamente.", false);
                        }
                        @Override public void onReschedule(String id, ErrorInfo error) {
                            atualizar("Aguardando conexão para tentar novamente…", true);
                        }
                    }).dispatch();
        } catch (RuntimeException error) {
            atualizar("Não foi possível iniciar o envio: " + error.getMessage(), false);
        }
    }

    private void salvarPost(String imagemUrl, String descricao) {
        Map<String, Object> post = new HashMap<>();
        post.put("url", imagemUrl);
        post.put("descricao", descricao.trim());
        post.put("likes", 0L);
        post.put("dislikes", 0L);
        post.put("comentarios", 0L);
        // Timestamp do servidor, não um Long local: é o tipo que o modelo Post espera ao ler o feed.
        post.put("criadoEm", FieldValue.serverTimestamp());
        FirebaseFirestore.getInstance().collection("posts").add(post)
                .addOnSuccessListener(documento -> main.post(() -> {
                    publicado = true;
                    enviando = false;
                    url = imagemUrl;
                    status = "Publicado no feed!\n" + imagemUrl;
                    getSharedPreferences("self", MODE_PRIVATE).edit()
                            .putString("url", url).apply();
                    if (atualizarTela != null) atualizarTela.run();
                }))
                .addOnFailureListener(erro -> atualizar(
                        "A imagem foi enviada, mas não foi possível salvar no feed: "
                                + erro.getMessage() + "\nToque em enviar para tentar novamente.", false));
    }
}
