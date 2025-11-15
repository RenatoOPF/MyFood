package br.ufal.ic.p2.myfood.service;

import br.ufal.ic.p2.myfood.exceptions.AtributoException;
import br.ufal.ic.p2.myfood.exceptions.PedidoException;
import br.ufal.ic.p2.myfood.exceptions.ProdutoException;
import br.ufal.ic.p2.myfood.models.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class PedidosService {
    private Map<Integer, Pedido> pedidosMap = new HashMap<>();
    private int proximoId = 1;
    private final String ARQUIVO = "pedidos.csv";
    private Map<Integer, Usuario> usuariosMap;
    private Map<Integer, Empresa> empresasMap;
    private Map<Integer, Produto> produtosMap;

    public PedidosService(Map<Integer, Usuario> usuariosMap,  Map<Integer, Empresa> empresasMap, Map<Integer, Produto> produtosMap) {
        this.usuariosMap = usuariosMap;
        this.empresasMap = empresasMap;
        this.produtosMap = produtosMap;
        carregarPedidos();
    }

    public void zerar() {
        pedidosMap.clear();
        proximoId = 1;
        salvar();
    }

    // Carrega produtos do CSV
    public void carregarPedidos() {
        File arquivo = new File(ARQUIVO);
        if (!arquivo.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {

            String linha;
            int maiorId = 0;

            while ((linha = br.readLine()) != null) {
                String[] p = linha.split(";");

                int numero = Integer.parseInt(p[0]);
                int cliente = Integer.parseInt(p[1]);
                int empresa = Integer.parseInt(p[2]);
                String estado = p[3];

                Pedido pedido = new Pedido(numero, cliente, empresa);
                pedido.setEstado(estado);

                // produtos do pedido
                if (p.length >= 5 && !p[4].isEmpty()) {
                    String[] ids = p[4].split(",");
                    for (String idProd : ids) {
                        Produto prod = produtosMap.get(Integer.parseInt(idProd));
                        if (prod != null) pedido.addProduto(prod);
                    }
                }

                pedidosMap.put(numero, pedido);
                maiorId = Math.max(maiorId, numero);
            }

            proximoId = maiorId + 1;

        } catch (Exception e) {
            throw new PedidoException("Erro ao carregar pedidos");
        }
    }

    // Salva pedidos no CSV
    public void salvar() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARQUIVO))) {

            for (Pedido p : pedidosMap.values()) {

                String listaProdutos = p.getProdutos().stream()
                        .map(prod -> String.valueOf(prod.getId()))
                        .collect(Collectors.joining(","));

                pw.printf("%d;%d;%d;%s;%s%n",
                        p.getNumero(),
                        p.getCliente(),
                        p.getEmpresa(),
                        p.getEstado(),
                        listaProdutos
                );
            }

        } catch (IOException ex) {
            throw new PedidoException("Erro ao salvar pedidos");
        }
    }

    public int criarPedido(int cliente, int empresa) {
        validarPedido(cliente, empresa);

        // Cria o pedido
        int numero = proximoId++;
        Pedido p = new Pedido(numero, cliente, empresa);
        pedidosMap.put(numero, p);
        salvar();
        return numero;
    }

    public void adicionarProduto(int numero, int produto) {
        validarProduto(numero, produto);
        Pedido pedido = pedidosMap.get(numero);
        Produto p = produtosMap.get(produto);
        pedido.addProduto(p);
        salvar();
    }

    public String getPedidos(int numero, String atributo) {
        Pedido pedido = pedidosMap.get(numero);
        if (atributo == null || atributo.trim().isEmpty()) {
            throw new AtributoException("Atributo invalido");
        }
        switch (atributo) {
            case "numero":
                return String.valueOf(pedido.getNumero());
            case "cliente":
                Usuario usuario = usuariosMap.get(pedido.getCliente());
                return usuario.getNome();
            case "empresa":
                Empresa empresa = empresasMap.get(pedido.getEmpresa());
                return empresa.getNome();
            case "estado":
                return pedido.getEstado();
            case "produtos":
                List<Produto> produtos = pedido.getProdutos();

                String lista = produtos.stream()
                        .map(Produto::getNome)   // pega apenas o nome
                        .collect(Collectors.joining(", ")); // separa por vírgula

                return "{[" + lista + "]}";
            case "valor":
                return String.format("%.2f", pedido.getValor());
            default:
                throw new AtributoException("Atributo nao existe");
        }
    }

    public void fecharPedido(int numero) {
        Pedido pedido = pedidosMap.get(numero);
        if  (pedido == null) {
            throw  new PedidoException("Pedido nao encontrado");
        }
        pedido.setEstado("preparando");
        salvar();
    }

    public void removerProduto(int idPedido, String nomeProduto) {
        if (nomeProduto == null || nomeProduto.trim().isEmpty()) {
            throw new ProdutoException("Produto invalido");
        }

        Pedido pedido = pedidosMap.get(idPedido);
        if (pedido.getEstado().equals("preparando")) {
            throw new PedidoException("Nao e possivel remover produtos de um pedido fechado");
        }
        Produto p = getProdutoPorNome(nomeProduto, idPedido);
        if (p != null) {
            pedido.removeProduto(p);
        } else {
            throw  new ProdutoException("Produto nao encontrado");
        }
        salvar();
    }

    public int getNumeroPedido(int cliente, int empresa, int indice) {
        List<Pedido> pedidos = pedidosMap.values().stream()
                .filter(p -> p.getCliente() == cliente && p.getEmpresa() == empresa)
                .sorted(Comparator.comparingInt(Pedido::getNumero)) // mais antigo primeiro
                .toList();

        return pedidos.get(indice).getNumero();
    }

    public void validarPedido(int cliente, int empresa) {
        Usuario dono = usuariosMap.get(cliente);

        if (dono instanceof DonoDeEmpresa) throw new PedidoException("Dono de empresa nao pode fazer um pedido");

        for (Pedido p : pedidosMap.values()) {
            // só interessa pedidos ainda abertos
            if (!"aberto".equals(p.getEstado())) continue;

            // um cliente só pode ter um pedido aberto em uma empresa
            if (p.getCliente() == cliente && p.getEmpresa() == empresa) {
                throw new PedidoException("Nao e permitido ter dois pedidos em aberto para a mesma empresa");
            }
        }
    }

    public void validarProduto(int numero, int idProduto) {
        Pedido pedido = pedidosMap.get(numero);
        Produto produto = produtosMap.get(idProduto);

        if (pedido == null) {
            throw new PedidoException("Nao existe pedido em aberto");
        }

        if (pedido.getEstado().equals("preparando")) {
            throw new PedidoException("Nao e possivel adcionar produtos a um pedido fechado");
        }

        if (pedido.getEmpresa() != produto.getEmpresa()) {
            throw new ProdutoException("O produto nao pertence a essa empresa");
        }
    }

    public Produto getProdutoPorNome(String nome, int pedido) {
        Pedido p = pedidosMap.get(pedido);
        List<Produto> produtos;

        produtos = p.getProdutos();
        for (Produto produto : produtos) {
            if (produto.getNome().equals(nome)) {
                return produto;
            }
        }
        return null;
    }
}
