package com.aula.tiktoktech.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.tiktoktech.R;
import com.aula.tiktoktech.model.Comentario;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Mostra a lista de comentários de um post, do mais antigo para o mais recente. */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ComentarioViewHolder> {

    public interface Acoes {
        void responder(Comentario comentario);
        void excluir(Comentario comentario);
    }

    private static final SimpleDateFormat FORMATO_DATA =
            new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());

    private final List<Comentario> comentarios = new ArrayList<>();
    private final Acoes acoes;
    private final boolean souDonoDoPost;
    private final String usuarioAtual;

    public CommentAdapter(Acoes acoes, boolean souDonoDoPost, String usuarioAtual) {
        this.acoes = acoes;
        this.souDonoDoPost = souDonoDoPost;
        this.usuarioAtual = usuarioAtual;
    }

    public void atualizar(List<Comentario> novosComentarios) {
        comentarios.clear();
        comentarios.addAll(novosComentarios);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ComentarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comentario, parent, false);
        return new ComentarioViewHolder(item);
    }

    private static final int NIVEL_MAXIMO_VISUAL = 3;
    private static final int INDENTACAO_BASE_DP = 12;
    private static final int INDENTACAO_POR_NIVEL_DP = 22;

    @Override
    public void onBindViewHolder(@NonNull ComentarioViewHolder holder, int position) {
        Comentario comentario = comentarios.get(position);
        int nivel = comentario.getNivel();
        String prefixo = nivel > 0 ? "↳ " : "";
        holder.txtAutor.setText(prefixo + comentario.getAutor());
        holder.txtTexto.setText(comentario.getTexto());
        Date criadoEm = comentario.getCriadoEm();
        holder.txtData.setText(criadoEm == null ? "" : FORMATO_DATA.format(criadoEm));
        holder.btnResponder.setOnClickListener(v -> acoes.responder(comentario));
        boolean souAutorDoComentario = usuarioAtual != null && usuarioAtual.equals(comentario.getAutor());
        holder.btnExcluir.setVisibility(souDonoDoPost || souAutorDoComentario ? View.VISIBLE : View.GONE);
        holder.btnExcluir.setOnClickListener(v -> acoes.excluir(comentario));

        // Indenta visualmente cada nível de resposta, limitando pra não sumir com a tela em telas pequenas.
        int nivelVisual = Math.min(nivel, NIVEL_MAXIMO_VISUAL);
        float densidade = holder.itemView.getResources().getDisplayMetrics().density;
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
        if (params != null) {
            params.leftMargin = (int) ((INDENTACAO_BASE_DP + INDENTACAO_POR_NIVEL_DP * nivelVisual) * densidade);
            holder.itemView.setLayoutParams(params);
        }
    }

    @Override
    public int getItemCount() {
        return comentarios.size();
    }

    static class ComentarioViewHolder extends RecyclerView.ViewHolder {
        final TextView txtAutor;
        final TextView txtTexto;
        final TextView txtData;
        final Button btnResponder;
        final Button btnExcluir;

        ComentarioViewHolder(@NonNull View item) {
            super(item);
            txtAutor = item.findViewById(R.id.txtAutor);
            txtTexto = item.findViewById(R.id.txtTexto);
            txtData = item.findViewById(R.id.txtData);
            btnResponder = item.findViewById(R.id.btnResponder);
            btnExcluir = item.findViewById(R.id.btnExcluir);
        }
    }
}
