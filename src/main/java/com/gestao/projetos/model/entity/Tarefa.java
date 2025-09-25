package com.gestao.projetos.model.entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Tarefa {
    private int id;
    private String titulo;
    private String descricao;
    private int idProjeto;
    private String nomeProjeto; // Para exibição
    private int idResponsavel;
    private String nomeResponsavel; // Para exibição
    private String status;
    private LocalDate dataInicioPrevista;
    private LocalDate dataFimPrevista;
    private LocalDate dataInicioReal;
    private LocalDate dataFimReal;
    private String prioridade;
    private LocalDate dataCriacao;
    private LocalDate dataAtualizacao;

    // Construtores
    public Tarefa() {
        this.dataCriacao = LocalDate.now();
        this.dataAtualizacao = LocalDate.now();
        this.status = "Pendente";
        this.prioridade = "Média";
    }

    public Tarefa(String titulo, String descricao, int idProjeto, int idResponsavel,
                  String status, LocalDate dataInicioPrevista, LocalDate dataFimPrevista,
                  String prioridade) {
        this();
        this.titulo = titulo;
        this.descricao = descricao;
        this.idProjeto = idProjeto;
        this.idResponsavel = idResponsavel;
        this.status = status;
        this.dataInicioPrevista = dataInicioPrevista;
        this.dataFimPrevista = dataFimPrevista;
        this.prioridade = prioridade;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) {
        this.titulo = titulo;
        this.dataAtualizacao = LocalDate.now();
    }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) {
        this.descricao = descricao;
        this.dataAtualizacao = LocalDate.now();
    }

    public int getIdProjeto() { return idProjeto; }
    public void setIdProjeto(int idProjeto) {
        this.idProjeto = idProjeto;
        this.dataAtualizacao = LocalDate.now();
    }

    public String getNomeProjeto() { return nomeProjeto; }
    public void setNomeProjeto(String nomeProjeto) { this.nomeProjeto = nomeProjeto; }

    public int getIdResponsavel() { return idResponsavel; }
    public void setIdResponsavel(int idResponsavel) {
        this.idResponsavel = idResponsavel;
        this.dataAtualizacao = LocalDate.now();
    }

    public String getNomeResponsavel() {
        return nomeResponsavel != null ? nomeResponsavel : "Não definido";
    }
    public void setNomeResponsavel(String nomeResponsavel) { this.nomeResponsavel = nomeResponsavel; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        this.dataAtualizacao = LocalDate.now();
    }

    public LocalDate getDataInicioPrevista() { return dataInicioPrevista; }
    public void setDataInicioPrevista(LocalDate dataInicioPrevista) {
        this.dataInicioPrevista = dataInicioPrevista;
        this.dataAtualizacao = LocalDate.now();
    }

    public LocalDate getDataFimPrevista() { return dataFimPrevista; }
    public void setDataFimPrevista(LocalDate dataFimPrevista) {
        this.dataFimPrevista = dataFimPrevista;
        this.dataAtualizacao = LocalDate.now();
    }

    public LocalDate getDataInicioReal() { return dataInicioReal; }
    public void setDataInicioReal(LocalDate dataInicioReal) {
        this.dataInicioReal = dataInicioReal;
        this.dataAtualizacao = LocalDate.now();
    }

    public LocalDate getDataFimReal() { return dataFimReal; }
    public void setDataFimReal(LocalDate dataFimReal) {
        this.dataFimReal = dataFimReal;
        this.dataAtualizacao = LocalDate.now();
    }

    public String getPrioridade() { return prioridade; }
    public void setPrioridade(String prioridade) {
        this.prioridade = prioridade;
        this.dataAtualizacao = LocalDate.now();
    }

    public LocalDate getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDate dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDate getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDate dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }

    // Métodos auxiliares para formatação
    public String getDataInicioPrevistaFormatada() {
        return dataInicioPrevista != null ?
                dataInicioPrevista.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
    }

    public String getDataFimPrevistaFormatada() {
        return dataFimPrevista != null ?
                dataFimPrevista.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
    }

    public String getDataInicioRealFormatada() {
        return dataInicioReal != null ?
                dataInicioReal.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
    }

    public String getDataFimRealFormatada() {
        return dataFimReal != null ?
                dataFimReal.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
    }

    public String getDataCriacaoFormatada() {
        return dataCriacao != null ?
                dataCriacao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
    }

    public String getStatusDisplay() {
        switch (status) {
            case "Pendente": return "⏳ " + status;
            case "Em Execução": return "🚀 " + status;
            case "Concluída": return "✅ " + status;
            case "Cancelada": return "❌ " + status;
            default: return status;
        }
    }

    public boolean estaAtrasada() {
        if (dataFimPrevista != null && LocalDate.now().isAfter(dataFimPrevista)) {
            return !"Concluída".equals(status) && !"Cancelada".equals(status);
        }
        return false;
    }

    public int getDiasRestantes() {
        if (dataFimPrevista != null && LocalDate.now().isBefore(dataFimPrevista)) {
            return (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), dataFimPrevista);
        }
        return 0;
    }

    public int getDiasAtraso() {
        if (dataFimPrevista != null && LocalDate.now().isAfter(dataFimPrevista)) {
            return (int) java.time.temporal.ChronoUnit.DAYS.between(dataFimPrevista, LocalDate.now());
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Tarefa{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", projeto='" + nomeProjeto + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}