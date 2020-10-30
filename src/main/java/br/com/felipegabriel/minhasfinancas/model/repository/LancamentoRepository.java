package br.com.felipegabriel.minhasfinancas.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.felipegabriel.minhasfinancas.model.entity.Lancamento;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long>{

}
