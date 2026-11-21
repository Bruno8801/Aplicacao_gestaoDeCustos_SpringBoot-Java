package br.com.javadev.gestao_despesas.useCases;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.javadev.gestao_despesas.model.Despesa;
import br.com.javadev.gestao_despesas.repository.DespesaRepository;

@Service
public class BuscarDespesaUseCase {
    private final DespesaRepository despesaRepository;

    BuscarDespesaUseCase(DespesaRepository despesaRepository) {
        this.despesaRepository = despesaRepository;
    }
    
    public List<Despesa> execute(String email, LocalDate data) {
        List<Despesa> despesas;
        if(data != null) {
            despesas = despesaRepository.findByEmailAndData(email, data);
        } else {
            despesas = despesaRepository.findByEmail(email);
        }
        return despesas;
    }
}
