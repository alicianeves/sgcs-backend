package br.com.centrosocial.sgcs.Models.Familia;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Familia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 150)
    private String nome;
    private boolean possuiRendaTrabalho;
    @Column(precision = 12, scale = 2) private BigDecimal valorRendaTrabalho;
    private boolean possuiAposentadoria;
    @Column(precision = 12, scale = 2) private BigDecimal valorAposentadoria;
    private boolean possuiTransferenciaRenda;
    @Column(precision = 12, scale = 2) private BigDecimal valorTransferenciaRenda;
    private boolean possuiBeneficioMunicipal;
    @Column(precision = 12, scale = 2) private BigDecimal valorBeneficioMunicipal;
    private boolean possuiBpcIdoso;
    @Column(precision = 12, scale = 2) private BigDecimal valorBpcIdoso;
    private boolean possuiBpcDeficiencia;
    @Column(precision = 12, scale = 2) private BigDecimal valorBpcDeficiencia;
    private boolean possuiBolsaFamilia;
    @Column(precision = 12, scale = 2) private BigDecimal valorBolsaFamilia;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String relatos;
    @Enumerated(EnumType.STRING)
    private AvaliacaoFamilia avaliacao;
    private boolean status;
    private LocalDateTime dataInativacao;

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public boolean isPossuiRendaTrabalho() { return possuiRendaTrabalho; }
    public void setPossuiRendaTrabalho(boolean valor) { this.possuiRendaTrabalho = valor; }
    public BigDecimal getValorRendaTrabalho() { return valorRendaTrabalho; }
    public void setValorRendaTrabalho(BigDecimal valor) { this.valorRendaTrabalho = valor; }
    public boolean isPossuiAposentadoria() { return possuiAposentadoria; }
    public void setPossuiAposentadoria(boolean valor) { this.possuiAposentadoria = valor; }
    public BigDecimal getValorAposentadoria() { return valorAposentadoria; }
    public void setValorAposentadoria(BigDecimal valor) { this.valorAposentadoria = valor; }
    public boolean isPossuiTransferenciaRenda() { return possuiTransferenciaRenda; }
    public void setPossuiTransferenciaRenda(boolean valor) { this.possuiTransferenciaRenda = valor; }
    public BigDecimal getValorTransferenciaRenda() { return valorTransferenciaRenda; }
    public void setValorTransferenciaRenda(BigDecimal valor) { this.valorTransferenciaRenda = valor; }
    public boolean isPossuiBeneficioMunicipal() { return possuiBeneficioMunicipal; }
    public void setPossuiBeneficioMunicipal(boolean valor) { this.possuiBeneficioMunicipal = valor; }
    public BigDecimal getValorBeneficioMunicipal() { return valorBeneficioMunicipal; }
    public void setValorBeneficioMunicipal(BigDecimal valor) { this.valorBeneficioMunicipal = valor; }
    public boolean isPossuiBpcIdoso() { return possuiBpcIdoso; }
    public void setPossuiBpcIdoso(boolean valor) { this.possuiBpcIdoso = valor; }
    public BigDecimal getValorBpcIdoso() { return valorBpcIdoso; }
    public void setValorBpcIdoso(BigDecimal valor) { this.valorBpcIdoso = valor; }
    public boolean isPossuiBpcDeficiencia() { return possuiBpcDeficiencia; }
    public void setPossuiBpcDeficiencia(boolean valor) { this.possuiBpcDeficiencia = valor; }
    public BigDecimal getValorBpcDeficiencia() { return valorBpcDeficiencia; }
    public void setValorBpcDeficiencia(BigDecimal valor) { this.valorBpcDeficiencia = valor; }
    public boolean isPossuiBolsaFamilia() { return possuiBolsaFamilia; }
    public void setPossuiBolsaFamilia(boolean valor) { this.possuiBolsaFamilia = valor; }
    public BigDecimal getValorBolsaFamilia() { return valorBolsaFamilia; }
    public void setValorBolsaFamilia(BigDecimal valor) { this.valorBolsaFamilia = valor; }
    public String getRelatos() { return relatos; }
    public void setRelatos(String relatos) { this.relatos = relatos; }
    public AvaliacaoFamilia getAvaliacao() { return avaliacao; }
    public void setAvaliacao(AvaliacaoFamilia avaliacao) { this.avaliacao = avaliacao; }
    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }
    public LocalDateTime getDataInativacao() { return dataInativacao; }
    public void setDataInativacao(LocalDateTime dataInativacao) { this.dataInativacao = dataInativacao; }
}
