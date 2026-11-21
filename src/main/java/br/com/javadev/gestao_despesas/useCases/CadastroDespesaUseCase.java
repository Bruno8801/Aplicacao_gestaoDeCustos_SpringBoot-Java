package br.com.javadev.gestao_despesas.useCases;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;

import br.com.javadev.gestao_despesas.model.Despesa;
import br.com.javadev.gestao_despesas.repository.DespesaRepository;

@Service 
public class CadastroDespesaUseCase {
    // SOLID = Single Responsability Princeple
    // cada classe deve ser unica e isolada

    private final DespesaRepository despesaRepository;

    public CadastroDespesaUseCase(DespesaRepository despesaRepository) {
        this.despesaRepository = despesaRepository;
    }

    @CacheEvict(cacheNames = "gastosPorEmailcache", allEntries = true)
    public Despesa execute(Despesa despesa) {
        if(despesa.getCategoria() == null || despesa.getCategoria().isEmpty()) {
            throw new IllegalArgumentException("Categoria é obrigatória");
        }
        if(despesa.getDescricao() == null || despesa.getDescricao().isEmpty()) {
            throw new IllegalArgumentException("Descrição é obrigatória");
        }
        if(despesa.getValor() == null || despesa.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor é obrigatório e deve ser positivo");
        }
        if(despesa.getData() == null) {
            throw new IllegalArgumentException("Data é obrigatória");
        }
        if(despesa.getEmail() == null || despesa.getEmail().isEmpty()) {
        }
        despesa = despesaRepository.save(despesa);
        return despesa;
    }
}
