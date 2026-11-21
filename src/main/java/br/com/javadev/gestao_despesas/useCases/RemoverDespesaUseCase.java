package br.com.javadev.gestao_despesas.useCases;

import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import br.com.javadev.gestao_despesas.repository.DespesaRepository;

@Service 
public class RemoverDespesaUseCase {
    private final DespesaRepository despesaRepository;
    
    public RemoverDespesaUseCase(DespesaRepository despesaRepository) {
        this.despesaRepository = despesaRepository;
    }
    @CacheEvict (cacheNames = "gastosPorEmailcache", allEntries = true)
    public boolean execute(UUID id) {
        if (!despesaRepository.existsById(id)) {
            return false;
        }
        despesaRepository.deleteById(id);
        return true;

    }
}
