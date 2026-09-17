package org.example;

public class Recado {
    private final int id;
    private final String autor;
    private final String mensagem;

    public Recado(int id, String autor, String mensagem) {
        this.id = id;
        this.autor = autor;
        this.mensagem = mensagem;
    }

    public int getId() {
        return id;
    }

    public String getAutor() {
        return autor;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String paraJson() {
        return "{\"id\":" + id
                + ", \"autor\":\"" + escapar(autor)
                + "\", \"mensagem\":\"" + escapar(mensagem) + "\"}";
    }

    //tratamento do texto recebido para o formato correto do json
    private String escapar(String texto) {
        return texto.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "").replace("\n", "\\n");
    }
}
