package br.com.felipegabriel.minhasfinancas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Example;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.felipegabriel.minhasfinancas.enums.StatusLancamento;
import br.com.felipegabriel.minhasfinancas.exceptions.RegraNegocioException;
import br.com.felipegabriel.minhasfinancas.model.entity.Lancamento;
import br.com.felipegabriel.minhasfinancas.model.entity.Usuario;
import br.com.felipegabriel.minhasfinancas.model.repository.LancamentoRepository;
import br.com.felipegabriel.minhasfinancas.model.repository.LancamentoRepositoryTest;
import br.com.felipegabriel.minhasfinancas.service.impl.LancamentoServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LancamentoServiceTest {
	
	@SpyBean
	LancamentoServiceImpl service;
	
	@MockBean
	LancamentoRepository repository;
	
	@Test
	public void deveSalvarUmLancamento() {
		Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();
		
		Lancamento lancamentoSalvo = LancamentoRepositoryTest.criarLancamento();
		lancamentoSalvo.setId(1L);
		lancamentoSalvo.setStatus(StatusLancamento.PENDENTE);
		
		Mockito.doNothing().when(service).validar(lancamentoASalvar);
		Mockito.when(repository.save(lancamentoASalvar)).thenReturn(lancamentoSalvo);
		
		Lancamento lancamento = service.salvar(lancamentoASalvar);
		
		assertThat(lancamento.getId()).isEqualTo(lancamentoSalvo.getId());
		assertThat(lancamento.getStatus()).isEqualTo(StatusLancamento.PENDENTE);
	}
	
	@Test
	public void naoDeveSalvarUmLancamentoQuandoHouverErroDeValidacao() {
		Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();
		doThrow(RegraNegocioException.class).when(service).validar(lancamentoASalvar);
		
		catchThrowableOfType(() -> service.salvar(lancamentoASalvar), RegraNegocioException.class);
		
		verify(repository, never()).save(lancamentoASalvar);
	}
	
	@Test
	public void deveAtualizarUmLancamento() {
		
		Lancamento lancamentoSalvo = LancamentoRepositoryTest.criarLancamento();
		lancamentoSalvo.setId(1L);
		lancamentoSalvo.setStatus(StatusLancamento.PENDENTE);
		
		doNothing().when(service).validar(lancamentoSalvo);
		when(repository.save(lancamentoSalvo)).thenReturn(lancamentoSalvo);
		
		service.atualizar(lancamentoSalvo);
		
		verify(repository, times(1)).save(lancamentoSalvo);

	}
	
	@Test
	public void deveLancarErroAoTentarAtualizarUmLancamentoQueAindaNaoFoiSalvo() {
		Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();
		
		catchThrowableOfType(() -> service.atualizar(lancamentoASalvar), NullPointerException.class);
		verify(repository, never()).save(lancamentoASalvar);
	}
	
	@Test
	public void deveDeletarUmLancamento() {
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(1L);
		
		service.deletar(lancamento);
		
		verify(repository).delete(lancamento);
	}
	
	@Test
	public void deveLancarErroAoTentarDeletarUmLancamentoQueAindaNaoFoiSalvo() {
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		
		catchThrowableOfType(() -> service.deletar(lancamento), NullPointerException.class);
		
		verify(repository, never()).delete(lancamento);
	}
	
	@Test
	public void deveFiltrarLancamento() {
		
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(1L);
		
		List<Lancamento> lista = Arrays.asList(lancamento);
		
		when(repository.findAll(any(Example.class))).thenReturn(lista);
		
		List<Lancamento> resultado = service.buscar(lancamento);
		
		assertThat(resultado)
			.isNotEmpty()
			.hasSize(1)
			.contains(lancamento);
	}
	
	@Test
	public void deveAtualizarOStatusDeUmLancamento() {
		
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(1L);
		lancamento.setStatus(StatusLancamento.PENDENTE);
		
		StatusLancamento novoStatus = StatusLancamento.EFETIVADO;
		doReturn(lancamento).when(service).atualizar(lancamento);
		
		service.atualizarStatus(lancamento, novoStatus);
		
		assertThat(lancamento.getStatus()).isEqualTo(novoStatus);
		Mockito.verify(service).atualizar(lancamento);
	}
	
	@Test
	public void deveObterUmLancamentoPorId() {
		
		Long id = 1l;
		
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(1L);
		
		Mockito.when(repository.findById(id)).thenReturn(Optional.of(lancamento));
		
		Optional<Lancamento> resultado = service.obterPorId(id);
		
		assertThat(resultado.isPresent()).isTrue();
	}
	
	@Test
	public void deveRetornarVazioQuandoOLancamentoNaoExiste() {
		
		Long id = 1l;
		
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(1L);
		
		Mockito.when(repository.findById(id)).thenReturn(Optional.empty());
		
		Optional<Lancamento> resultado = service.obterPorId(id);
		
		assertThat(resultado.isPresent()).isFalse();
	}
	
	@Test
	public void deveLancarErrosAoValidarUmLancamento() {
		Lancamento lancamento = new Lancamento();
		
		Throwable erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe uma descrição válida.");
		
		lancamento.setDescricao("");
		
		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe uma descrição válida.");
		
		lancamento.setDescricao("Salario");
		
		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um mês válido.");
		
		lancamento.setAno(0);
		
		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um mês válido.");
		
		lancamento.setAno(13);
		
		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um mês válido.");
		
		lancamento.setMes(1);
		
		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um ano válido.");
		
		lancamento.setAno(2020);
		
		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um usuário.");
		
		lancamento.setUsuario(new Usuario());
		lancamento.getUsuario().setId(1L);
		
		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um valor válido.");
		
		lancamento.setValor(BigDecimal.ZERO);
		
		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um valor válido.");
		
		lancamento.setValor(BigDecimal.valueOf(1));
		
		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um tipo de lançamento");
		
	}
}
