package com.aula.tiktoktech.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.tiktoktech.R;
import com.aula.tiktoktech.UsuarioPrefs;
import com.aula.tiktoktech.model.Post;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/** Mostra a lista de posts do feed, um card por foto, carregando a imagem do Cloudinary com Glide. */
public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {

    /** Ações disparadas pelos botões de cada card, delegadas para quem criou o adapter. */
    public interface Acoes {
        void votar(Post post, String campo);
        void comentar(Post post);
    }

    private final List<Post> posts = new ArrayList<>();
    private final Acoes acoes;

    public PostsAdapter(Acoes acoes) {
        this.acoes = acoes;
    }

    public void atualizar(List<Post> novosPosts) {
        posts.clear();
        posts.addAll(novosPosts);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        Glide.with(holder.imgFoto.getContext())
                .load(post.getUrl())
                .placeholder(R.drawable.fundo_imagem)
                .centerCrop()
                .into(holder.imgFoto);
        holder.txtDescricao.setText(post.getDescricao());
        String autor = post.getAutor();
        holder.txtAutor.setText(autor == null || autor.trim().isEmpty() ? ""
                : holder.itemView.getContext().getString(R.string.rotulo_autor, autor));
        holder.txtAutor.setVisibility(holder.txtAutor.getText().length() == 0 ? View.GONE : View.VISIBLE);
        String meuVoto = post.getVotos().get(UsuarioPrefs.obter(holder.itemView.getContext()));
        holder.btnLike.setImageResource("likes".equals(meuVoto) ? R.drawable.ic_coracao : R.drawable.ic_coracao_contorno);
        holder.btnDislike.setAlpha(meuVoto == null || "dislikes".equals(meuVoto) ? 1f : 0.5f);
        holder.btnLike.setAlpha(meuVoto == null || "likes".equals(meuVoto) ? 1f : 0.5f);
        holder.txtLikes.setText(String.valueOf(post.getLikes()));
        holder.txtDislikes.setText(String.valueOf(post.getDislikes()));
        holder.txtComentarios.setText(String.valueOf(post.getComentarios()));
        holder.btnLike.setOnClickListener(v -> acoes.votar(post, "likes"));
        holder.btnDislike.setOnClickListener(v -> acoes.votar(post, "dislikes"));
        holder.btnComentario.setOnClickListener(v -> acoes.comentar(post));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgFoto;
        final TextView txtDescricao;
        final TextView txtAutor;
        final TextView txtLikes;
        final TextView txtDislikes;
        final TextView txtComentarios;
        final ImageButton btnLike;
        final ImageButton btnDislike;
        final ImageButton btnComentario;

        PostViewHolder(@NonNull View item) {
            super(item);
            imgFoto = item.findViewById(R.id.imgFoto);
            txtDescricao = item.findViewById(R.id.txtDescricao);
            txtAutor = item.findViewById(R.id.txtAutor);
            txtLikes = item.findViewById(R.id.txtLikes);
            txtDislikes = item.findViewById(R.id.txtDislikes);
            txtComentarios = item.findViewById(R.id.txtComentarios);
            btnLike = item.findViewById(R.id.btnLike);
            btnDislike = item.findViewById(R.id.btnDislike);
            btnComentario = item.findViewById(R.id.btnComentario);
        }
    }
}
