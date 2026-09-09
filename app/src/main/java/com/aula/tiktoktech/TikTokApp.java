package com.aula.tiktoktech;

import android.app.Application;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import java.util.HashMap;
import java.util.Map;

/** Inicializa o Cloudinary uma vez e mantém o envio independente da rotação da tela. */
public class TikTokApp extends Application {
    public Uri foto;
    public String url = "";
    public String status = "Selecione uma foto para começar.";
    public boolean enviando;
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

    public void enviar(String turma) {
        if (foto == null || enviando || !turma.matches("[D-I]")) return;
        enviando = true;
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
                            main.post(() -> {
                                enviando = false;
                                if (recebida != null && recebida.startsWith("https://")) {
                                    url = recebida;
                                    status = "Foto enviada! Abra o link para conferir.";
                                    getSharedPreferences("self", MODE_PRIVATE).edit()
                                            .putString("url", url).apply();
                                } else {
                                    status = "O serviço não devolveu uma URL HTTPS. Tente novamente.";
                                }
                                if (atualizarTela != null) atualizarTela.run();
                            });
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
}
