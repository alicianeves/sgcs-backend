package br.com.centrosocial.sgcs.Service;

import br.com.centrosocial.sgcs.DTO.Pessoa.*;
import br.com.centrosocial.sgcs.Exception.*;
import br.com.centrosocial.sgcs.Models.Pessoa.*;
import br.com.centrosocial.sgcs.Models.Familia.Familia;
import br.com.centrosocial.sgcs.Repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Stream;

@Service
public class PessoaService {
    private final PessoaRepository pessoaRepository;
    private final FisicaRepository fisicaRepository;
    private final JuridicaRepository juridicaRepository;
    private final FamiliaRepository familiaRepository;
    private final PasswordEncoder passwordEncoder;

    public PessoaService(PessoaRepository pessoaRepository, FisicaRepository fisicaRepository,
                         JuridicaRepository juridicaRepository, FamiliaRepository familiaRepository,
                         PasswordEncoder passwordEncoder) {
        this.pessoaRepository = pessoaRepository;
        this.fisicaRepository = fisicaRepository;
        this.juridicaRepository = juridicaRepository;
        this.familiaRepository = familiaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<PessoaResponse> listar() { return listar(null, true); }

    @Transactional(readOnly = true)
    public List<PessoaResponse> listar(String busca, boolean status) {
        String termo = normalizar(busca);
        String documento = termo == null ? null : somenteNumeros(termo);
        if (documento != null && documento.isBlank()) documento = null;
        return Stream.concat(fisicaRepository.buscar(termo, documento, status).stream(),
                        juridicaRepository.buscar(termo, documento, status).stream())
                .sorted(Comparator.comparing(this::nome, String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PessoaResponse buscarPorId(Long id) {
        return toResponse(pessoaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada.")));
    }

    @Transactional
    public PessoaResponse cadastrarFisica(FisicaRequest request) { return cadastrarFisica(request, false); }

    @Transactional
    public PessoaResponse cadastrarAdministradorInicial(FisicaRequest request) {
        return cadastrarFisica(request, true);
    }

    private PessoaResponse cadastrarFisica(FisicaRequest request, boolean setupInicial) {
        validarRegrasFisica(request);
        validarIdade(request.dataNascimento(), request.idadeInformada());
        String cpf = somenteNumeros(request.cpf());
        validarCpf(cpf);
        if (possuiDadosDeAcesso(request) && !setupInicial) exigirAdministrador();
        validarConflitosFisica(cpf, normalizar(request.usuario()), null);
        Fisica fisica = new Fisica();
        preencherDadosComuns(fisica, request.telefone(), request.cep(), request.logradouro(), request.numero(),
                request.bairro(), request.cidade(), request.estado());
        preencherFisica(fisica, request, cpf);
        configurarAcesso(fisica, request, true);
        ativarNovaPessoa(fisica);
        return toResponse(fisicaRepository.save(fisica));
    }

    @Transactional
    public PessoaResponse atualizarFisica(Long id, FisicaRequest request) {
        validarRegrasFisica(request);
        Fisica fisica = fisicaRepository.findById(id).filter(Pessoa::isStatus)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa física não encontrada."));
        validarIdade(request.dataNascimento(), request.idadeInformada());
        String cpf = somenteNumeros(request.cpf());
        validarCpf(cpf);
        if (!fisica.getCpf().equals(cpf)) throw new BusinessException("CPF não pode ser alterado.");

        if (request.tipoCadastro() == TipoCadastro.IDOSO && fisica.getUsuario() != null)
            throw new BusinessException("Remova o acesso ao sistema antes de alterar o tipo de cadastro para Idoso.");

        String novoUsuario = normalizar(request.usuario());
        boolean acessoOmitido = request.usuario() == null && request.senha() == null && request.perfil() == null;
        boolean usuarioAlterado = !acessoOmitido && !Objects.equals(fisica.getUsuario(), novoUsuario);
        boolean perfilAlterado = !acessoOmitido && !Objects.equals(fisica.getPerfil(), request.perfil());
        boolean senhaAlterada = !acessoOmitido && normalizar(request.senha()) != null;
        if (usuarioAlterado || perfilAlterado || senhaAlterada) {
            exigirAdministrador();
            if (perfilAlterado && Objects.equals(id, idPessoaAutenticada()))
                throw new BusinessException("O usuário autenticado não pode alterar o próprio perfil de acesso.");
        }

        validarConflitosFisica(cpf, novoUsuario, id);
        preencherDadosComuns(fisica, request.telefone(), request.cep(), request.logradouro(), request.numero(),
                request.bairro(), request.cidade(), request.estado());
        preencherFisica(fisica, request, cpf);
        if (!(acessoOmitido && fisica.getUsuario() != null)) configurarAcesso(fisica, request, false);
        return toResponse(fisicaRepository.save(fisica));
    }

    @Transactional
    public void removerAcesso(Long id) {
        exigirAdministrador();
        Fisica fisica = fisicaRepository.findById(id).filter(Pessoa::isStatus)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa física não encontrada."));
        if (Objects.equals(id, idPessoaAutenticada()))
            throw new BusinessException("O usuário autenticado não pode remover o próprio acesso.");
        fisica.setUsuario(null);
        fisica.setSenha(null);
        fisica.setPerfil(null);
        fisicaRepository.save(fisica);
    }

    @Transactional
    public PessoaResponse cadastrarJuridica(JuridicaRequest request) {
        validarTipoJuridica(request);
        String cnpj = somenteNumeros(request.cnpj());
        validarCnpj(cnpj);
        validarConflitoCnpj(cnpj);
        Juridica juridica = new Juridica();
        preencherDadosComuns(juridica, request.telefone(), request.cep(), request.logradouro(), request.numero(),
                request.bairro(), request.cidade(), request.estado());
        juridica.setRazaoSocial(request.razaoSocial().trim());
        juridica.setCnpj(cnpj);
        ativarNovaPessoa(juridica);
        return toResponse(juridicaRepository.save(juridica));
    }

    @Transactional
    public PessoaResponse atualizarJuridica(Long id, JuridicaRequest request) {
        validarTipoJuridica(request);
        Juridica juridica = juridicaRepository.findById(id).filter(Pessoa::isStatus)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa jurídica não encontrada."));
        String cnpj = somenteNumeros(request.cnpj());
        validarCnpj(cnpj);
        if (!juridica.getCnpj().equals(cnpj)) throw new BusinessException("CNPJ não pode ser alterado.");
        preencherDadosComuns(juridica, request.telefone(), request.cep(), request.logradouro(), request.numero(),
                request.bairro(), request.cidade(), request.estado());
        juridica.setRazaoSocial(request.razaoSocial().trim());
        return toResponse(juridicaRepository.save(juridica));
    }

    @Transactional
    public void inativar(Long id) {
        Pessoa pessoa = buscarPessoaAtiva(id);
        if (pessoa instanceof Fisica fisica) {
            if (Objects.equals(id, idPessoaAutenticada()))
                throw new BusinessException("O usuário autenticado não pode inativar a si mesmo.");
            if (fisica.getUsuario() != null) exigirAdministrador();
        }
        pessoa.setStatus(false);
        pessoa.setDataInativacao(LocalDateTime.now());
        pessoaRepository.save(pessoa);
    }

    @Transactional
    public void reativar(Long id) {
        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada."));
        if (pessoa.isStatus()) throw new BusinessException("A pessoa já está ativa.");
        if (pessoa instanceof Fisica f && f.getUsuario() != null) exigirAdministrador();
        pessoa.setStatus(true);
        pessoa.setDataInativacao(null);
        pessoaRepository.save(pessoa);
    }

    private Pessoa buscarPessoaAtiva(Long id) {
        return pessoaRepository.findById(id).filter(Pessoa::isStatus)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada."));
    }

    private void configurarAcesso(Fisica fisica, FisicaRequest request, boolean cadastro) {
        String usuario = normalizar(request.usuario());
        String senha = normalizar(request.senha());
        if (usuario == null && request.perfil() == null && senha == null) {
            fisica.setUsuario(null);
            fisica.setSenha(null);
            fisica.setPerfil(null);
            return;
        }
        if (usuario == null || request.perfil() == null)
            throw new BusinessException("Usuário e perfil devem ser informados para conceder acesso.");
        if ((cadastro || fisica.getSenha() == null) && senha == null)
            throw new BusinessException("A senha é obrigatória para conceder acesso.");
        if (senha != null && senha.length() < 8)
            throw new BusinessException("A senha deve ter no mínimo 8 caracteres.");
        fisica.setUsuario(usuario);
        fisica.setPerfil(request.perfil());
        if (senha != null) fisica.setSenha(passwordEncoder.encode(senha));
    }

    private boolean possuiDadosDeAcesso(FisicaRequest r) {
        return normalizar(r.usuario()) != null || normalizar(r.senha()) != null || r.perfil() != null;
    }

    private void validarRegrasFisica(FisicaRequest request) {
        if (request.tipoCadastro() == null)
            throw new BusinessException("O tipo de cadastro é obrigatório.");
        List<ContatoFamiliarRequest> contatos = request.contatosFamiliares() == null
                ? List.of() : request.contatosFamiliares();
        if (request.tipoCadastro() == TipoCadastro.IDOSO) {
            if (possuiDadosDeAcesso(request))
                throw new BusinessException("Pessoa física do tipo Idoso não pode possuir acesso ao sistema.");
            if (contatos.isEmpty())
                throw new BusinessException("O cadastro de Idoso deve possuir pelo menos um contato familiar.");
            contatos.forEach(this::validarContatoFamiliar);
        } else if (!contatos.isEmpty()) {
            throw new BusinessException("Contatos familiares são exclusivos do cadastro de Idoso.");
        }
        if (request.familiaId() == null && request.vinculoFamiliar() != null)
            throw new BusinessException("O vínculo familiar exige uma família selecionada.");
        if (request.familiaId() != null && request.vinculoFamiliar() == null)
            throw new BusinessException("Informe o vínculo da pessoa com a família selecionada.");
    }

    private void validarContatoFamiliar(ContatoFamiliarRequest contato) {
        if (contato == null || normalizar(contato.nome()) == null || contato.dataNascimento() == null
                || contato.idade() == null || contato.vinculoFamiliar() == null
                || somenteNumeros(contato.telefone()).isBlank())
            throw new BusinessException("Preencha todos os dados do contato familiar.");
        if (contato.dataNascimento().isAfter(LocalDate.now()))
            throw new BusinessException("A data de nascimento do contato familiar não pode ser futura.");
        int idadeCalculada = Period.between(contato.dataNascimento(), LocalDate.now()).getYears();
        if (idadeCalculada != contato.idade())
            throw new BusinessException("A idade do contato familiar não corresponde à data de nascimento.");
    }

    private void validarTipoJuridica(JuridicaRequest request) {
        if (request.tipoCadastro() != TipoCadastro.PESSOA)
            throw new BusinessException("Pessoa jurídica somente pode utilizar o tipo de cadastro Pessoa.");
    }

    private void exigirAdministrador() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean admin = auth != null && auth.isAuthenticated() && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMINISTRADOR".equals(a.getAuthority()));
        if (!admin) throw new AccessDeniedException(
                "Somente ADMINISTRADOR pode conceder, alterar ou remover acesso ao sistema.");
    }

    private Long idPessoaAutenticada() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        if (auth.getPrincipal() instanceof Jwt jwt) {
            Number id = jwt.getClaim("pessoaId");
            return id == null ? null : id.longValue();
        }
        return fisicaRepository.findByUsuarioAndStatusTrue(auth.getName()).map(Fisica::getId).orElse(null);
    }

    private void validarConflitosFisica(String cpf, String usuario, Long id) {
        boolean cpfUsado = id == null ? fisicaRepository.existsByCpf(cpf) : fisicaRepository.existsByCpfAndIdNot(cpf, id);
        if (cpfUsado) throw new ConflictException("CPF já cadastrado.");
        if (usuario != null) {
            boolean usuarioUsado = id == null ? fisicaRepository.existsByUsuario(usuario)
                    : fisicaRepository.existsByUsuarioAndIdNot(usuario, id);
            if (usuarioUsado) throw new ConflictException("Usuário já cadastrado.");
        }
    }

    private void validarConflitoCnpj(String cnpj) {
        if (juridicaRepository.existsByCnpj(cnpj)) throw new ConflictException("CNPJ já cadastrado.");
    }

    private void validarIdade(LocalDate data, Integer idadeInformada) {
        if (data != null && data.isAfter(LocalDate.now()))
            throw new BusinessException("A data de nascimento não pode ser futura.");
        if (data == null && idadeInformada == null)
            throw new BusinessException("Informe a data de nascimento ou a idade.");
    }

    private void ativarNovaPessoa(Pessoa pessoa) {
        pessoa.setStatus(true);
        pessoa.setDataCriacao(LocalDateTime.now());
        pessoa.setDataInativacao(null);
    }

    private void preencherDadosComuns(Pessoa p, String telefone, String cep, String logradouro,
                                      String numero, String bairro, String cidade, String estado) {
        p.setTelefone(somenteNumeros(telefone));
        p.setCep(somenteNumeros(cep));
        p.setLogradouro(logradouro.trim());
        p.setNumero(numero.trim());
        p.setBairro(bairro.trim());
        p.setCidade(cidade.trim());
        p.setEstado(estado.trim().toUpperCase());
    }

    private void preencherFisica(Fisica f, FisicaRequest r, String cpf) {
        f.setNome(r.nome().trim());
        f.setCpf(cpf);
        f.setDataNascimento(r.dataNascimento());
        f.setIdadeInformada(r.dataNascimento() == null ? r.idadeInformada() : null);
        f.setEmail(normalizar(r.email()));
        f.setNomeMae(normalizar(r.nomeMae()));
        f.setSexo(r.sexo());
        f.setEstadoCivil(r.estadoCivil());
        f.setRg(normalizar(r.rg()));
        f.setNis(normalizar(r.nis()));
        f.setEscolaridade(r.escolaridade());
        f.setOcupacao(normalizar(r.ocupacao()));
        f.setContato2(normalizar(r.contato2()));
        f.setTipoCadastro(r.tipoCadastro());
        vincularFamilia(f, r.familiaId(), r.vinculoFamiliar());
        List<ContatoFamiliar> contatos = (r.contatosFamiliares() == null ? List.<ContatoFamiliarRequest>of()
                : r.contatosFamiliares()).stream().map(this::toContatoFamiliar).toList();
        f.substituirContatosFamiliares(contatos);
    }

    private void vincularFamilia(Fisica fisica, Long familiaId, VinculoFamiliar vinculo) {
        if (familiaId == null) {
            fisica.setFamilia(null);
            fisica.setVinculoFamiliar(null);
            return;
        }
        Familia familia = familiaRepository.findById(familiaId).filter(Familia::isStatus)
                .orElseThrow(() -> new ResourceNotFoundException("Família ativa não encontrada."));
        fisica.setFamilia(familia);
        fisica.setVinculoFamiliar(vinculo);
    }

    private ContatoFamiliar toContatoFamiliar(ContatoFamiliarRequest request) {
        ContatoFamiliar contato = new ContatoFamiliar();
        contato.setNome(request.nome().trim());
        contato.setDataNascimento(request.dataNascimento());
        contato.setVinculoFamiliar(request.vinculoFamiliar());
        contato.setTelefone(somenteNumeros(request.telefone()));
        return contato;
    }

    private String nome(Pessoa p) { return p instanceof Fisica f ? f.getNome() : ((Juridica) p).getRazaoSocial(); }
    private String normalizar(String v) { return v == null || v.isBlank() ? null : v.trim(); }
    private String somenteNumeros(String v) { return v == null ? "" : v.replaceAll("\\D", ""); }

    private void validarCpf(String cpf) {
        if (cpf.length() != 11 || cpf.chars().distinct().count() == 1 || !documentoValido(cpf, 9))
            throw new BusinessException("CPF inválido.");
    }

    private void validarCnpj(String cnpj) {
        if (cnpj.length() != 14 || cnpj.chars().distinct().count() == 1 || !documentoValido(cnpj, 12))
            throw new BusinessException("CNPJ inválido.");
    }

    private boolean documentoValido(String d, int base) {
        int[] cpf1 = {10,9,8,7,6,5,4,3,2}, cpf2 = {11,10,9,8,7,6,5,4,3,2};
        int[] cnpj1 = {5,4,3,2,9,8,7,6,5,4,3,2}, cnpj2 = {6,5,4,3,2,9,8,7,6,5,4,3,2};
        return d.charAt(base) - '0' == digito(d, base == 9 ? cpf1 : cnpj1)
                && d.charAt(base + 1) - '0' == digito(d, base == 9 ? cpf2 : cnpj2);
    }

    private int digito(String d, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) soma += (d.charAt(i) - '0') * pesos[i];
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    public PessoaResponse toResponse(Pessoa pessoa) {
        if (pessoa instanceof Fisica f) {
            List<ContatoFamiliarResponse> contatos = f.getContatosFamiliares().stream()
                    .map(c -> new ContatoFamiliarResponse(c.getId(), c.getNome(), c.getDataNascimento(),
                            c.getIdade(), c.getVinculoFamiliar(), c.getTelefone()))
                    .toList();
            return new PessoaResponse(f.getId(), "FISICA", f.getTipoCadastro(), f.getTelefone(), f.getCep(), f.getLogradouro(),
                    f.getNumero(), f.getBairro(), f.getCidade(), f.getEstado(), f.isStatus(), f.getDataCriacao(),
                    f.getDataInativacao(), f.getNome(), f.getCpf(), f.getDataNascimento(), f.getIdadeEfetiva(), f.getEmail(),
                    f.getNomeMae(), f.getSexo(), f.getEstadoCivil(), f.getRg(), f.getNis(), f.getEscolaridade(),
                    f.getOcupacao(), f.getContato2(), f.getUsuario(), f.getPerfil(),
                    f.getFamilia() == null ? null : f.getFamilia().getId(), f.getVinculoFamiliar(), contatos,
                    null, null);
        }
        Juridica j = (Juridica) pessoa;
        return new PessoaResponse(j.getId(), "JURIDICA", TipoCadastro.PESSOA, j.getTelefone(), j.getCep(), j.getLogradouro(),
                j.getNumero(), j.getBairro(), j.getCidade(), j.getEstado(), j.isStatus(), j.getDataCriacao(),
                j.getDataInativacao(), null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, j.getRazaoSocial(), j.getCnpj());
    }
}
