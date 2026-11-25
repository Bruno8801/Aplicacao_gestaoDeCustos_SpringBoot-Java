package br.com.javadev.gestao_despesas.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import br.com.javadev.gestao_despesas.model.Despesa;

@DataJpaTest
@Import(DespesaRepositoryTests.CacheConfiguration.class)
class DespesaRepositoryTests {

    @Autowired
    private DespesaRepository despesaRepository;

    @Test
    void filtersExpensesAndSumsOnlyMatchingRows() {
        despesaRepository.saveAll(List.of(
            despesa("ana@example.com", "Moradia", LocalDate.of(2026, 11, 5), "100.00"),
            despesa("ana@example.com", "Moradia", LocalDate.of(2026, 11, 25), "25.50"),
            despesa("ana@example.com", "Transporte", LocalDate.of(2026, 11, 10), "40.00"),
            despesa("bruno@example.com", "Moradia", LocalDate.of(2026, 11, 10), "75.00"),
            despesa("ana@example.com", "Moradia", LocalDate.of(2026, 12, 1), "90.00")
        ));

        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fim = LocalDate.of(2026, 11, 30);
        Page<Despesa> resultado = despesaRepository.pesquisar(
            "ANA@EXAMPLE.COM",
            "moradia",
            inicio,
            fim,
            PageRequest.of(0, 10)
        );

        assertEquals(2, resultado.getTotalElements());
        assertEquals(
            2,
            despesaRepository.contarFiltradas(
                "ANA@EXAMPLE.COM",
                "moradia",
                inicio,
                fim
            )
        );
        assertEquals(
            0,
            despesaRepository.somarValoresFiltrados(
                "ANA@EXAMPLE.COM",
                "moradia",
                inicio,
                fim
            ).compareTo(new BigDecimal("125.50"))
        );
    }

    private Despesa despesa(String email, String categoria, LocalDate data, String valor) {
        Despesa despesa = new Despesa();
        despesa.setDescricao("Despesa de teste");
        despesa.setEmail(email);
        despesa.setCategoria(categoria);
        despesa.setData(data);
        despesa.setValor(new BigDecimal(valor));
        return despesa;
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class CacheConfiguration {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("gastosPorEmailcache");
        }
    }
}
