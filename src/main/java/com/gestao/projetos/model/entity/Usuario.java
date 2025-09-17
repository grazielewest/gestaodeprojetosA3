package com.gestao.projetos.model.entity;

public class Usuario {
    private int id;
    private String nome;
    private String cpf;
    private String email;
    private String cargo;
    private String login;
    private String senha;
    private Perfil perfil;
    private boolean ativo;

    public enum Perfil {
        ADMINISTRADOR, GERENTE, COLABORADOR
    }

    // Construtor padrão
    public Usuario() {
        this.ativo = true;
        this.perfil = Perfil.COLABORADOR;
    }

    // Construtor com parâmetros básicos
    public Usuario(String nome, String email, String login, String senha, Perfil perfil) {
        this.nome = nome;
        this.email = email;
        this.login = login;
        this.senha = senha;
        this.perfil = perfil;
        this.ativo = true;
    }

    // Construtor completo
    public Usuario(int id, String nome, String cpf, String email, String cargo,
                   String login, String senha, Perfil perfil, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.cargo = cargo;
        this.login = login;
        this.senha = senha;
        this.perfil = perfil;
        this.ativo = ativo;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    // Métodos úteis
    public boolean isAdministrador() {
        return Perfil.ADMINISTRADOR.equals(this.perfil);
    }

    public boolean isGerente() {
        return Perfil.GERENTE.equals(this.perfil);
    }

    public boolean isColaborador() {
        return Perfil.COLABORADOR.equals(this.perfil);
    }

    public void ativar() {
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
    }

    // toString para debug
    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                ", login='" + login + '\'' +
                ", perfil=" + perfil +
                ", ativo=" + ativo +
                '}';
    }

    // equals e hashCode baseado no ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return id == usuario.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}