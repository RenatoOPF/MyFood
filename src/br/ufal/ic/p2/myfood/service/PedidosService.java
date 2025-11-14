package br.ufal.ic.p2.myfood.service;

import br.ufal.ic.p2.myfood.models.*;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                String[] partes = linha.split(";");
                if (partes.length < 4) continue;

                int id = Integer.parseInt(partes[0]);
                int cliente = Integer.parseInt(partes[1]);
                int empresa = Integer.parseInt(partes[2]);
                String estado = partes[3];
                Float valor = Float.parseFloat(partes[4]);

                Pedido p = new Pedido(id, cliente, empresa);
                pedidosMap.put(id, p);

                if (id > maiorId) maiorId = id;
            }

            proximoId = maiorId + 1;

        } catch (IOException | NumberFormatException ex) {
            System.err.println("Erro ao carregar pedidos: " + ex.getMessage());
        }
    }

    // Salva pedidos no CSV
    public void salvar() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARQUIVO))) {
            for (Pedido p : pedidosMap.values()) {
                pw.printf("%d;%d;%d;%s;%f%n",
                        p.getNumero(),
                        p.getCliente(),
                        p.getEmpresa(),
                        p.getEstado(),
                        p.getValor()
                );
            }
        } catch (IOException ex) {
            System.err.println("Erro ao salvar pedidos: " + ex.getMessage());
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
    }

    public String getPedidos(int numero, String atributo) {
        Pedido pedido = pedidosMap.get(numero);
        if (atributo == null || atributo.trim().isEmpty()) {
            throw new RuntimeException("Atributo invalido");
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
                throw new RuntimeException("Atributo nao existe");
        }
    }

    public void fecharPedido(int numero) {
        Pedido pedido = pedidosMap.get(numero);
        if  (pedido == null) {
            throw  new RuntimeException("Pedido nao encontrado");
        }
        pedido.setEstado("preparando");
    }

    public void removerProduto(int pedido, String produto) {
        Pedido p = pedidosMap.get(pedido);

    }

    public int getNumeroPedido(int cliente, int empresa, int indice) {
        return 0;
    }

    public void validarPedido(int cliente, int empresa) {
        Usuario dono = usuariosMap.get(cliente);

        if (dono instanceof DonoDeEmpresa) throw new RuntimeException("Dono de empresa nao pode fazer um pedido");

        for(Pedido p : pedidosMap.values()) {
            if(p.getEmpresa() == empresa) {
                throw new RuntimeException("Nao e permitido ter dois pedidos em aberto para a mesma empresa");
            }
        }
    }

    public void validarProduto(int numero, int idProduto) {
        Pedido pedido = pedidosMap.get(numero);
        Produto produto = produtosMap.get(idProduto);

        if (pedido.getEstado().equals("preparando")) {
            throw new RuntimeException("Nao e possivel adcionar produtos a um pedido fechado");
        }

        if (pedido == null) {
            throw new RuntimeException("Nao existe pedido em aberto");
        }
        if (pedido.getEmpresa() != produto.getEmpresa()) {
            throw new RuntimeException("O produto nao pertence a essa empresa");
        }
    }
}
