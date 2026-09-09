package com.aula.tiktoktech;

// Adaptado de myself: mesmos componentes e métodos principais da aula.
import android.content.Intent;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.io.File;
import java.io.IOException;

public class SelfActivity extends AppCompatActivity {
    // Componentes de tela, como no projeto myself.
    private Uri fotoUri;
    private Uri cameraUri;
    private Uri previewUri;
    private ImageView imgFoto;
    private Button btnTirarFoto;
    private Button btnGaleria;
    private Button btnEnviar;
    private ProgressBar progress;
    private TextView txtStatus;
    private TikTokApp app;

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), tirouFoto -> {
                if (tirouFoto && cameraUri != null) selecionarFoto(cameraUri);
                else Toast.makeText(this, R.string.status_foto_cancelada, Toast.LENGTH_SHORT).show();
            });

    private ActivityResultLauncher<Intent> galeriaAbrir =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult o) {
                            if (o.getResultCode() == RESULT_OK && o.getData() != null
                                    && o.getData().getData() != null) {
                                selecionarFoto(o.getData().getData());
                            }
                        }
                    });

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (TikTokApp) getApplication();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_self);
        int margem = Math.round(16 * getResources().getDisplayMetrics().density);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left + margem, bars.top + margem, bars.right + margem, bars.bottom + margem);
            return insets;
        });
        imgFoto = findViewById(R.id.imgFoto);
        btnTirarFoto = findViewById(R.id.btnTirarFoto);
        btnGaleria = findViewById(R.id.btnGaleria);
        btnEnviar = findViewById(R.id.btnEnviar);
        progress = findViewById(R.id.progress);
        txtStatus = findViewById(R.id.txtStatus);
        btnTirarFoto.setOnClickListener(v -> tirarFoto());
        btnGaleria.setOnClickListener(v -> abrirGaleria());
        btnEnviar.setOnClickListener(v -> salvarNuvem());

        // Recuperar informações após girar a tela ou recriar a Activity.
        if (savedInstanceState != null) {
            String salva = savedInstanceState.getString("fotoUri");
            if (app.foto == null && salva != null) app.foto = Uri.parse(salva);
            String pendente = savedInstanceState.getString("cameraUri");
            if (pendente != null) cameraUri = Uri.parse(pendente);
        }
        if (app.url.isEmpty() && app.foto == null) {
            app.url = getSharedPreferences("self", MODE_PRIVATE).getString("url", "");
        }
    }

    @Override protected void onStart() {
        super.onStart();
        // O callback do upload atualiza a Activity visível, inclusive após rotação.
        app.atualizarTela = this::atualizarTela;
        atualizarTela();
    }

    @Override protected void onStop() {
        app.atualizarTela = null;
        super.onStop();
    }

    private void abrirGaleria() {
        // Abre um aplicativo de galeria, como no exemplo myself.
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        try { galeriaAbrir.launch(intent); }
        catch (android.content.ActivityNotFoundException erro) {
            Toast.makeText(this, "Nenhuma galeria disponível neste aparelho.", Toast.LENGTH_LONG).show();
        }
    }

    private void salvarNuvem() {
        if (fotoUri == null) {
            txtStatus.setText(R.string.erro_sem_foto);
            return;
        }
        // UploadCallback e inicialização ficam na Application para sobreviver à rotação.
        app.enviar("D");
    }

    private void tirarFoto() {
        File pasta = getExternalFilesDir(null);
        if (pasta == null) {
            txtStatus.setText("Armazenamento indisponível. Tente usar a galeria.");
            return;
        }
        File arquivo = new File(pasta, "foto_" + System.currentTimeMillis() + ".jpg");
        try {
            cameraUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", arquivo);
            cameraLauncher.launch(cameraUri);
        } catch (RuntimeException erro) {
            txtStatus.setText("Não foi possível abrir a câmera: " + erro.getMessage());
        }
    }

    private void selecionarFoto(Uri uri) {
        fotoUri = uri;
        app.foto = uri;
        app.url = "";
        app.status = getString(R.string.status_foto_ok);
        atualizarTela();
    }

    private void atualizarTela() {
        fotoUri = app.foto;
        if (fotoUri != null && !fotoUri.equals(previewUri)) {
            try {
                // Reduz somente a prévia para evitar estouro de memória com fotos grandes.
                imgFoto.setImageDrawable(ImageDecoder.decodeDrawable(
                        ImageDecoder.createSource(getContentResolver(), fotoUri), (decoder, info, source) -> {
                            int maior = Math.max(info.getSize().getWidth(), info.getSize().getHeight());
                            decoder.setTargetSampleSize(Math.max(1, (maior + 1199) / 1200));
                        }));
                previewUri = fotoUri;
            } catch (IOException | RuntimeException erro) {
                app.foto = null;
                fotoUri = null;
                imgFoto.setImageResource(R.drawable.ic_imagem_vazia);
                app.status = "Não foi possível ler a imagem. Selecione outra foto.";
            }
        }
        progress.setVisibility(app.enviando ? View.VISIBLE : View.GONE);
        btnTirarFoto.setEnabled(!app.enviando);
        btnGaleria.setEnabled(!app.enviando);
        btnEnviar.setEnabled(fotoUri != null && !app.enviando && app.url.isEmpty());
        txtStatus.setText(app.url.isEmpty() ? app.status : app.url);
        // autoLink do XML permite abrir a URL; pressione o texto para copiar.
    }

    @Override protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (fotoUri != null) outState.putString("fotoUri", fotoUri.toString());
        if (cameraUri != null) outState.putString("cameraUri", cameraUri.toString());
    }
}
