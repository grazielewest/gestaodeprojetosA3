package com.gestao.projetos.model.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Equipe {
    private int id;
    private String nome;
    private String descricao;
    private LocalDate dataCriacao;
    private LocalDate dataAtualizacao;
    private boolean ativa;

    // Lista de membros (usuários)
    private List<Usuario> membros;

    // Lista de projetos associados
    private List<Projeto> projetos;

    public Equipe() {
        this.membros = new ArrayList<>();
        this.projetos = new ArrayList<>();
        this.dataCriacao = LocalDate.now();
        this.ativa = true;
    }

    public Equipe(String nome, String descricao) {
        this();
        this.nome = nome;
        this.descricao = descricao;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDate getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDate dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDate getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDate dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }

    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }

    public List<Usuario> getMembros() { return membros; }
    public void setMembros(List<Usuario> membros) { this.membros = membros; }

    public List<Projeto> getProjetos() { return projetos; }
    public void setProjetos(List<Projeto> projetos) { this.projetos = projetos; }

    // Métodos utilitários
    public void adicionarMembro(Usuario usuario) {
        if (!membros.contains(usuario)) {
            membros.add(usuario);
        }
    }

    public void removerMembro(Usuario usuario) {
        membros.remove(usuario);
    }

    public void adicionarProjeto(Projeto projeto) {
        if (!projetos.contains(projeto)) {
            projetos.add(projeto);
        }
    }

    public void removerProjeto(Projeto projeto) {
        projetos.remove(projeto);
    }

    public int getQuantidadeMembros() {
        return membros.size();
    }

    public int getQuantidadeProjetos() {
        return projetos.size();
    }

    @Override
    public String toString() {
        return nome + " (" + getQuantidadeMembros() + " membros)";
    }
}