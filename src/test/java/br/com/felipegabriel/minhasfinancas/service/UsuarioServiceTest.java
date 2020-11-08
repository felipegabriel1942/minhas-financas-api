package br.com.felipegabriel.minhasfinancas.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.felipegabriel.minhasfinancas.exceptions.ErroAutenticacao;
import br.com.felipegabriel.minhasfinancas.exceptions.RegraNegocioException;
import br.com.felipegabriel.minhasfinancas.model.entity.Usuario;
import br.com.felipegabriel.minhasfinancas.model.repository.UsuarioRepository;
import br.com.felipegabriel.minhasfinancas.service.impl.UsuarioServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class UsuarioServiceTest {

	@SpyBean
	UsuarioServiceImpl service;

	@MockBean
	UsuarioRepository repository;

	@Test
	public void deveSalvarUmUsuario() {
		
		assertDoesNotThrow(() -> {
			
			Mockito.doNothing().when(service).validarEmail(Mockito.anyString());
			Usuario usuario = Usuario.builder().id(1L).nome("nome").email("email@email.com").senha("senha").build();
			
			Mockito.when(repository.save(Mockito.any(Usuario.class))).thenReturn(usuario);

			Usuario usuarioSalvo = service.salvarUsuario(new Usuario());
			
			Assertions.assertThat(usuarioSalvo).isNotNull();
			Assertions.assertThat(usuarioSalvo.getId()).isEqualTo(1);
			Assertions.assertThat(usuarioSalvo.getNome()).isEqualTo("nome");
			Assertions.assertThat(usuarioSalvo.getEmail()).isEqualTo("email@email.com");
			Assertions.assertThat(usuarioSalvo.getSenha()).isEqualTo("senha");
			
		});		
	}
	
	@Test
	public void naoDeveSalvarUmUsuarioComEmailJaCadastrado() {
		
		assertThrows(RegraNegocioException.class, () -> {
			String email = "email@email.com";
			Usuario usuario = Usuario.builder().email(email).build();
			Mockito.doThrow(RegraNegocioException.class).when(service).validarEmail(email);
			
			service.salvarUsuario(usuario);
			
			Mockito.verify(repository, Mockito.never()).save(usuario);
		});		
	}

	@Test
	public void deveAutenticarUmUsuarioComSucesso() {
		
		assertDoesNotThrow(() -> {
			String email = "email@email.com";
			String senha = "senha";

			Usuario usuario = Usuario.builder().email(email).senha(senha).id(1L).build();
			Mockito.when(repository.findByEmail(email)).thenReturn(Optional.of(usuario));

			Usuario result = service.autenticar(email, senha);

			Assertions.assertThat(result).isNotNull();
		});
	}

	@Test
	public void deveLancarErroQuandoNaoEncontrarUsuarioCadastradoComEmailInformado() {

		Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());

		Throwable exception = Assertions.catchThrowable(() -> service.autenticar("email@email.com", "123"));

		Assertions.assertThat(exception).isInstanceOf(ErroAutenticacao.class)
				.hasMessage("Usuário não encontrado para o email informado.");

	}

	@Test
	public void deveLancarErroQuandoSenhaNaoBater() {

		String senha = "senha";
		Usuario usuario = Usuario.builder().email("email@email.com").senha(senha).build();
		Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(usuario));

		Throwable exception = Assertions.catchThrowable(() -> service.autenticar("email@email.com", "123"));
		Assertions.assertThat(exception).isInstanceOf(ErroAutenticacao.class).hasMessage("Senha inválida");

	}

	@Test
	public void deveValidarEmail() {
		
		assertDoesNotThrow(() -> {
			Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(false);
			service.validarEmail("email@email.com");
		});
	}

	@Test
	public void deveLancarErroAoValidarEmailQuandoExistirEmailCadastrado() {

		assertThrows(RegraNegocioException.class, () -> {
			Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(true);
			service.validarEmail("email@email.com");
		});

	}

}
