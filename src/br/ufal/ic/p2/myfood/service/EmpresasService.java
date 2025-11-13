package br.ufal.ic.p2.myfood.service;

import br.ufal.ic.p2.myfood.models.DonoDeEmpresa;
import br.ufal.ic.p2.myfood.models.Empresa;
import br.ufal.ic.p2.myfood.models.Usuario;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class EmpresasService {

    private Map<Integer, Empresa> empresasMap = new HashMap<>();
    private int proximoId = 1;
    private final String ARQUIVO = "empresas.csv";

    private Map<Integer, Usuario> usuariosMap; // referência para verificar donos

    public EmpresasService(Map<Integer, Usuario> usuariosMap) {
        this.usuariosMap = usuariosMap;
        carregarEmpresas();
    }

    // Zera todas as empresas
    public void zerar() {
        empresasMap.clear();
        proximoId = 1;
        salvar();
    }

    // Carrega empresas do CSV
    public void carregarEmpresas() {
        File arquivo = new File(ARQUIVO);
        if (!arquivo.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            int maiorId = 0;

            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(";");
                if (partes.length < 5) continue;

                int id = Integer.parseInt(partes[0]);
                int dono = Integer.parseInt(partes[1]);
                String nome = partes[2];
                String endereco = partes[3];
                String tipoCozinha = partes[4];
                String tipoEmpresa = partes.length > 5 ? partes[5] : "restaurante";

                Empresa e = new Empresa(id, dono, tipoEmpresa, nome, endereco, tipoCozinha);
                empresasMap.put(id, e);

                if (id > maiorId) maiorId = id;
            }

            proximoId = maiorId + 1;

        } catch (IOException | NumberFormatException ex) {
            System.err.println("Erro ao carregar empresas: " + ex.getMessage());
        }
    }

    // Salva empresas no CSV
    public void salvar() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARQUIVO))) {
            for (Empresa e : empresasMap.values()) {
                pw.printf("%d;%d;%s;%s;%s;%s%n",
                        e.getId(),
                        e.getDono(),
                        e.getNome(),
                        e.getEndereco(),
                        e.getTipoCozinha(),
                        e.getTipoEmpresa()
                );
            }
        } catch (IOException ex) {
            System.err.println("Erro ao salvar empresas: " + ex.getMessage());
        }
    }

    // Cria uma nova empresa
    public int criarEmpresa(String tipoEmpresa, int dono, String nome, String endereco, String tipoCozinha) {
        // Verifica se o usuário é um DonoDeEmpresa
        Usuario donoUsuario = usuariosMap.get(dono);
        if (!(donoUsuario instanceof DonoDeEmpresa)) {
            throw new RuntimeException("Usuario nao pode criar uma empresa");
        }

        // Valida os dados
        validarEmpresa(tipoEmpresa, dono, nome, endereco, tipoCozinha);

        // Cria empresa
        int idEmpresa = proximoId++;
        Empresa empresa = new Empresa(idEmpresa, dono, tipoEmpresa, nome, endereco, tipoCozinha);
        empresasMap.put(idEmpresa, empresa);
        salvar();
        return idEmpresa;
    }

    // Validação de regras
    private void validarEmpresa(String tipoEmpresa, int dono, String nome, String endereco, String tipoCozinha) {
        if (tipoEmpresa == null || tipoEmpresa.trim().isEmpty())
            throw new RuntimeException("Nome da empresa invalido");

        if (endereco == null || endereco.trim().isEmpty())
            throw new RuntimeException("Endereco invalido");

        if (tipoCozinha == null || tipoCozinha.trim().isEmpty())
            throw new RuntimeException("Tipo de cozinha invalido");

        // Verifica se outro dono já tem empresa com o mesmo nome
        boolean mesmoNomeMesmoEnderecoMesmoDono = empresasMap.values().stream()
                .anyMatch(e -> e.getNome().equals(nome)
                        && e.getEndereco().equals(endereco)
                        && e.getDono() == dono);

        boolean mesmoNomeOutroDono = empresasMap.values().stream()
                .anyMatch(e -> e.getNome().equals(nome) && e.getDono() != dono);

        if (mesmoNomeMesmoEnderecoMesmoDono) {
            throw new RuntimeException("Proibido cadastrar duas empresas com o mesmo nome e local");
        } else if (mesmoNomeOutroDono) {
            throw new RuntimeException("Empresa com esse nome ja existe");
        }
    }

    // Lista empresas de um dono
    public List<Empresa> getEmpresasDoUsuario(int dono) {
        return empresasMap.values().stream()
                .filter(e -> e.getId() == dono)
                .collect(Collectors.toList());
    }

    public String getEmpresasDoUsuarioFormatado(int idDono) {
        Usuario usuario = usuariosMap.get(idDono);
        if (!(usuario instanceof DonoDeEmpresa)) {
            throw new RuntimeException("Usuario nao pode criar uma empresa");
        }

        List<Empresa> empresasDoDono = empresasMap.values().stream()
                .filter(e -> e.getDono() == idDono)
                .sorted(Comparator.comparingInt(Empresa::getId))
                .toList();

        StringBuilder sb = new StringBuilder();
        sb.append("{["); // início

        for (int i = 0; i < empresasDoDono.size(); i++) {
            Empresa e = empresasDoDono.get(i);
            sb.append("[")
                    .append(e.getNome()).append(", ").append(e.getEndereco())
                    .append("]");
            if (i < empresasDoDono.size() - 1) {
                sb.append(", ");
            }
        }

        sb.append("]}"); // fechamento
        return sb.toString();
    }

    public String getAtributoEmpresa(int empresa, String atributo) {
        Empresa e = getEmpresa(empresa);
        if (e == null) throw new RuntimeException("Empresa nao cadastrada");

        if (atributo == null || atributo.trim().isEmpty()) {
            throw new RuntimeException("Atributo invalido");
        }

        switch (atributo) {
            case "id": return String.valueOf(e.getId());
            case "TipoEmpresa": return e.getTipoEmpresa();
            case "nome": return e.getNome();
            case "endereco": return e.getEndereco();
            case "tipoCozinha": return e.getTipoCozinha();
            case "dono":
                Usuario dono = usuariosMap.get(e.getDono());
                return dono.getNome();
            default:
                throw new RuntimeException("Atributo invalido");
        }
    }

    private Empresa getEmpresa(int id) {
        Empresa e = empresasMap.get(id);
        if (e == null) throw new RuntimeException("Empresa nao cadastrada");
        return e;
    }

    public int getIdEmpresa(int idDono, String nome, int indice) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RuntimeException("Nome invalido");
        }

        if (indice < 0) {
            throw new RuntimeException("Indice invalido");
        }

        // Filtra empresas do dono com o nome informado
        List<Empresa> filtradas = empresasMap.values().stream()
                .filter(e -> e.getDono() == idDono && e.getNome().equals(nome))
                .toList();

        if (filtradas.isEmpty()) {
            throw new RuntimeException("Nao existe empresa com esse nome");
        }

        if (indice >= filtradas.size()) {
            throw new RuntimeException("Indice maior que o esperado");
        }

        return filtradas.get(indice).getId();
    }

}