package br.com.javadev.gestao_despesas.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.javadev.gestao_despesas.model.Despesa;

public interface DespesaRepository extends JpaRepository<Despesa, UUID> {
    List<Despesa> findByEmail(String email);
    List<Despesa> findByEmailAndData(String email, LocalDate data);
    Page<Despesa> findByEmail(String email, Pageable pageable);
    List<Despesa> findByEmailAndDataBetween(String email, LocalDate inicio, LocalDate fim);

    @Query(
        value = """
            SELECT d FROM Despesa d
            WHERE (:email IS NULL OR LOWER(d.email) LIKE LOWER(CONCAT('%', :email, '%')))
              AND (:categoria IS NULL OR LOWER(d.categoria) LIKE LOWER(CONCAT('%', :categoria, '%')))
              AND (:inicio IS NULL OR d.data >= :inicio)
              AND (:fim IS NULL OR d.data <= :fim)
            """,
        countQuery = """
            SELECT COUNT(d) FROM Despesa d
            WHERE (:email IS NULL OR LOWER(d.email) LIKE LOWER(CONCAT('%', :email, '%')))
              AND (:categoria IS NULL OR LOWER(d.categoria) LIKE LOWER(CONCAT('%', :categoria, '%')))
              AND (:inicio IS NULL OR d.data >= :inicio)
              AND (:fim IS NULL OR d.data <= :fim)
            """
    )
    Page<Despesa> pesquisar(
        @Param("email") String email,
        @Param("categoria") String categoria,
        @Param("inicio") LocalDate inicio,
        @Param("fim") LocalDate fim,
        Pageable pageable
    );

    @Query("""
        SELECT COUNT(d) FROM Despesa d
        WHERE (:email IS NULL OR LOWER(d.email) LIKE LOWER(CONCAT('%', :email, '%')))
          AND (:categoria IS NULL OR LOWER(d.categoria) LIKE LOWER(CONCAT('%', :categoria, '%')))
          AND (:inicio IS NULL OR d.data >= :inicio)
          AND (:fim IS NULL OR d.data <= :fim)
        """)
    long contarFiltradas(
        @Param("email") String email,
        @Param("categoria") String categoria,
        @Param("inicio") LocalDate inicio,
        @Param("fim") LocalDate fim
    );

    @Query("""
        SELECT COALESCE(SUM(d.valor), 0) FROM Despesa d
        WHERE (:email IS NULL OR LOWER(d.email) LIKE LOWER(CONCAT('%', :email, '%')))
          AND (:categoria IS NULL OR LOWER(d.categoria) LIKE LOWER(CONCAT('%', :categoria, '%')))
          AND (:inicio IS NULL OR d.data >= :inicio)
          AND (:fim IS NULL OR d.data <= :fim)
        """)
    BigDecimal somarValoresFiltrados(
        @Param("email") String email,
        @Param("categoria") String categoria,
        @Param("inicio") LocalDate inicio,
        @Param("fim") LocalDate fim
    );
}
