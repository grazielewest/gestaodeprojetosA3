package com.gestao.projetos.dao;

import com.gestao.projetos.model.entity.Projeto;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

public class ProjetoMapper {

    public static Projeto mapearBasico(ResultSet rs) throws SQLException {
        Projeto projeto = new Projeto();
        projeto.setId(rs.getInt("id"));
        projeto.setNome(rs.getString("nome"));
        projeto.setDescricao(rs.getString("descricao"));
        projeto.setStatus(rs.getString("status"));

        // Datas
        java.sql.Date dataInicio = rs.getDate("data_inicio");
        if (dataInicio != null) {
            projeto.setDataInicio(dataInicio.toLocalDate());
        }

        java.sql.Date dataFim = rs.getDate("data_fim");
        if (dataFim != null) {
            projeto.setDataFim(dataFim.toLocalDate());
        }

        projeto.setOrcamento(rs.getDouble("orcamento"));
        projeto.setIdResponsavel(rs.getInt("id_responsavel"));
        projeto.setPrioridade(rs.getString("prioridade"));

        // Lista vazia para evitar recursão
        projeto.setEquipes(new ArrayList<>());

        return projeto;
    }

    // Opcional: método completo se necessário
    public static Projeto mapearCompleto(ResultSet rs) throws SQLException {
        Projeto projeto = mapearBasico(rs);
        // Aqui você pode adicionar lógica para carregar relações se necessário
        return projeto;
    }
}