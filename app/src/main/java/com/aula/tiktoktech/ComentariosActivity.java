package com.aula.tiktoktech;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.tiktoktech.adapter.CommentAdapter;
import com.aula.tiktoktech.model.Comentario;
import com.aula.tiktoktech.model.Post;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

/** Lista e permite escrever comentários de um post, exigindo login para publicar. */
public class ComentariosActivity extends AppCompatActivity implements CommentAdapter.Acoes {
    public static final String EXTRA_POST_AUTOR = "postAutor";

    private CommentAdapter adapter;
    private final FirebaseFirestore banco = FirebaseFirestore.getInstance();
    private ListenerRegistration registroComentarios;
    private String postId;
    private String postAutor;
    private View txtVazioComentarios;
    private String comentarioRespondendoId;
    private List<Comentario> comentariosAtuais = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comentarios);

        postId = getIntent().getStringExtra(MainActivity.EXTRA_POST_ID);
        postAutor = getIntent().getStringExtra(EXTRA_POST_AUTOR);
        if (postId == null) {
            Toast.makeText(this, R.string.msg_post_invalido, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ((MaterialToolbar) findViewById(R.id.toolbarComentarios))
                .setNavigationOnClickListener(v -> finish());

        boolean souDonoDoPost = postAutor != null && postAutor.equals(UsuarioPrefs.obter(this));
        adapter = new CommentAdapter(this, souDonoDoPost);
        RecyclerView recyclerComentarios = findViewById(R.id.recyclerComentarios);
        recyclerComentarios.setLayoutManager(new LinearLayoutManager(this));
        recyclerComentarios.setAdapter(adapter);
        txtVazioComentarios = findViewById(R.id.txtVazioComentarios);

        TextInputEditText autor = findViewById(R.id.edtAutor);
        View painel = findViewById(R.id.painelNovoComentario);
        View aviso = findViewById(R.id.txtLoginComentarios);
        boolean logado = UsuarioPrefs.estaLogado(this);
        painel.setVisibility(logado ? View.VISIBLE : View.GONE);
        aviso.setVisibility(logado ? View.GONE : View.VISIBLE);
        if (logado) {
            autor.setText(UsuarioPrefs.obter(this));
            autor.setEnabled(false);
        }

        TextInputEditText edtComentario = findViewById(R.id.edtComentario);
        findViewById(R.id.btnEnviarComentario).setOnClickListener(v -> {
            if (!UsuarioPrefs.estaLogado(this)) {
                Toast.makeText(this, R.string.msg_login_obrigatorio, Toast.LENGTH_LONG).show();
                return;
            }
            String texto = edtComentario.getText() == null ? "" : edtComentario.getText().toString().trim();
            if (texto.isEmpty()) {
                Toast.makeText(this, R.string.msg_comentario_vazio, Toast.LENGTH_SHORT).show();
                return;
            }
            enviarComentario(UsuarioPrefs.obter(this), texto, edtComentario);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        registroComentarios = banco.collection(Post.COLECAO).document(postId).collection("comentarios")
                .orderBy("criadoEm", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, erro) -> {
                    if (erro != null) {
                        Toast.makeText(this, getString(R.string.msg_erro_comentario, erro.getMessage()),
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    List<Comentario> comentarios = new ArrayList<>();
                    if (snapshot != null) {
                        for (QueryDocumentSnapshot doc : snapshot) {
                            try {
                                // ESTIMATE: um comentário recém-enviado ainda sem hora do servidor já aparece com a hora local.
                                comentarios.add(doc.toObject(Comentario.class, ServerTimestampBehavior.ESTIMATE));
                            } catch (RuntimeException erroConversao) {
                                // Um comentário em formato antigo (criadoEm numérico) não pode esconder os demais.
                                Log.e("ComentariosActivity", "Comentário " + doc.getId() + " ignorado", erroConversao);
                            }
                        }
                    }
                    comentariosAtuais = comentarios;
                    adapter.atualizar(comentarios);
                    txtVazioComentarios.setVisibility(comentarios.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    @Override
    protected void onStop() {
        if (registroComentarios != null) registroComentarios.remove();
        super.onStop();
    }

    private void enviarComentario(String autor, String texto, TextInputEditText edtComentario) {
        DocumentReference post = banco.collection(Post.COLECAO).document(postId);
        WriteBatch lote = banco.batch();
        Comentario comentario = new Comentario(autor, texto, comentarioRespondendoId);
        lote.set(post.collection("comentarios").document(), comentario);
        lote.update(post, "comentarios", FieldValue.increment(1));
        lote.commit()
                .addOnSuccessListener(v -> {
                    edtComentario.setText("");
                    comentarioRespondendoId = null;
                })
                .addOnFailureListener(erro -> Toast.makeText(this,
                        getString(R.string.msg_erro_comentario, erro.getMessage()), Toast.LENGTH_LONG).show());
    }

    @Override
    public void responder(Comentario comentario) {
        if (!UsuarioPrefs.estaLogado(this)) {
            Toast.makeText(this, R.string.msg_login_obrigatorio, Toast.LENGTH_LONG).show();
            return;
        }
        comentarioRespondendoId = comentario.getId();
        Toast.makeText(this, "Respondendo a " + comentario.getAutor(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void excluir(Comentario comentario) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir comentário?")
                .setMessage("Isso também apagará as respostas.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (d, w) -> {
                    banco.runTransaction(t -> {
                        DocumentReference post = banco.collection(Post.COLECAO).document(postId);
                        int total = 1;
                        for (Comentario c : comentariosAtuais) {
                            if (comentario.getId().equals(c.getRespondendoA())) total++;
                        }
                        WriteBatch lote = banco.batch();
                        lote.delete(post.collection("comentarios").document(comentario.getId()));
                        for (Comentario c : comentariosAtuais) {
                            if (comentario.getId().equals(c.getRespondendoA())) {
                                lote.delete(post.collection("comentarios").document(c.getId()));
                            }
                        }
                        lote.update(post, "comentarios", FieldValue.increment(-total));
                        return lote.commit();
                    }).addOnFailureListener(e -> Toast.makeText(this,
                            "Erro ao excluir: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }).show();
    }
}
