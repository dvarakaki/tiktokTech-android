package com.aula.tiktoktech;

import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.tiktoktech.adapter.PostsAdapter;
import com.aula.tiktoktech.model.Post;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.List;

/** Tela do feed: mostra em tempo real os posts que a turma inteira publica no Firestore. */
public class MainActivity extends AppCompatActivity {

    private final PostsAdapter adapter = new PostsAdapter();
    private ListenerRegistration registroFeed;
    private ProgressBar progress;
    private TextView txtVazio;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        progress = findViewById(R.id.progress);
        txtVazio = findViewById(R.id.txtVazio);
        toolbar = findViewById(R.id.toolbar);

        RecyclerView recyclerPosts = findViewById(R.id.recyclerPosts);
        recyclerPosts.setLayoutManager(new LinearLayoutManager(this));
        recyclerPosts.setAdapter(adapter);

        toolbar.inflateMenu(R.menu.menu_usuario);
        toolbar.setOnMenuItemClickListener(this::aoClicarMenu);

        findViewById(R.id.fabNovaFoto).setOnClickListener(v -> {
            if (UsuarioPrefs.estaLogado(this)) {
                startActivity(new Intent(this, SelfActivity.class));
            } else {
                exigirLogin();
            }
        });
        if (!UsuarioPrefs.estaLogado(this)) abrirLogin();
    }

    @Override
    protected void onResume() {
        super.onResume();
        atualizarUsuario();
    }

    @Override
    protected void onStart() {
        super.onStart();
        progress.setVisibility(View.VISIBLE);
        // Ouve o Firestore em tempo real: quando qualquer aluno publica, o feed atualiza sozinho.
        registroFeed = FirebaseFirestore.getInstance()
                .collection("posts")
                .orderBy("criadoEm", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, erro) -> {
                    progress.setVisibility(View.GONE);
                    if (erro != null) {
                        Toast.makeText(this, getString(R.string.msg_erro_feed, erro.getMessage()),
                                Toast.LENGTH_LONG).show();
                        Log.e("MainActivity", "Erro ao carregar o feed", erro);
                        return;
                    }
                    if (snapshot == null) return;
                    List<Post> posts = snapshot.toObjects(Post.class);
                    adapter.atualizar(posts);
                    txtVazio.setVisibility(posts.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    @Override
    protected void onStop() {
        if (registroFeed != null) registroFeed.remove();
        super.onStop();
    }

    private boolean aoClicarMenu(MenuItem item) {
        if (item.getItemId() == R.id.acaoUsuario) {
            abrirLogin();
            return true;
        }
        if (item.getItemId() == R.id.acaoSair) {
            UsuarioPrefs.sair(this);
            atualizarUsuario();
            Toast.makeText(this, R.string.msg_logout, Toast.LENGTH_SHORT).show();
            abrirLogin();
            return true;
        }
        return false;
    }

    private void exigirLogin() {
        Toast.makeText(this, R.string.msg_login_obrigatorio, Toast.LENGTH_LONG).show();
        abrirLogin();
    }

    private void abrirLogin() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    private void atualizarUsuario() {
        String email = UsuarioPrefs.obter(this);
        boolean identificado = !email.isEmpty();
        toolbar.setSubtitle(identificado
                ? getString(R.string.usuario_identificado, email)
                : getString(R.string.usuario_visitante));
        toolbar.getMenu().findItem(R.id.acaoUsuario).setTitle(
                identificado ? R.string.acao_trocar_usuario : R.string.acao_entrar);
        toolbar.getMenu().findItem(R.id.acaoSair).setVisible(identificado);
    }
}
