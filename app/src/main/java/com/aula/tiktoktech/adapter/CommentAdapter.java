package com.aula.tiktoktech.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private static final SimpleDateFormat FORMATO_DATA =
            new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());

    private final List<Comentario> comentarios = new ArrayList<>();

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

    @Override
    public void onBindViewHolder(@NonNull ComentarioViewHolder holder, int position) {
        Comentario comentario = comentarios.get(position);
        holder.txtAutor.setText(comentario.getAutor());
        holder.txtTexto.setText(comentario.getTexto());
        Date criadoEm = comentario.getCriadoEm();
        holder.txtData.setText(criadoEm == null ? "" : FORMATO_DATA.format(criadoEm));
    }

    @Override
    public int getItemCount() {
        return comentarios.size();
    }

    static class ComentarioViewHolder extends RecyclerView.ViewHolder {
        final TextView txtAutor;
        final TextView txtTexto;
        final TextView txtData;

        ComentarioViewHolder(@NonNull View item) {
            super(item);
            txtAutor = item.findViewById(R.id.txtAutor);
            txtTexto = item.findViewById(R.id.txtTexto);
            txtData = item.findViewById(R.id.txtData);
        }
    }
}
