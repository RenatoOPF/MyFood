package br.ufal.ic.p2.myfood.models;

public class Produto {
    private int id;
    private int empresa;
    private String nome;
    private Float valor;
    private String categoria;

    public Produto(int id, int empresa, String nome, Float valor, String categoria) {
        this.id = id;
        this.empresa = empresa;
        this.nome = nome;
        this.valor = valor;
        this.categoria = categoria;
    }

    public int getId() { return id; }
    public int getEmpresa() { return empresa; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Float getValor() { return valor; }
    public void setValor(Float valor) { this.valor = valor; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
}
