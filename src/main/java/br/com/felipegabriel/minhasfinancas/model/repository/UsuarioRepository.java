package br.com.felipegabriel.minhasfinancas.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.felipegabriel.minhasfinancas.model.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>{

}
