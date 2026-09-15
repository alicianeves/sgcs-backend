package br.com.centrosocial.sgcs.Models.Pessoa;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
public class Fisica extends Pessoa{
    private String nome;
    @jakarta.persistence.Column(unique = true, nullable = false)
    private String cpf;
    private LocalDate dataNascimento;
    private String email;
    @jakarta.persistence.Column(unique = true, nullable = false)
    private String usuario;
    private String senha;
    @Enumerated(EnumType.STRING)
    private Perfil perfil;

    public Fisica(Long id, String telefone, String cep, String logradouro, String numero, String bairro, String cidade, String estado, boolean status, LocalDateTime dataCriacao, LocalDateTime dataInativacao, String nome, String cpf, LocalDate dataNascimento, String email, String usuario, String senha, Perfil perfil) {
        super(id, telefone, cep, logradouro, numero, bairro, cidade, estado, status, dataCriacao, dataInativacao);
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.email = email;
        this.usuario = usuario;
        this.senha = senha;
        this.perfil = perfil;
    }

    public Fisica(){
        super();
    }

    //getters and setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }
}
