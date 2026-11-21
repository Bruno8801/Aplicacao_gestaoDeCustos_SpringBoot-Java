package br.com.javadev.gestao_despesas.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import br.com.javadev.gestao_despesas.useCases.RemoverDespesaUseCase;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.javadev.gestao_despesas.custom_messages.ErrorMessage;
import br.com.javadev.gestao_despesas.dto.ResumoMensalDespesa;
import br.com.javadev.gestao_despesas.model.Despesa;
import br.com.javadev.gestao_despesas.repository.DespesaRepository;
import br.com.javadev.gestao_despesas.useCases.BuscarDespesaUseCase;
import br.com.javadev.gestao_despesas.useCases.CadastroDespesaUseCase;
import br.com.javadev.gestao_despesas.useCases.GastoMensalDespesaUseCase;

@RequestMapping ("/gestao") // abrir banco de dados http://localhost:8080/h2-console
@RestController 
public class GestaoDespesaController {

    private final CadastroDespesaUseCase cadastroDespesa;
    private final DespesaRepository despesaRepository;
    private final BuscarDespesaUseCase buscarDespesa;
    private final RemoverDespesaUseCase removerDespesa;
    private final GastoMensalDespesaUseCase buscarResumoMensal;

    public GestaoDespesaController(
            CadastroDespesaUseCase cadastroDespesa,
            DespesaRepository despesaRepository, BuscarDespesaUseCase buscarDespesa,
            RemoverDespesaUseCase removerDespesa,
            GastoMensalDespesaUseCase buscarResumoMensal) {
        this.cadastroDespesa = cadastroDespesa;
        this.despesaRepository = despesaRepository;
        this.buscarDespesa = buscarDespesa;
        this.removerDespesa = removerDespesa;
        this.buscarResumoMensal = buscarResumoMensal;
    }

    @PostMapping ("/create") // criar dados usando POST na extensão REST API client ou outra de sua preferência
                            // http://localhosta:8080/gestao/create
    public ResponseEntity<?> create(@RequestBody Despesa despesa) {
        try {
            var result = cadastroDespesa.execute(despesa);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(new ErrorMessage("Invalid input", e.getMessage()));
        }
    }

    @GetMapping("/salvos") // puxar todos os dados salvos usando o GET na extensão REST API client ou outra de sua preferência
    // http://localhosta:8080/gestao/salvos
    public ResponseEntity<List<Despesa>> salvos() {
        var despesas = despesaRepository.findAll();
        return ResponseEntity.ok(despesas);
    }

    // Buscar dados especificos GET na extensão REST API client ou outra de sua preferência
    // localhost:8080/gestao/nomedeusuario@domínio.com?data=2025-01-01, caso não queira uma data mas todos do mesmo e-mail, coloque até o e-mail sem a "?data..."
    @GetMapping("/{email}")
    public List<Despesa> findByEmailAndDate(@PathVariable String email, 
    
    @RequestParam(required = false) LocalDate data) {
    return buscarDespesa.execute(email, data);
        // Implementação do método de busca por email e data
    }

    @DeleteMapping ("/remover/{id}") //Apagar dados salvos usando o DELETE na extensão REST API client ou outra de sua preferência
    // http://localhosta:8080/gestao/remover/UUID_da_despesa
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        if (!removerDespesa.execute(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping ("/{email}/resumo-mensal") //Mostrar dados do total de gastos mensais usando GET na extensão REST API client ou outra de sua preferência
    // http://localhosta:8080/gestao/usuario@email.com/resumo-mensal?mes=2026-11
    public ResponseEntity<ResumoMensalDespesa> resumoMensal (@PathVariable String email, @RequestParam @DateTimeFormat (pattern = "yyyy-MM") YearMonth mes) {
        return ResponseEntity.ok(buscarResumoMensal.execute(email, mes));
    }
}
