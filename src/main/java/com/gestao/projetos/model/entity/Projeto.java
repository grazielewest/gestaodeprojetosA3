package com.gestao.projetos.model.entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Projeto {
    private int id;
    private String nome;
    private String descricao;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String status;
    private int idResponsavel;
    private String nomeResponsavel; // Para exibição na interface
    private double orcamento;
    private String prioridade;
    private LocalDate dataCriacao;
    private LocalDate dataAtualizacao;

    // Construtores
    public Projeto() {
        this.dataCriacao = LocalDate.now();
        this.dataAtualizacao = LocalDate.now();
        this.status = "Planejamento";
        this.prioridade = "Média";
    }

    public Projeto(String nome, String descricao, LocalDate dataInicio, LocalDate dataFim,
                   String status, int idResponsavel, double orcamento, String prioridade) {
        this();
        this.nome = nome;
        this.descricao = descricao;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.status = status;
        this.idResponsavel = idResponsavel;
        this.orcamento = orcamento;
        this.prioridade = prioridade;
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
        this.dataAtualizacao = LocalDate.now();
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
        this.dataAtualizacao = LocalDate.now();
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
        this.dataAtualizacao = LocalDate.now();
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
        this.dataAtualizacao = LocalDate.now();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.dataAtualizacao = LocalDate.now();
    }

    public int getIdResponsavel() {
        return idResponsavel;
    }

    public void setIdResponsavel(int idResponsavel) {
        this.idResponsavel = idResponsavel;
        this.dataAtualizacao = LocalDate.now();
    }

    public String getNomeResponsavel() {
        return nomeResponsavel;
    }

    public void setNomeResponsavel(String nomeResponsavel) {
        this.nomeResponsavel = nomeResponsavel;
    }

    public double getOrcamento() {
        return orcamento;
    }

    public void setOrcamento(double orcamento) {
        this.orcamento = orcamento;
        this.dataAtualizacao = LocalDate.now();
    }

    public String getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(String prioridade) {
        this.prioridade = prioridade;
        this.dataAtualizacao = LocalDate.now();
    }

    public LocalDate getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDate dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDate getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDate dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    // Métodos auxiliares
    public String getDataInicioFormatada() {
        if (dataInicio != null) {
            return dataInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        return "";
    }

    public String getDataFimFormatada() {
        if (dataFim != null) {
            return dataFim.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        return "";
    }

    public String getDataCriacaoFormatada() {
        if (dataCriacao != null) {
            return dataCriacao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        return "";
    }

    public String getDataAtualizacaoFormatada() {
        if (dataAtualizacao != null) {
            return dataAtualizacao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        return "";
    }

    public boolean estaAtrasado() {
        if (dataFim != null && LocalDate.now().isAfter(dataFim)) {
            return !"Concluído".equals(status) && !"Cancelado".equals(status);
        }
        return false;
    }

    public int getDiasRestantes() {
        if (dataFim != null && LocalDate.now().isBefore(dataFim)) {
            return (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), dataFim);
        }
        return 0;
    }

    public int getDiasAtraso() {
        if (dataFim != null && LocalDate.now().isAfter(dataFim)) {
            return (int) java.time.temporal.ChronoUnit.DAYS.between(dataFim, LocalDate.now());
        }
        return 0;
    }

    public String getSituacao() {
        if ("Concluído".equals(status)) {
            return "Concluído";
        } else if ("Cancelado".equals(status)) {
            return "Cancelado";
        } else if (estaAtrasado()) {
            return "Atrasado";
        } else {
            return "No Prazo";
        }
    }

    // Métodos para uso na TableView
    public String getStatusDisplay() {
        return status + (estaAtrasado() ? " ⚠️" : "");
    }

    public String getOrcamentoFormatado() {
        return String.format("R$ %.2f", orcamento);
    }

    // toString para debug
    @Override
    public String toString() {
        return "Projeto{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", status='" + status + '\'' +
                ", dataInicio=" + getDataInicioFormatada() +
                ", dataFim=" + getDataFimFormatada() +
                '}';
    }

    // equals e hashCode para comparação
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Projeto projeto = (Projeto) o;
        return id == projeto.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}