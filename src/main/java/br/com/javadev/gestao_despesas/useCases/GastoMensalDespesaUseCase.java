package br.com.javadev.gestao_despesas.useCases;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.javadev.gestao_despesas.dto.ResumoMensalDespesa;
import br.com.javadev.gestao_despesas.model.Despesa;
import br.com.javadev.gestao_despesas.repository.DespesaRepository;

@Service 
public class GastoMensalDespesaUseCase {
    private final DespesaRepository despesaRepository;

    public GastoMensalDespesaUseCase(DespesaRepository despesaRepository) {
        this.despesaRepository = despesaRepository;
    }

    public ResumoMensalDespesa execute(String email, YearMonth mes) {
        LocalDate inicio = mes.atDay(1);
        LocalDate fim = mes.atEndOfMonth();

        List<Despesa> despesas = despesaRepository.findByEmailAndDataBetween(email, inicio, fim);

        @SuppressWarnings("null")
        BigDecimal total = despesas.stream()
            .map(Despesa::getValor)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return new ResumoMensalDespesa(mes, inicio, fim, total, despesas);
    }
}
