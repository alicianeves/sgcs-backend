package br.com.centrosocial.sgcs.Models.Pessoa;

import br.com.centrosocial.sgcs.Models.Familia.Familia;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Fisica extends Pessoa {
    private String nome;
    @Column(unique = true, nullable = false)
    private String cpf;
    private LocalDate dataNascimento;
    private Integer idadeInformada;
    private String email;
    private String nomeMae;
    @Enumerated(EnumType.STRING)
    private Sexo sexo;
    @Enumerated(EnumType.STRING)
    private EstadoCivil estadoCivil;
    private String rg;
    private String nis;
    @Enumerated(EnumType.STRING)
    private Escolaridade escolaridade;
    private String ocupacao;
    private String contato2;
    @Column(unique = true)
    private String usuario;
    private String senha;
    @Enumerated(EnumType.STRING)
    private Perfil perfil;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCadastro tipoCadastro = TipoCadastro.PESSOA;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "familia_id")
    private Familia familia;
    @Enumerated(EnumType.STRING)
    private VinculoFamiliar vinculoFamiliar;
    @OneToMany(mappedBy = "idoso", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<ContatoFamiliar> contatosFamiliares = new ArrayList<>();

    public Fisica() { super(); }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public Integer getIdadeInformada() { return idadeInformada; }
    public void setIdadeInformada(Integer idadeInformada) { this.idadeInformada = idadeInformada; }
    @Transient
    public Integer getIdadeEfetiva() {
        if (dataNascimento == null) return idadeInformada;
        return Period.between(dataNascimento, LocalDate.now()).getYears();
    }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getNomeMae() { return nomeMae; }
    public void setNomeMae(String nomeMae) { this.nomeMae = nomeMae; }
    public Sexo getSexo() { return sexo; }
    public void setSexo(Sexo sexo) { this.sexo = sexo; }
    public EstadoCivil getEstadoCivil() { return estadoCivil; }
    public void setEstadoCivil(EstadoCivil estadoCivil) { this.estadoCivil = estadoCivil; }
    public String getRg() { return rg; }
    public void setRg(String rg) { this.rg = rg; }
    public String getNis() { return nis; }
    public void setNis(String nis) { this.nis = nis; }
    public Escolaridade getEscolaridade() { return escolaridade; }
    public void setEscolaridade(Escolaridade escolaridade) { this.escolaridade = escolaridade; }
    public String getOcupacao() { return ocupacao; }
    public void setOcupacao(String ocupacao) { this.ocupacao = ocupacao; }
    public String getContato2() { return contato2; }
    public void setContato2(String contato2) { this.contato2 = contato2; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }
    public TipoCadastro getTipoCadastro() { return tipoCadastro; }
    public void setTipoCadastro(TipoCadastro tipoCadastro) { this.tipoCadastro = tipoCadastro; }
    public Familia getFamilia() { return familia; }
    public void setFamilia(Familia familia) { this.familia = familia; }
    public VinculoFamiliar getVinculoFamiliar() { return vinculoFamiliar; }
    public void setVinculoFamiliar(VinculoFamiliar vinculoFamiliar) { this.vinculoFamiliar = vinculoFamiliar; }
    public List<ContatoFamiliar> getContatosFamiliares() { return contatosFamiliares; }
    public void substituirContatosFamiliares(List<ContatoFamiliar> contatos) {
        contatosFamiliares.clear();
        contatos.forEach(contato -> {
            contato.setIdoso(this);
            contatosFamiliares.add(contato);
        });
    }
}
