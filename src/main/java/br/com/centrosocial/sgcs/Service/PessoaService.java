package br.com.centrosocial.sgcs.Service;

import br.com.centrosocial.sgcs.DTO.Pessoa.FisicaRequest;
import br.com.centrosocial.sgcs.DTO.Pessoa.JuridicaRequest;
import br.com.centrosocial.sgcs.DTO.Pessoa.PessoaResponse;
import br.com.centrosocial.sgcs.Exception.BusinessException;
import br.com.centrosocial.sgcs.Exception.ConflictException;
import br.com.centrosocial.sgcs.Exception.ResourceNotFoundException;
import br.com.centrosocial.sgcs.Models.Pessoa.Fisica;
import br.com.centrosocial.sgcs.Models.Pessoa.Juridica;
import br.com.centrosocial.sgcs.Models.Pessoa.Pessoa;
import br.com.centrosocial.sgcs.Repository.FisicaRepository;
import br.com.centrosocial.sgcs.Repository.JuridicaRepository;
import br.com.centrosocial.sgcs.Repository.PessoaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PessoaService {
    private final PessoaRepository pessoaRepository;
    private final FisicaRepository fisicaRepository;
    private final JuridicaRepository juridicaRepository;
    private final PasswordEncoder passwordEncoder;

    public PessoaService(
            PessoaRepository pessoaRepository,
            FisicaRepository fisicaRepository,
            JuridicaRepository juridicaRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.pessoaRepository = pessoaRepository;
        this.fisicaRepository = fisicaRepository;
        this.juridicaRepository = juridicaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<PessoaResponse> listar() {
        return pessoaRepository.findAllByStatusTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PessoaResponse buscarPorId(Long id) {
        return toResponse(buscarPessoaAtiva(id));
    }

    @Transactional
    public PessoaResponse cadastrarFisica(FisicaRequest request) {
        validarSenhaObrigatoria(request.senha());
        validarConflitosFisica(request.cpf(), request.usuario(), null);

        Fisica fisica = new Fisica();
        preencherDadosComuns(fisica, request.telefone(), request.cep(), request.logradouro(),
                request.numero(), request.bairro(), request.cidade(), request.estado());
        preencherFisica(fisica, request);
        fisica.setSenha(passwordEncoder.encode(request.senha()));
        ativarNovaPessoa(fisica);

        return toResponse(fisicaRepository.save(fisica));
    }

    @Transactional
    public PessoaResponse atualizarFisica(Long id, FisicaRequest request) {
        Fisica fisica = fisicaRepository.findById(id)
                .filter(Pessoa::isStatus)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa física não encontrada."));

        validarConflitosFisica(request.cpf(), request.usuario(), id);
        preencherDadosComuns(fisica, request.telefone(), request.cep(), request.logradouro(),
                request.numero(), request.bairro(), request.cidade(), request.estado());
        preencherFisica(fisica, request);

        if (request.senha() != null && !request.senha().isBlank()) {
            fisica.setSenha(passwordEncoder.encode(request.senha()));
        }

        return toResponse(fisicaRepository.save(fisica));
    }

    @Transactional
    public PessoaResponse cadastrarJuridica(JuridicaRequest request) {
        validarConflitoCnpj(request.cnpj(), null);

        Juridica juridica = new Juridica();
        preencherDadosComuns(juridica, request.telefone(), request.cep(), request.logradouro(),
                request.numero(), request.bairro(), request.cidade(), request.estado());
        preencherJuridica(juridica, request);
        ativarNovaPessoa(juridica);

        return toResponse(juridicaRepository.save(juridica));
    }

    @Transactional
    public PessoaResponse atualizarJuridica(Long id, JuridicaRequest request) {
        Juridica juridica = juridicaRepository.findById(id)
                .filter(Pessoa::isStatus)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa jurídica não encontrada."));

        validarConflitoCnpj(request.cnpj(), id);
        preencherDadosComuns(juridica, request.telefone(), request.cep(), request.logradouro(),
                request.numero(), request.bairro(), request.cidade(), request.estado());
        preencherJuridica(juridica, request);

        return toResponse(juridicaRepository.save(juridica));
    }

    @Transactional
    public void inativar(Long id) {
        Pessoa pessoa = buscarPessoaAtiva(id);
        pessoa.setStatus(false);
        pessoa.setDataInativacao(LocalDateTime.now());
        pessoaRepository.save(pessoa);
    }

    private Pessoa buscarPessoaAtiva(Long id) {
        return pessoaRepository.findById(id)
                .filter(Pessoa::isStatus)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada."));
    }

    private void validarConflitosFisica(String cpf, String usuario, Long id) {
        boolean cpfEmUso = id == null
                ? fisicaRepository.existsByCpf(cpf)
                : fisicaRepository.existsByCpfAndIdNot(cpf, id);
        boolean usuarioEmUso = id == null
                ? fisicaRepository.existsByUsuario(usuario)
                : fisicaRepository.existsByUsuarioAndIdNot(usuario, id);

        if (cpfEmUso) {
            throw new ConflictException("CPF já cadastrado.");
        }
        if (usuarioEmUso) {
            throw new ConflictException("Usuário já cadastrado.");
        }
    }

    private void validarConflitoCnpj(String cnpj, Long id) {
        boolean cnpjEmUso = id == null
                ? juridicaRepository.existsByCnpj(cnpj)
                : juridicaRepository.existsByCnpjAndIdNot(cnpj, id);

        if (cnpjEmUso) {
            throw new ConflictException("CNPJ já cadastrado.");
        }
    }

    private void validarSenhaObrigatoria(String senha) {
        if (senha == null || senha.isBlank()) {
            throw new BusinessException("A senha é obrigatória.");
        }
    }

    private void ativarNovaPessoa(Pessoa pessoa) {
        pessoa.setStatus(true);
        pessoa.setDataCriacao(LocalDateTime.now());
        pessoa.setDataInativacao(null);
    }

    private void preencherDadosComuns(
            Pessoa pessoa,
            String telefone,
            String cep,
            String logradouro,
            String numero,
            String bairro,
            String cidade,
            String estado
    ) {
        pessoa.setTelefone(telefone);
        pessoa.setCep(cep);
        pessoa.setLogradouro(logradouro);
        pessoa.setNumero(numero);
        pessoa.setBairro(bairro);
        pessoa.setCidade(cidade);
        pessoa.setEstado(estado);
    }

    private void preencherFisica(Fisica fisica, FisicaRequest request) {
        fisica.setNome(request.nome());
        fisica.setCpf(request.cpf());
        fisica.setDataNascimento(request.dataNascimento());
        fisica.setEmail(request.email());
        fisica.setUsuario(request.usuario());
        fisica.setPerfil(request.perfil());
    }

    private void preencherJuridica(Juridica juridica, JuridicaRequest request) {
        juridica.setRazaoSocial(request.razaoSocial());
        juridica.setCnpj(request.cnpj());
    }

    private PessoaResponse toResponse(Pessoa pessoa) {
        if (pessoa instanceof Fisica fisica) {
            return new PessoaResponse(
                    fisica.getId(), "FISICA", fisica.getTelefone(), fisica.getCep(), fisica.getLogradouro(),
                    fisica.getNumero(), fisica.getBairro(), fisica.getCidade(), fisica.getEstado(),
                    fisica.isStatus(), fisica.getDataCriacao(), fisica.getDataInativacao(), fisica.getNome(),
                    fisica.getCpf(), fisica.getDataNascimento(), fisica.getEmail(), fisica.getUsuario(),
                    fisica.getPerfil(), null, null
            );
        }

        Juridica juridica = (Juridica) pessoa;
        return new PessoaResponse(
                juridica.getId(), "JURIDICA", juridica.getTelefone(), juridica.getCep(), juridica.getLogradouro(),
                juridica.getNumero(), juridica.getBairro(), juridica.getCidade(), juridica.getEstado(),
                juridica.isStatus(), juridica.getDataCriacao(), juridica.getDataInativacao(), null,
                null, null, null, null, null, juridica.getRazaoSocial(), juridica.getCnpj()
        );
    }
}
