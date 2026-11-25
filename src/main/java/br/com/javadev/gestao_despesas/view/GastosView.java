package br.com.javadev.gestao_despesas.view;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import org.springframework.data.core.PropertyPath;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import br.com.javadev.gestao_despesas.model.Despesa;
import br.com.javadev.gestao_despesas.useCases.CadastroDespesaUseCase;
import br.com.javadev.gestao_despesas.useCases.ConsultarDespesasUseCase;
import br.com.javadev.gestao_despesas.useCases.RemoverDespesaUseCase;

@Route("")
@PageTitle("Gestão de despesas")
public class GastosView extends VerticalLayout {

    private static final Logger LOGGER = LoggerFactory.getLogger(GastosView.class);
    private static final int PAGE_SIZE = 50;
    private static final Locale BRAZIL = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final CadastroDespesaUseCase cadastroDespesa;
    private final ConsultarDespesasUseCase consultarDespesas;
    private final RemoverDespesaUseCase removerDespesa;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(BRAZIL);

    private final TextField filtroEmail = new TextField("E-mail");
    private final TextField filtroCategoria = new TextField("Categoria");
    private final DatePicker filtroInicio = new DatePicker("Data inicial");
    private final DatePicker filtroFim = new DatePicker("Data final");
    private final Span totalFiltrado = new Span();
    private final Grid<Despesa> grid = new Grid<>(Despesa.class, false);

    private final Dialog formularioDialog = new Dialog();
    private final TextField descricao = new TextField("Descrição");
    private final TextField email = new TextField("E-mail");
    private final TextField categoria = new TextField("Categoria");
    private final DatePicker dataGasto = new DatePicker("Data do gasto");
    private final BigDecimalField valor = new BigDecimalField("Valor (R$)");

    private Despesa despesaEmEdicao;

    public GastosView(
            CadastroDespesaUseCase cadastroDespesa,
            ConsultarDespesasUseCase consultarDespesas,
            RemoverDespesaUseCase removerDespesa) {
        this.cadastroDespesa = cadastroDespesa;
        this.consultarDespesas = consultarDespesas;
        this.removerDespesa = removerDespesa;

        configurarPagina();
        configurarFiltros();
        configurarGrid();
        configurarFormulario();

        refreshResultados();
    }

    private void configurarPagina() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle()
            .set("box-sizing", "border-box")
            .set("max-width", "1440px")
            .set("margin", "0 auto");

        H1 titulo = new H1("Gestão de despesas");
        titulo.getStyle().set("margin", "0");
        Paragraph introducao = new Paragraph(
            "Consulte, filtre e organize suas despesas em um só lugar.");

        add(titulo, introducao);
    }

    private void configurarFiltros() {
        filtroEmail.setClearButtonVisible(true);
        filtroEmail.setPlaceholder("Filtrar por e-mail");
        filtroEmail.setAriaLabel("Filtrar despesas pelo e-mail");

        filtroCategoria.setClearButtonVisible(true);
        filtroCategoria.setPlaceholder("Filtrar por categoria");
        filtroCategoria.setAriaLabel("Filtrar despesas pela categoria");

        filtroInicio.setClearButtonVisible(true);
        filtroInicio.setAriaLabel("Filtrar despesas a partir desta data");

        filtroFim.setClearButtonVisible(true);
        filtroFim.setAriaLabel("Filtrar despesas até esta data");

        FormLayout camposFiltro = new FormLayout();
        camposFiltro.setWidthFull();
        camposFiltro.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("600px", 2),
            new FormLayout.ResponsiveStep("1000px", 4)
        );
        camposFiltro.add(filtroEmail, filtroCategoria, filtroInicio, filtroFim);
        
        Button novaDespesa = new Button("Nova despesa", event -> abrirFormulario(null));
        novaDespesa.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        novaDespesa.getElement().setAttribute("aria-label", "Cadastrar nova despesa");

        Button aplicar = new Button("Aplicar filtros", event -> refreshResultados(true));
        aplicar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button mesAtual = new Button("Mês atual", event -> filtrarMesAtual());
        Button limpar = new Button("Limpar filtros", event -> limparFiltros());
        
        HorizontalLayout cabecalho = new HorizontalLayout();
        cabecalho.setWidthFull();
        cabecalho.setJustifyContentMode(JustifyContentMode.BETWEEN);
        cabecalho.setAlignItems(Alignment.CENTER);
        
        HorizontalLayout acoesFiltro = new HorizontalLayout(novaDespesa, aplicar, mesAtual, limpar);
        acoesFiltro.setWidthFull();
        acoesFiltro.setPadding(false);
        acoesFiltro.setSpacing(true);

        H2 tituloFiltros = new H2("Filtros e total");
        tituloFiltros.getStyle().set("margin", "0");
        totalFiltrado.setId("total-despesas-filtradas");
        totalFiltrado.getElement().setAttribute("role", "status");
        totalFiltrado.getElement().setAttribute("aria-live", "polite");
        totalFiltrado.getStyle()
            .set("font-size", "var(--lumo-font-size-xl)")
            .set("font-weight", "600");

        add(tituloFiltros, camposFiltro, acoesFiltro, totalFiltrado);
    }

    private void configurarGrid() {
        grid.setPageSize(PAGE_SIZE);
        grid.setHeight("60vh");
        grid.setMinHeight("20rem");
        grid.getElement().setAttribute("aria-label", "Tabela de despesas");

        grid.addColumn(despesa -> formatarData(despesa.getData()))
            .setHeader("Data do gasto")
            .setWidth("12rem")
            .setFlexGrow(0);
        grid.addColumn((@NonNull Despesa despesa) -> despesa.getDescricao())
            .setHeader("Descrição")
            .setAutoWidth(true)
            .setFlexGrow(1);
        grid.addColumn((@NonNull Despesa despesa) -> despesa.getCategoria())
            .setHeader("Categoria")
            .setAutoWidth(true);
        grid.addColumn((@NonNull Despesa despesa) -> despesa.getEmail())
            .setHeader("E-mail")
            .setWidth("15rem")
            .setFlexGrow(0);
        grid.addColumn(despesa -> currencyFormat.format(despesa.getValor()))
            .setHeader("Valor")
            .setWidth("10rem")
            .setFlexGrow(0);
        grid.addComponentColumn(this::criarAcoesLinha)
            .setHeader("Ações")
            .setAutoWidth(true)
            .setFlexGrow(0);

        CallbackDataProvider<Despesa, Void> dataProvider = DataProvider.fromCallbacks(
            this::buscarPagina,
            this::contarResultados
        );
        grid.setDataProvider(dataProvider);

        add(grid);
    }

    private java.util.stream.Stream<Despesa> buscarPagina(Query<Despesa, Void> query) {
        var pageable = PageRequest.of(
            query.getPage(),
            query.getPageSize(),
            Sort.by(
                Sort.Direction.DESC,
                PropertyPath.of((@NonNull Despesa despesa) -> despesa.getData())
            )
        );
        return consultarDespesas.pesquisar(
            valorFiltro(filtroEmail),
            valorFiltro(filtroCategoria),
            filtroInicio.getValue(),
            filtroFim.getValue(),
            pageable
        ).getContent().stream();
    }

    private int contarResultados(Query<Despesa, Void> query) {
        long quantidade = consultarDespesas.contar(
            valorFiltro(filtroEmail),
            valorFiltro(filtroCategoria),
            filtroInicio.getValue(),
            filtroFim.getValue()
        );
        return (int) Math.min(quantidade, Integer.MAX_VALUE);
    }

    private HorizontalLayout criarAcoesLinha(Despesa despesa) {
        Button editar = new Button("Editar", event -> abrirFormulario(despesa));
        editar.getElement().setAttribute(
            "aria-label",
            "Editar despesa: " + despesa.getDescricao()
        );

        Button excluir = new Button("Excluir", event -> confirmarExclusao(despesa));
        excluir.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
        excluir.getElement().setAttribute(
            "aria-label",
            "Excluir despesa: " + despesa.getDescricao()
        );

        HorizontalLayout acoes = new HorizontalLayout(editar, excluir);
        acoes.setPadding(false);
        acoes.setSpacing(true);
        return acoes;
    }

    private void configurarFormulario() {
        descricao.setRequired(true);
        descricao.setRequiredIndicatorVisible(true);
        email.setRequired(true);
        email.setRequiredIndicatorVisible(true);
        categoria.setRequired(true);
        categoria.setRequiredIndicatorVisible(true);
        dataGasto.setRequired(true);
        dataGasto.setRequiredIndicatorVisible(true);
        valor.setRequired(true);
        valor.setRequiredIndicatorVisible(true);

        descricao.setMaxLength(255);
        email.setMaxLength(254);
        categoria.setMaxLength(100);
        valor.setAriaLabel("Valor da despesa em reais");

        FormLayout campos = new FormLayout(
            descricao,
            email,
            categoria,
            dataGasto,
            valor
        );
        campos.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("600px", 2)
        );
        formularioDialog.add(campos);

        Button cancelar = new Button("Cancelar", event -> formularioDialog.close());
        Button salvar = new Button("Salvar despesa", event -> salvarDespesa());
        salvar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        formularioDialog.getFooter().add(cancelar, salvar);
        formularioDialog.setWidth("min(42rem, 95vw)");
        formularioDialog.setCloseOnEsc(true);
    }

    private void abrirFormulario(@Nullable Despesa despesa) {
        despesaEmEdicao = despesa;
        boolean editando = despesa != null;
        formularioDialog.setHeaderTitle(editando ? "Editar despesa" : "Nova despesa");

        if (despesa == null) {
            descricao.setValue("");
            email.setValue("");
            categoria.setValue("");
            dataGasto.setValue(LocalDate.now());
            valor.setValue(null);
        } else {
            descricao.setValue(textoOuVazio(despesa.getDescricao()));
            email.setValue(textoOuVazio(despesa.getEmail()));
            categoria.setValue(textoOuVazio(despesa.getCategoria()));
            dataGasto.setValue(despesa.getData());
            valor.setValue(despesa.getValor());
        }

        formularioDialog.open();
        descricao.focus();
    }

    private void salvarDespesa() {
        String descricaoInformada = descricao.getValue().trim();
        String emailInformado = email.getValue().trim();
        String categoriaInformada = categoria.getValue().trim();
        BigDecimal valorInformado = valor.getValue();

        if (descricaoInformada.isEmpty()
                || emailInformado.isEmpty()
                || categoriaInformada.isEmpty()
                || dataGasto.getValue() == null
                || valorInformado == null
                || valorInformado.compareTo(BigDecimal.ZERO) <= 0) {
            notificar("Preencha todos os campos e informe um valor maior que zero.");
            return;
        }

        Despesa despesa = despesaEmEdicao == null ? new Despesa() : despesaEmEdicao;
        despesa.setDescricao(descricaoInformada);
        despesa.setEmail(emailInformado);
        despesa.setCategoria(categoriaInformada);
        despesa.setData(dataGasto.getValue());
        despesa.setValor(valorInformado);

        try {
            cadastroDespesa.execute(despesa);
            formularioDialog.close();
            notificar(despesaEmEdicao == null
                ? "Despesa cadastrada."
                : "Despesa atualizada.");
            despesaEmEdicao = null;
            refreshResultados();
        } catch (IllegalArgumentException exception) {
            notificar(exception.getMessage());
        } catch (DataAccessException exception) {
            LOGGER.error("Não foi possível salvar a despesa.", exception);
            notificar("Não foi possível salvar a despesa. Verifique os dados e tente novamente.");
        }
    }

    private void confirmarExclusao(Despesa despesa) {
        Dialog confirmacao = new Dialog();
        confirmacao.setHeaderTitle("Excluir despesa?");
        confirmacao.setCloseOnEsc(true);
        confirmacao.setCloseOnOutsideClick(false);
        confirmacao.add(new Paragraph(
            "A despesa \"" + despesa.getDescricao()
                + "\" será removida permanentemente."
        ));

        Button cancelar = new Button("Cancelar", event -> confirmacao.close());
        Button excluir = new Button("Excluir despesa", event -> {
            try {
                if (removerDespesa.execute(despesa.getId())) {
                    notificar("Despesa excluída.");
                } else {
                    notificar("A despesa não foi encontrada; atualize a lista.");
                }
                confirmacao.close();
                refreshResultados();
            } catch (DataAccessException exception) {
                LOGGER.error("Não foi possível excluir a despesa {}.", despesa.getId(), exception);
                notificar("Não foi possível excluir a despesa. Tente novamente.");
            }
        });
        excluir.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        confirmacao.getFooter().add(cancelar, excluir);
        confirmacao.open();
    }

    private void filtrarMesAtual() {
        YearMonth mesAtual = YearMonth.now();
        filtroInicio.setValue(mesAtual.atDay(1));
        filtroFim.setValue(mesAtual.atEndOfMonth());
        refreshResultados(true);
    }

    private void limparFiltros() {
        filtroEmail.clear();
        filtroCategoria.clear();
        filtroInicio.clear();
        filtroFim.clear();
        refreshResultados(true);
    }

    private void refreshResultados() {
        refreshResultados(false);
    }

    private void refreshResultados(boolean alertarSemResultados) {
        LocalDate inicio = filtroInicio.getValue();
        LocalDate fim = filtroFim.getValue();
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            notificar("A data inicial não pode ser posterior à data final.");
            return;
        }

        if (alertarSemResultados
                && consultarDespesas.contar(
                    valorFiltro(filtroEmail),
                    valorFiltro(filtroCategoria),
                    inicio,
                    fim
                ) == 0) {
            mostrarAlertaSemResultados();
        }

        grid.getDataProvider().refreshAll();
        BigDecimal total = consultarDespesas.somar(
            valorFiltro(filtroEmail),
            valorFiltro(filtroCategoria),
            inicio,
            fim
        );
        totalFiltrado.setText("Total de gastos encontrados: " + currencyFormat.format(total));
    }

    private void mostrarAlertaSemResultados() {
        Dialog alerta = new Dialog();
        alerta.addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        alerta.setWidth("min(28rem, calc(100vw - 2rem))");
        alerta.setCloseOnEsc(true);
        alerta.setCloseOnOutsideClick(true);

        Button fechar = new Button("×", event -> alerta.close());
        fechar.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        fechar.setAriaLabel("Fechar aviso");
        fechar.getStyle()
            .set("font-size", "1.4rem")
            .set("color", "var(--lumo-secondary-text-color)");

        HorizontalLayout topo = new HorizontalLayout(fechar);
        topo.setWidthFull();
        topo.setPadding(false);
        topo.setSpacing(false);
        topo.setJustifyContentMode(JustifyContentMode.END);

        Span icone = new Span("i");
        icone.getStyle()
            .set("display", "inline-flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("width", "2.5rem")
            .set("height", "2.5rem")
            .set("border-radius", "50%")
            .set("background-color", "rgba(255, 255, 255, 0.7)")
            .set("color", "var(--lumo-primary-color)")
            .set("font-weight", "700");

        H2 titulo = new H2("Nenhum resultado encontrado");
        titulo.getStyle()
            .set("margin", "0")
            .set("font-size", "var(--lumo-font-size-l)");

        Paragraph mensagem = new Paragraph(
            "Não encontramos despesas com os filtros aplicados. "
                + "Tente ajustar os critérios da busca."
        );
        mensagem.getStyle()
            .set("margin", "0")
            .set("color", "var(--lumo-secondary-text-color)")
            .set("text-align", "center");

        VerticalLayout conteudo = new VerticalLayout(topo, icone, titulo, mensagem);
        conteudo.setWidthFull();
        conteudo.setPadding(true);
        conteudo.setSpacing(true);
        conteudo.setAlignItems(Alignment.CENTER);
        conteudo.getStyle()
            .set("box-sizing", "border-box")
            .set("padding", "1.5rem")
            .set("background-color", "#eaf5ff")
            .set("border-radius", "var(--lumo-border-radius-l)");

        alerta.add(conteudo);
        alerta.open();
    }

    private String valorFiltro(TextField campo) {
        String valorInformado = campo.getValue();
        return valorInformado == null || valorInformado.isBlank()
            ? null
            : valorInformado.trim();
    }

    private String textoOuVazio(String texto) {
        return texto == null ? "" : texto;
    }

    private String formatarData(LocalDate data) {
        return data == null ? "" : data.format(DATE_FORMAT);
    }

    private void notificar(String mensagem) {
        com.vaadin.flow.component.notification.Notification.show(mensagem);
    }
}
