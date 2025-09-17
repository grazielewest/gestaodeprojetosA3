package com.gestao.projetos.model.entity;

import java.util.Arrays;
import java.util.List;

public class ProjetoConstantes {

    public static final List<String> STATUS = Arrays.asList(
            "Planejamento",
            "Em Andamento",
            "Pausado",
            "Concluído",
            "Cancelado"
    );

    public static final List<String> PRIORIDADES = Arrays.asList(
            "Baixa",
            "Média",
            "Alta",
            "Urgente"
    );

    public static final List<String> CORES_STATUS = Arrays.asList(
            "#17a2b8", // Planejamento - Azul
            "#28a745", // Em Andamento - Verde
            "#ffc107", // Pausado - Amarelo
            "#6c757d", // Concluído - Cinza
            "#dc3545"  // Cancelado - Vermelho
    );
}