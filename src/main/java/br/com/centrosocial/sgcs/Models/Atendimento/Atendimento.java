package br.com.centrosocial.sgcs.Models.Atendimento;

import br.com.centrosocial.sgcs.Models.Pessoa.Fisica;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Atendimento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fisica_id", nullable = false)
    private Fisica fisica;
    private LocalDate dataAtendimento;
    private boolean bolsaFamilia;
    private boolean crasNochete;
    private boolean ubsEsfGuanabara;
    @Enumerated(EnumType.STRING)
    private RespostaBinaria resposta01;
    @Column(columnDefinition = "TEXT")
    private String observacao01;
    @Enumerated(EnumType.STRING)
    private RespostaBinaria resposta02;
    @Column(columnDefinition = "TEXT")
    private String observacao02;
    @Enumerated(EnumType.STRING)
    private RespostaBinaria resposta03;
    @Column(columnDefinition = "TEXT")
    private String observacao03;
    @Enumerated(EnumType.STRING)
    private RespostaBinaria resposta04;
    @Column(columnDefinition = "TEXT")
    private String observacao04;
    @Enumerated(EnumType.STRING)
    private RespostaBinaria resposta05;
    @Column(columnDefinition = "TEXT")
    private String observacao05;
    @Enumerated(EnumType.STRING)
    private RespostaBinaria resposta06;
    @Column(columnDefinition = "TEXT")
    private String observacao06;
    @Enumerated(EnumType.STRING)
    private RespostaBinaria resposta07;
    @Column(columnDefinition = "TEXT")
    private String observacao07;
    @Enumerated(EnumType.STRING)
    private RespostaBinaria resposta08;
    @Column(columnDefinition = "TEXT")
    private String observacao08;
    @Enumerated(EnumType.STRING)
    private RespostaBinaria resposta09;
    @Column(columnDefinition = "TEXT")
    private String observacao09;
    @Enumerated(EnumType.STRING)
    private RespostaBinaria resposta10;
    @Column(columnDefinition = "TEXT")
    private String observacao10;
    @Enumerated(EnumType.STRING)
    private RespostaBinaria resposta11;
    @Column(columnDefinition = "TEXT")
    private String observacao11;
    @Enumerated(EnumType.STRING)
    private RespostaBinaria resposta12;
    @Column(columnDefinition = "TEXT")
    private String observacao12;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String encaminhamentos;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String relatos;
    private boolean status;
    private LocalDateTime dataInativacao;

    public Long getId() { return id; }
    public Fisica getFisica() { return fisica; }
    public void setFisica(Fisica fisica) { this.fisica = fisica; }
    public LocalDate getDataAtendimento() { return dataAtendimento; }
    public void setDataAtendimento(LocalDate dataAtendimento) { this.dataAtendimento = dataAtendimento; }
    public boolean isBolsaFamilia() { return bolsaFamilia; }
    public void setBolsaFamilia(boolean bolsaFamilia) { this.bolsaFamilia = bolsaFamilia; }
    public boolean isCrasNochete() { return crasNochete; }
    public void setCrasNochete(boolean crasNochete) { this.crasNochete = crasNochete; }
    public boolean isUbsEsfGuanabara() { return ubsEsfGuanabara; }
    public void setUbsEsfGuanabara(boolean valor) { this.ubsEsfGuanabara = valor; }
    public String getEncaminhamentos() { return encaminhamentos; }
    public void setEncaminhamentos(String encaminhamentos) { this.encaminhamentos = encaminhamentos; }
    public String getRelatos() { return relatos; }
    public void setRelatos(String relatos) { this.relatos = relatos; }
    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }
    public LocalDateTime getDataInativacao() { return dataInativacao; }
    public void setDataInativacao(LocalDateTime dataInativacao) { this.dataInativacao = dataInativacao; }

    public RespostaBinaria getResposta(int numero) {
        return switch (numero) {
            case 1 -> resposta01;
            case 2 -> resposta02;
            case 3 -> resposta03;
            case 4 -> resposta04;
            case 5 -> resposta05;
            case 6 -> resposta06;
            case 7 -> resposta07;
            case 8 -> resposta08;
            case 9 -> resposta09;
            case 10 -> resposta10;
            case 11 -> resposta11;
            case 12 -> resposta12;
            default -> throw new IllegalArgumentException("Pergunta inválida.");
        };
    }

    public void setResposta(int numero, RespostaBinaria resposta) {
        switch (numero) {
            case 1 -> resposta01 = resposta;
            case 2 -> resposta02 = resposta;
            case 3 -> resposta03 = resposta;
            case 4 -> resposta04 = resposta;
            case 5 -> resposta05 = resposta;
            case 6 -> resposta06 = resposta;
            case 7 -> resposta07 = resposta;
            case 8 -> resposta08 = resposta;
            case 9 -> resposta09 = resposta;
            case 10 -> resposta10 = resposta;
            case 11 -> resposta11 = resposta;
            case 12 -> resposta12 = resposta;
            default -> throw new IllegalArgumentException("Pergunta inválida.");
        }
    }

    public String getObservacao(int numero) {
        return switch (numero) {
            case 1 -> observacao01;
            case 2 -> observacao02;
            case 3 -> observacao03;
            case 4 -> observacao04;
            case 5 -> observacao05;
            case 6 -> observacao06;
            case 7 -> observacao07;
            case 8 -> observacao08;
            case 9 -> observacao09;
            case 10 -> observacao10;
            case 11 -> observacao11;
            case 12 -> observacao12;
            default -> throw new IllegalArgumentException("Pergunta inválida.");
        };
    }

    public void setObservacao(int numero, String observacao) {
        switch (numero) {
            case 1 -> observacao01 = observacao;
            case 2 -> observacao02 = observacao;
            case 3 -> observacao03 = observacao;
            case 4 -> observacao04 = observacao;
            case 5 -> observacao05 = observacao;
            case 6 -> observacao06 = observacao;
            case 7 -> observacao07 = observacao;
            case 8 -> observacao08 = observacao;
            case 9 -> observacao09 = observacao;
            case 10 -> observacao10 = observacao;
            case 11 -> observacao11 = observacao;
            case 12 -> observacao12 = observacao;
            default -> throw new IllegalArgumentException("Pergunta inválida.");
        }
    }
}

