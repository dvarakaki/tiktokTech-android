package com.aula.tiktoktech;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {
    private TextInputLayout campoEmail;
    private TextInputLayout campoSenha;
    private TextInputEditText edtEmail;
    private TextInputEditText edtSenha;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        campoEmail = findViewById(R.id.campoEmail);
        campoSenha = findViewById(R.id.campoSenha);
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);
        TextView sessao = findViewById(R.id.txtSessaoAtual);
        String atual = UsuarioPrefs.obter(this);
        if (!atual.isEmpty()) {
            sessao.setVisibility(View.VISIBLE);
            sessao.setText(getString(R.string.login_sessao_atual, atual));
        }

        findViewById(R.id.btnEntrar).setOnClickListener(v -> entrar());
        findViewById(R.id.btnCadastrar).setOnClickListener(v -> cadastrar());
        findViewById(R.id.btnVisitante).setOnClickListener(v -> {
            UsuarioPrefs.sair(this);
            finish();
        });
        edtSenha.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                entrar();
                return true;
            }
            return false;
        });
    }

    private void entrar() {
        String email = texto(edtEmail);
        String senha = texto(edtSenha);
        if (!validar(email, senha)) return;
        if (!UsuarioPrefs.contaExiste(this, email)) {
            campoEmail.setError(getString(R.string.msg_conta_inexistente));
            return;
        }
        if (!UsuarioPrefs.autenticar(this, email, senha)) {
            campoSenha.setError(getString(R.string.msg_senha_incorreta));
            edtSenha.requestFocus();
            return;
        }
        concluir(email);
    }

    private void cadastrar() {
        String email = texto(edtEmail);
        String senha = texto(edtSenha);
        if (!validar(email, senha)) return;
        if (UsuarioPrefs.contaExiste(this, email)) {
            campoEmail.setError(getString(R.string.msg_conta_existente));
            return;
        }
        if (UsuarioPrefs.cadastrar(this, email, senha)) concluir(email);
    }

    private boolean validar(String email, String senha) {
        campoEmail.setError(null);
        campoSenha.setError(null);
        boolean valido = true;
        if (!UsuarioPrefs.emailValido(email)) {
            campoEmail.setError(getString(R.string.msg_email_invalido));
            valido = false;
        }
        if (senha.length() < 6) {
            campoSenha.setError(getString(R.string.msg_senha_curta));
            valido = false;
        }
        return valido;
    }

    private void concluir(String email) {
        Toast.makeText(this, getString(R.string.msg_boas_vindas, email.trim()), Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private static String texto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }
}
