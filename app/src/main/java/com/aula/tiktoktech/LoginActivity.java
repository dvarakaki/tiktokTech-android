package com.aula.tiktoktech;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {
    private TextInputLayout campoLogin;
    private TextInputEditText edtLogin;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        campoLogin = findViewById(R.id.campoLogin);
        edtLogin = findViewById(R.id.edtLogin);
        TextView sessao = findViewById(R.id.txtSessaoAtual);
        String atual = UsuarioPrefs.obter(this);
        if (!atual.isEmpty()) {
            sessao.setVisibility(View.VISIBLE);
            sessao.setText(getString(R.string.usuario_identificado, atual));
            edtLogin.setText(atual);
        }

        findViewById(R.id.btnSalvar).setOnClickListener(v -> entrar());
        findViewById(R.id.btnVisitante).setOnClickListener(v -> {
            UsuarioPrefs.sair(this);
            finish();
        });
        edtLogin.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                entrar();
                return true;
            }
            return false;
        });
    }

    private void entrar() {
        String login = texto(edtLogin);
        campoLogin.setError(null);
        if (login.isEmpty()) {
            campoLogin.setError(getString(R.string.msg_login_vazio));
            return;
        }
        UsuarioPrefs.entrar(this, login);
        setResult(RESULT_OK);
        finish();
    }

    private static String texto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }
}
