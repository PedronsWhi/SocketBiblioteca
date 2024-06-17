package Server;

import java.io.Serializable;

public class Livro implements Serializable {
    private String escritor;
    private String titulo;
    private String categoria;
    private int quantidade;

    public Livro(String escritor, String titulo, String categoria, int quantidade) {
        this.escritor = escritor;
        this.titulo = titulo;
        this.categoria = categoria;
        this.quantidade = quantidade;
    }

    public String getEscritor() {
        return escritor;
    }

    public void setEscritor(String escritor) {
        this.escritor = escritor;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    @Override
    public String toString() {
        return "Livro{" +
                "escritor='" + escritor + '\'' +
                ", titulo='" + titulo + '\'' +
                ", categoria='" + categoria + '\'' +
                ", quantidade=" + quantidade +
                '}';
    }
}