package br.ufal.ic.p2.myfood.service;

import br.ufal.ic.p2.myfood.models.DonoDeEmpresa;
import br.ufal.ic.p2.myfood.models.Produto;
import br.ufal.ic.p2.myfood.models.Usuario;

import java.io.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProdutosService {
    private Map<Integer, Produto> produtosMap = new HashMap<>();
    private int proximoId = 1;
    private final String ARQUIVO = "produtos.csv";
    // referência para verificar donos

    public ProdutosService(Map<Integer, Usuario> usuariosMap) {

    }

    // Zera todas as produtos
    public void zerar() {
        produtosMap.clear();
        proximoId = 1;
        salvar();
    }

    // Carrega produtos do CSV
    public void carregarProdutos() {
        File arquivo = new File(ARQUIVO);
        if (!arquivo.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            int maiorId = 0;

            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(";");
                if (partes.length < 4) continue;

                int id = Integer.parseInt(partes[0]);
                int empresa = Integer.parseInt(partes[1]);
                String nome = partes[2];
                Float valor = Float.parseFloat(partes[3]);
                String categoria = partes[4];

                Produto e = new Produto(id, empresa, nome, valor, categoria);
                produtosMap.put(id, e);

                if (id > maiorId) maiorId = id;
            }

            proximoId = maiorId + 1;

        } catch (IOException | NumberFormatException ex) {
            System.err.println("Erro ao carregar produtos: " + ex.getMessage());
        }
    }

    // Salva produtos no CSV
    public void salvar() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARQUIVO))) {
            for (Produto e : produtosMap.values()) {
                pw.printf("%d;%d;%s;%f;%s%n",
                        e.getId(),
                        e.getEmpresa(),
                        e.getNome(),
                        e.getValor(),
                        e.getCategoria()
                );
            }
        } catch (IOException ex) {
            System.err.println("Erro ao salvar produtos: " + ex.getMessage());
        }
    }

    // Cria uma nova empresa
    public int criarProduto(int empresa, String nome, Float valor, String categoria) {
        // Valida os dados
        validarProduto(empresa, nome, valor, categoria);

        // Cria empresa
        int idProduto = proximoId++;
        Produto p = new Produto(idProduto, empresa, nome, valor, categoria);
        produtosMap.put(idProduto, p);
        salvar();
        return idProduto;
    }

    // Validação de regras
    private void validarProduto(int empresa, String nome, Float valor, String categoria) {
        if (nome == null || nome.trim().isEmpty())
            throw new RuntimeException("Nome invalido");

        if (valor == null || valor <= 0)
            throw new RuntimeException("Valor invalido");

        if (categoria == null || categoria.trim().isEmpty())
            throw new RuntimeException("Categoria invalido");


        boolean mesmoNomeMesmoEmpresa = produtosMap.values().stream()
                .anyMatch(p -> p.getNome().equals(nome)
                        && p.getEmpresa() == empresa);

        if (mesmoNomeMesmoEmpresa) {
            throw new RuntimeException("Ja existe um produto com esse nome para essa empresa");
        }
    }

    public void editarProduto(int produto, String nome, Float valor, String categoria) {
        Produto p = produtosMap.get(produto);
        if (p == null) throw new RuntimeException("Produto nao cadastrado");
        validarProduto(produto, nome, valor, categoria);

        p.setNome(nome);
        p.setValor(valor);
        p.setCategoria(categoria);
        salvar();
    }

    public String getProduto(String nome, int empresa, String atributo) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RuntimeException("Nome invalido");
        }
        if (atributo == null || atributo.trim().isEmpty()) {
            throw new RuntimeException("Atributo invalido");
        }

        // Encontra o produto na empresa pelo nome
        Produto p = produtosMap.values().stream()
                .filter(prod -> prod.getEmpresa() == empresa && prod.getNome().equals(nome))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Produto nao cadastrado"));

        switch (atributo.toLowerCase()) {
            case "id": return String.valueOf(p.getId());
            case "nome": return p.getNome();
            case "valor": return String.valueOf(p.getValor());
            case "categoria": return p.getCategoria();
            case "empresa": return String.valueOf(p.getEmpresa());
            default: throw new RuntimeException("Atributo invalido");
        }
    }
}
