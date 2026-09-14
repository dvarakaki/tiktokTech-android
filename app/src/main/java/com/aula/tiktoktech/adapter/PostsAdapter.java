package com.aula.tiktoktech.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.tiktoktech.R;
import com.aula.tiktoktech.model.Post;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/** Mostra a lista de posts do feed, um card por foto, carregando a imagem do Cloudinary com Glide. */
public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {

    private final List<Post> posts = new ArrayList<>();

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
        holder.txtLikes.setText(String.valueOf(post.getLikes()));
        holder.txtDislikes.setText(String.valueOf(post.getDislikes()));
        holder.txtComentarios.setText(String.valueOf(post.getComentarios()));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgFoto;
        final TextView txtDescricao;
        final TextView txtLikes;
        final TextView txtDislikes;
        final TextView txtComentarios;

        PostViewHolder(@NonNull View item) {
            super(item);
            imgFoto = item.findViewById(R.id.imgFoto);
            txtDescricao = item.findViewById(R.id.txtDescricao);
            txtLikes = item.findViewById(R.id.txtLikes);
            txtDislikes = item.findViewById(R.id.txtDislikes);
            txtComentarios = item.findViewById(R.id.txtComentarios);
        }
    }
}
