package com.aula.tiktoktech.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

    /**
     * Lê um documento do feed da turma tolerando formatos diferentes: o firestore é compartilhado e
     * outros apps gravam criadoEm como Timestamp (ou em criadoEmMillis), "usuario" no lugar de
     * "autor" e listas likesPor/dislikesPor no lugar dos contadores.
     */
    public static Post de(DocumentSnapshot doc) {
        Post post = new Post();
        post.id = doc.getId();
        post.url = doc.getString("url");
        post.descricao = doc.getString("descricao");
        String autor = doc.getString("autor");
        post.autor = autor != null && !autor.trim().isEmpty() ? autor : doc.getString("usuario");
        post.likes = contador(doc, "likes", "likesPor");
        post.dislikes = contador(doc, "dislikes", "dislikesPor");
        post.comentarios = numero(doc.get("comentarios"));
        post.criadoEm = milissegundos(doc.get("criadoEm"), doc.get("criadoEmMillis"));
        Object votos = doc.get("votos");
        if (votos instanceof Map) {
            for (Map.Entry<?, ?> voto : ((Map<?, ?>) votos).entrySet()) {
                post.votos.put(String.valueOf(voto.getKey()), String.valueOf(voto.getValue()));
            }
        }
        return post;
    }

    private static long numero(Object valor) {
        return valor instanceof Number ? ((Number) valor).longValue() : 0;
    }

    private static long contador(DocumentSnapshot doc, String campo, String campoLista) {
        Object valor = doc.get(campo);
        if (valor instanceof Number) return ((Number) valor).longValue();
        Object lista = doc.get(campoLista);
        return lista instanceof List ? ((List<?>) lista).size() : 0;
    }

    private static long milissegundos(Object criadoEm, Object criadoEmMillis) {
        if (criadoEm instanceof Timestamp) return ((Timestamp) criadoEm).toDate().getTime();
        if (criadoEm instanceof Date) return ((Date) criadoEm).getTime();
        if (criadoEm instanceof Number) return ((Number) criadoEm).longValue();
        return numero(criadoEmMillis);
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

