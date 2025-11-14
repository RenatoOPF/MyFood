package br.ufal.ic.p2.myfood;

import java.util.List;

import br.ufal.ic.p2.myfood.service.EmpresasService;
import br.ufal.ic.p2.myfood.service.ProdutosService;
import br.ufal.ic.p2.myfood.service.UsuariosService;

public class Facade {
    private UsuariosService usuariosService;
    private EmpresasService empresasService;
    private ProdutosService produtosService;

    public Facade() {
        usuariosService = new UsuariosService();
        usuariosService.carregarUsuarios();
        empresasService = new EmpresasService(usuariosService.getUsuariosMap());
        empresasService.carregarEmpresas();
        produtosService = new ProdutosService(usuariosService.getUsuariosMap());
        produtosService.carregarProdutos();
    }

    public void zerarSistema() {
        usuariosService.zerar();
        empresasService.zerar();
        produtosService.zerar();
    }

    public void encerrarSistema() {
        usuariosService.salvar();
        empresasService.salvar();
        produtosService.salvar();
    }

    public int criarUsuario(String nome, String email, String senha, String endereco) {
        return usuariosService.criarUsuario(nome, email, senha, endereco);
    }

    public int criarUsuario(String nome, String email, String senha, String endereco, String cpf) {
        return usuariosService.criarUsuario(nome, email, senha, endereco, cpf);
    }

    public int login(String email, String senha) {
        return usuariosService.login(email, senha);
    }

    public String getAtributoUsuario(int id, String atributo) {
        return usuariosService.getAtributoUsuario(id, atributo);
    }

    public int criarEmpresa(String tipoEmpresa, int dono, String nome, String endereco, String tipoCozinha) {
        return empresasService.criarEmpresa(tipoEmpresa, dono, nome, endereco, tipoCozinha);
    }

    public String getEmpresasDoUsuario(int idDono) {
        return empresasService.getEmpresasDoUsuarioFormatado(idDono);
    }

    public String getAtributoEmpresa(int empresa, String atributo) {
        return empresasService.getAtributoEmpresa(empresa, atributo);
    }

    public int getIdEmpresa(int idDono, String nome, int indice) {
        return empresasService.getIdEmpresa(idDono, nome, indice);
    }

    public int criarProduto(int empresa, String nome, Float valor, String categoria) {
        return produtosService.criarProduto(empresa, nome, valor, categoria);
    }

    public void editarProduto(int produto, String nome, Float valor, String categoria) {
        produtosService.editarProduto(produto, nome, valor, categoria);
    }

    public String getProduto(String nome, int empresa, String atributo) {
        return produtosService.getProduto(nome, empresa, atributo);
    }

    public List<String> listarProdutos(int empresa) {
        return null;
    }

    public int criarPedido(int cliente, int empresa) {
        return 0;
    }

    public void adicionarProduto(int numero, int produto) {

    }

    public String getPedidos(int numero, String atributo) {
        return null;
    }

    public void fecharPedido(int numero) {
    }

    public void removerProduto(int pedido, String produto) {
    }

    public int getNumeroPedido(int cliente, int empresa, int indice) {
        return 0;
    }

}
