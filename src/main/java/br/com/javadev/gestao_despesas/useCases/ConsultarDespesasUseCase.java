package br.com.javadev.gestao_despesas.useCases;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.javadev.gestao_despesas.model.Despesa;
import br.com.javadev.gestao_despesas.repository.DespesaRepository;

@Service
public class ConsultarDespesasUseCase {

    private final DespesaRepository despesaRepository;

    public ConsultarDespesasUseCase(DespesaRepository despesaRepository) {
        this.despesaRepository = despesaRepository;
    }

    @Transactional(readOnly = true)
    public Page<Despesa> pesquisar(
            String email,
            String categoria,
            LocalDate inicio,
            LocalDate fim,
            Pageable pageable) {
        return despesaRepository.pesquisar(email, categoria, inicio, fim, pageable);
    }

    @Transactional(readOnly = true)
    public BigDecimal somar(
            String email,
            String categoria,
            LocalDate inicio,
            LocalDate fim) {
        return despesaRepository.somarValoresFiltrados(email, categoria, inicio, fim);
    }

    @Transactional(readOnly = true)
    public long contar(
            String email,
            String categoria,
            LocalDate inicio,
            LocalDate fim) {
        return despesaRepository.contarFiltradas(email, categoria, inicio, fim);
    }
}
