package br.com.centrosocial.sgcs.Models.Pessoa;

import jakarta.persistence.Entity;

import java.time.LocalDateTime;

@Entity
public class Juridica extends Pessoa{
    private String razaoSocial;
    @jakarta.persistence.Column(unique = true, nullable = false)
    private String cnpj;

    public Juridica(Long id, String telefone, String cep, String logradouro, String numero, String bairro, String cidade, String estado, boolean status, LocalDateTime dataCriacao, LocalDateTime dataInativacao, String razaoSocial, String cnpj) {
        super(id, telefone, cep, logradouro, numero, bairro, cidade, estado, status, dataCriacao, dataInativacao);
        this.razaoSocial = razaoSocial;
        this.cnpj = cnpj;
    }

    public Juridica(){
        super();
    }

    //getters and setters
    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }
}
