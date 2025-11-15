package br.ufal.ic.p2.myfood.service;

import br.ufal.ic.p2.myfood.exceptions.AtributoException;
import br.ufal.ic.p2.myfood.exceptions.LoginException;
import br.ufal.ic.p2.myfood.exceptions.UsuarioException;
import br.ufal.ic.p2.myfood.models.Usuario;
import br.ufal.ic.p2.myfood.models.DonoDeEmpresa;

import java.io.*;
import java.util.*;

public class UsuariosService {
    private Map<Integer, Usuario> usuariosMap = new HashMap<>();
    private int proximoId = 1;
    private final String ARQUIVO = "usuarios.csv";

    public UsuariosService() {
        carregarUsuarios();
    }

    public void zerar() {
        usuariosMap.clear();
        proximoId = 1;
        salvar();
    }

    public void carregarUsuarios() {
        File arquivo = new File(ARQUIVO);
        if (!arquivo.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            int maiorId = 0;

            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(";");
                if (partes.length < 5) continue;

                int id = Integer.parseInt(partes[0]);
                String nome = partes[1];
                String email = partes[2];
                String senha = partes[3];
                String endereco = partes[4];
                String cpf = partes.length > 5 ? partes[5] : "";

                Usuario u;
                if (cpf != null && !cpf.isEmpty()) {
                    u = new DonoDeEmpresa(id, nome, email, senha, endereco, cpf);
                } else {
                    u = new Usuario(id, nome, email, senha, endereco);
                }

                usuariosMap.put(id, u);
            }

            proximoId = maiorId + 1;
        } catch (IOException | NumberFormatException ex) {
            System.err.println("Erro ao carregar usuarios: " + ex.getMessage());
        }
    }

    public void salvar() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARQUIVO))) {
            for (Usuario u : usuariosMap.values()) {
                String cpf = (u instanceof DonoDeEmpresa) ? ((DonoDeEmpresa) u).getCpf() : "";
                pw.printf("%d;%s;%s;%s;%s;%s%n",
                        u.getId(),
                        u.getNome(),
                        u.getEmail(),
                        u.getSenha(),
                        u.getEndereco(),
                        cpf
                );
            }
        } catch (IOException ex) {
            System.err.println("Erro ao salvar usuarios: " + ex.getMessage());
        }
    }

    public int criarUsuario(String nome, String email, String senha, String endereco) {
        validarUsuario(nome, email, senha, endereco, null, false);

        int id = proximoId++;
        Usuario u = new Usuario(id, nome, email, senha, endereco);
        usuariosMap.put(id, u);
        salvar();
        return id;
    }

    public int criarUsuario(String nome, String email, String senha, String endereco, String cpf) {
        validarUsuario(nome, email, senha, endereco, cpf, true);

        int id = proximoId++;
        Usuario u = new DonoDeEmpresa(id, nome, email, senha, endereco, cpf);
        usuariosMap.put(u.getId(), u);
        salvar();
        return u.getId();
    }

    public int login(String email, String senha) {
        for (Usuario u : usuariosMap.values()) {
            if (u.getEmail().equals(email) && u.getSenha().equals(senha)) {
                return u.getId();
            }
        }
        throw new LoginException("Login ou senha invalidos");
    }

    public String getAtributoUsuario(int id, String atributo) {
        Usuario u = getUsuario(id);

        switch (atributo) {
            case "id": return String.valueOf(u.getId());
            case "nome": return u.getNome();
            case "email": return u.getEmail();
            case "senha": return u.getSenha();
            case "endereco": return u.getEndereco();
            case "cpf":
                if (u instanceof DonoDeEmpresa) return ((DonoDeEmpresa) u).getCpf();
                return "";
            default: throw new AtributoException("Atributo invalido.");
        }
    }

    private Usuario getUsuario(int id) {
        Usuario u = usuariosMap.get(id);
        if (u == null) {
            throw new UsuarioException("Usuario nao cadastrado.");
        }
        return u;
    }

    private void validarUsuario(String nome, String email, String senha, String endereco, String cpf, boolean isDono) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new UsuarioException("Nome invalido");
        }

        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            throw new UsuarioException("Email invalido");
        }

        if (isDono) {
            if (cpf == null || cpf.trim().isEmpty() || cpf.length() != 14) {
                throw new UsuarioException("CPF invalido");
            }
        }

        if (senha == null || senha.trim().isEmpty()) {
            throw new LoginException("Senha invalido");
        }

        if (endereco == null || endereco.trim().isEmpty()) {
            throw new UsuarioException("Endereco invalido");
        }

        if(usuariosMap.values().stream().anyMatch(u -> u.getEmail().equals(email))) {
            throw new LoginException("Conta com esse email ja existe");
        }
    }

    public Map<Integer, Usuario> getUsuariosMap() {
        return usuariosMap;
    }

    // Debugando
    public void listarUsuarios() {
        for (Usuario u : usuariosMap.values()) {
            System.out.println("Usuario ID=" + u.getId() + "Nome= " + u.getNome() + " Email= " + u.getEmail());
        }
    }
}