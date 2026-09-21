package com.aula.tiktoktech.model;

import com.google.firebase.firestore.DocumentId;

import java.util.HashMap;
import java.util.Map;

public class Post {

    /** Coleção do Firestore usada por este feed (turma 2D). */
    public static final String COLECAO = "POSTS_2D";

    @DocumentId
    private String id;
    private String url;
    private String descricao;
    private String autor;
    private long likes;
    private long dislikes;
    private long comentarios;

    private long criadoEm;

    /** Quem já votou: login -> "likes" ou "dislikes". Cada usuário só pode votar uma vez. */
    private Map<String, String> votos = new HashMap<>();


    public Post() {
    }

    public Post(String url, String descricao) {
        this.url = url;
        this.descricao = descricao;
        this.likes = 0;
        this.dislikes = 0;
        this.comentarios = 0;
        this.criadoEm = System.currentTimeMillis();
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public Map<String, String> getVotos() {
        return votos;
    }

    public void setVotos(Map<String, String> votos) {
        this.votos = votos == null ? new HashMap<>() : votos;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public long getLikes() {
        return likes;
    }

    public void setLikes(long likes) {
        this.likes = likes;
    }

    public long getDislikes() {
        return dislikes;
    }

    public void setDislikes(long dislikes) {
        this.dislikes = dislikes;
    }

    public long getComentarios() {
        return comentarios;
    }

    public void setComentarios(long comentarios) {
        this.comentarios = comentarios;
    }

    public long getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(long criadoEm) {
        this.criadoEm = criadoEm;
    }
}

