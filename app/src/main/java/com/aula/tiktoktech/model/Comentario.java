package com.aula.tiktoktech.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Comentario {
    @DocumentId
    private String id;
    private String autor;
    private String texto;
    private String respondendoA;
    /** Não vai pro Firestore: calculado no app pra indentar visualmente a resposta. */
    private int nivel;
    /** Tipo Date (Timestamp no Firestore); nulo ao gravar, o servidor preenche com a hora dele. */
    @ServerTimestamp
    private Date criadoEm;

    /** Construtor vazio exigido pelo Firestore. */
    public Comentario() {
    }

    public Comentario(String autor, String texto) {
        this.autor = autor;
        this.texto = texto;
    }

    public Comentario(String autor, String texto, String respondendoA) {
        this.autor = autor;
        this.texto = texto;
        this.respondendoA = respondendoA;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public Date getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Date criadoEm) {
        this.criadoEm = criadoEm;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRespondendoA() {
        return respondendoA;
    }

    public void setRespondendoA(String respondendoA) {
        this.respondendoA = respondendoA;
    }

    @Exclude
    public int getNivel() {
        return nivel;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

}
