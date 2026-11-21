package br.com.javadev.gestao_despesas.performance;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.javadev.gestao_despesas.model.Despesa;
import br.com.javadev.gestao_despesas.repository.DespesaRepository;

@RequestMapping ("/gestao/performance")
@RestController
public class GestaoDespesaPerformance {
    public DespesaRepository repository;

    public GestaoDespesaPerformance(DespesaRepository repository) {
        this.repository = repository;
    }

    @GetMapping ("/sem-paginacao") // localhost:8080/gestao/performance/sem-paginacao
    public ResponseEntity<List<Despesa>> listarSemPaginacao() {
        long inicio = System.currentTimeMillis();
        var despesas = repository.findAll();

        long fim = System.currentTimeMillis();
        System.out.println("Tempo (sem paginação): " + (fim - inicio) + " ms");
        return ResponseEntity.ok(despesas);
    }
    @GetMapping ("/com-paginacao") // localhost:8080/gestao/performance/com-paginacao?page=0&size=0
    public ResponseEntity<Page<Despesa>> listarComPaginacao(Pageable pageable) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        var despesas = repository.findAll(pageable);
        stopWatch.stop();

        System.out.println("Tempo (com paginação): " + stopWatch.getTotalTimeMillis() + " ms");
        return ResponseEntity.ok(despesas);
    }
    @GetMapping ("/com-paginacao/{email}") // localhost:8080/gestao/performance/com-paginacao/nomedousuario@domínio.com?page=0&size=0
    public ResponseEntity<Page<Despesa>> listarComPaginacao(@PathVariable String email, Pageable pageable) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        var despesas = repository.findByEmail(email, pageable);
        stopWatch.stop();

        System.out.println("Tempo (com paginação): " + stopWatch.getTotalTimeMillis() + " ms");
        return ResponseEntity.ok(despesas);
    }
    @Cacheable("gastosPorEmailcache")
    @GetMapping ("/cache/{email}") // localhost:8080/gestao/performance/cache/nomedousuario@domínio.com?page=0&size=0
    public ResponseEntity<Page<Despesa>> cacheComPaginacao(@PathVariable String email, Pageable pageable) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        var despesas = repository.findByEmail(email, pageable);
        stopWatch.stop();

        System.out.println("Tempo (com paginação): " + stopWatch.getTotalTimeMillis() + " ms");
        return ResponseEntity.ok(despesas);
    }
}
