package br.com.centrosocial.sgcs.Models.Pessoa;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.Period;

@Entity
@Table(name = "contato_familiar_idoso")
public class ContatoFamiliar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idoso_id", nullable = false)
    private Fisica idoso;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false)
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VinculoFamiliar vinculoFamiliar;

    @Column(nullable = false, length = 20)
    private String telefone;

    public Long getId() { return id; }
    public Fisica getIdoso() { return idoso; }
    public void setIdoso(Fisica idoso) { this.idoso = idoso; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public Integer getIdade() {
        return dataNascimento == null ? null : Period.between(dataNascimento, LocalDate.now()).getYears();
    }
    public VinculoFamiliar getVinculoFamiliar() { return vinculoFamiliar; }
    public void setVinculoFamiliar(VinculoFamiliar vinculoFamiliar) { this.vinculoFamiliar = vinculoFamiliar; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
}
