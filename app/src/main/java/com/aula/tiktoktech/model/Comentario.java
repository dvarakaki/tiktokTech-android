package com.aula.tiktoktech.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Comentario {
    private String autor;
    private String texto;
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

}
