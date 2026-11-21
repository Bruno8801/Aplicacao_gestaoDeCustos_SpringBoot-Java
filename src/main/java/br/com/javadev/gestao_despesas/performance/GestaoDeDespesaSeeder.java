package br.com.javadev.gestao_despesas.performance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import br.com.javadev.gestao_despesas.model.Despesa;
import br.com.javadev.gestao_despesas.repository.DespesaRepository;

@Component
public class GestaoDeDespesaSeeder implements CommandLineRunner {

    private final DespesaRepository despesaRepository;

    public GestaoDeDespesaSeeder(DespesaRepository despesaRepository) {
        this.despesaRepository = despesaRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. CHAVE LIGA/DESLIGA: mude para false quando não quiser rodar o seeder
        boolean executarSeeder = false;
        if (!executarSeeder) return;

        // Limpa os dados de teste anteriores antes de inserir os novos para evitar duplicidade quando usar a função apenasMesEspecifico = true;
        List<Despesa> antigos = despesaRepository.findByEmail("performance@gmail.com");
        if (!antigos.isEmpty()) {
        despesaRepository.deleteAllInBatch(antigos);
        }

        // 2. ESCOLHA O MODO: true = apenas um mês específico | false = vários meses (retroativo)
        boolean apenasMesEspecifico = true;

        // 3. QUANTIDADE (usado quando apenasMesEspecifico = false)
        int quantidadeRegistros = 150000;

        List<Despesa> despesas = new ArrayList<>();
        System.out.println("Iniciando geração de seed...");

        if (apenasMesEspecifico) {
            // Define o mês exato que você quer testar (ex: Novembro de 2026)
            YearMonth mesAlvo = YearMonth.of(2026, 10);
            int totalDiasNoMes = mesAlvo.lengthOfMonth(); // Novembro = 30, Dezembro = 31

            // Cria 1 despesa para cada dia do mês escolhido (valores de 10 até 39 ou 40)
            for (int dia = 1; dia <= totalDiasNoMes; dia++) {
                Despesa despesa = new Despesa();
                despesa.setDescricao("Gasto dia " + dia);
                despesa.setValor(BigDecimal.valueOf(10 + (dia - 1))); // Começa em 10 e soma +1 por dia
                despesa.setData(mesAlvo.atDay(dia)); // Garante 100% dentro do mês escolhido!
                despesa.setCategoria("TESTE");
                despesa.setEmail("performance@gmail.com");

                despesas.add(despesa);
            }
        } else {
            // Espalha os gastos pelos últimos dias/meses a partir de hoje
            for (int i = 0; i < quantidadeRegistros; i++) {
                Despesa despesa = new Despesa();
                despesa.setDescricao("Gasto n: " + i);
                despesa.setValor(BigDecimal.valueOf(10 + (i % 50)));
                // % 365 espalha a quantidade de gastos ao longo de 1 ano inteiro (12 meses)
                // caso queira alterar, mude o % 365 para os dias especificos ex: 30 dias o mês
                despesa.setData(LocalDate.now().minusDays(i % 365));
                despesa.setCategoria("TESTE");
                despesa.setEmail("performance@gmail.com");

                despesas.add(despesa);
            }
        }

        despesaRepository.saveAll(despesas);
        System.out.println("Finalizou geração de seed: " + despesas.size() + " registros criados.");
    }
}

