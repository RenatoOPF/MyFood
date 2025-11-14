package br.ufal.ic.p2.myfood.models;

import java.util.ArrayList;
import java.util.List;

public class Pedido {
    private int numero;
    private int cliente;
    private int empresa;
    private String estado;
    private List<Produto> produtos;
    private float valor;

    public Pedido(int numero, int cliente, int empresa) {
        this.numero = numero;
        this.cliente = cliente;
        this.empresa = empresa;
        this.estado = "aberto";
        this.produtos = new ArrayList<>();
    }

    public int getNumero() { return numero; }
    public int getCliente() { return cliente; }
    public int getEmpresa() { return empresa; }
    public String getEstado() { return estado; }
    public List<Produto> getProdutos() { return produtos; }

    public float getValor() {
        for (Produto produto : produtos) {
            valor += produto.getValor();
        }
        return valor;
    }

    public void addProduto(Produto produto) {
        produtos.add(produto);
    }
    public void removeProduto(Produto produto) {
        produtos.remove(produto);
    }
    public void setEstado(String estado) {
        this.estado = estado;
    }
}
