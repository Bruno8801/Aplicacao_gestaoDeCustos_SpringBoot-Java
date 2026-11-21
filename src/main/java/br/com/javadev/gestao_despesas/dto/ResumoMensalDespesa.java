package br.com.javadev.gestao_despesas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import br.com.javadev.gestao_despesas.model.Despesa;

public record ResumoMensalDespesa(
    YearMonth mes,
    LocalDate inicio,
    LocalDate fim,
    BigDecimal total,
    List<Despesa> despesas
    
) {}
