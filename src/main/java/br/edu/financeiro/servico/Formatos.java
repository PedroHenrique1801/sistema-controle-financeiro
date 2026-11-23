package br.edu.financeiro.servico;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

public final class Formatos {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private Formatos() { }

    public static BigDecimal lerValor(String texto) {
        if (texto == null || !texto.strip().matches("[0-9]{1,10}([,.][0-9]{1,2})?"))
            throw new IllegalArgumentException("Informe o valor sem R$ e sem separador de milhar. Exemplo: 1250,50.");
        return new BigDecimal(texto.strip().replace(',', '.')).setScale(2);
    }
    public static LocalDate lerData(String texto) {
        try {
            LocalDate data = LocalDate.parse(texto == null ? "" : texto.strip(), DATA);
            if (data.getYear() < 1000 || data.getYear() > 9999) throw new DateTimeParseException("Ano inválido", texto, 0);
            return data;
        } catch (DateTimeParseException erro) {
            throw new IllegalArgumentException("Informe uma data válida no formato dd/mm/aaaa.");
        }
    }
    public static LocalDate lerDataOpcional(String texto) {
        return texto == null || texto.isBlank() ? null : lerData(texto);
    }
    public static String data(LocalDate data) { return data.format(DATA); }
    public static String moeda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(valor);
    }
}
