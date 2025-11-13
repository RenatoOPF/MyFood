package br.ufal.ic.p2.myfood.models;

public class Empresa {
    private int id;
    private int dono;
    private String tipoEmpresa;
    private String nome;
    private String endereco;
    private String tipoCozinha;

    public Empresa(int id, int dono, String tipoEmpresa,String nome, String endereco, String tipoCozinha) {
        this.id = id;
        this.dono = dono;
        this.tipoEmpresa = tipoEmpresa;
        this.nome = nome;
        this.endereco = endereco;
        this.tipoCozinha = tipoCozinha;
    }

    public int getId() { return id; }
    public int getDono() { return dono; }
    public String getTipoEmpresa() { return tipoEmpresa; }
    public String getNome() { return nome; }
    public String getEndereco() { return endereco; }
    public String getTipoCozinha() { return tipoCozinha; }
}
