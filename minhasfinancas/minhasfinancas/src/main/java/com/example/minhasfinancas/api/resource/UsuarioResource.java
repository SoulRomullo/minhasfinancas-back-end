package com.example.minhasfinancas.api.resource;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.minhasfinancas.api.dto.UsuarioDTO;
import com.example.minhasfinancas.exception.ErroAutenticacao;
import com.example.minhasfinancas.exception.RegraNegocioException;
import com.example.minhasfinancas.model.entity.Usuario;
import com.example.minhasfinancas.service.LancamentoService;
import com.example.minhasfinancas.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@Configuration
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioResource {

	private final UsuarioService service;
	private final LancamentoService lancamentoService;
	
	// Com a anotação @RequiredArgsConstructor faz o funcionamento do comando abaixo
	// Não precisando passar os parâmetros criados para UsuarioResource o construtor
	/*public UsuarioResource(UsuarioService service) {
		this.service = service;
	}*/
	
	@PostMapping("/autenticar")
	public ResponseEntity<Usuario> autenticar( @RequestBody UsuarioDTO dto) {
		try {
			
			Usuario usuarioAutenticado = service.autenticar(dto.getEmail(), dto.getSenha());
			return ResponseEntity.ok(usuarioAutenticado);
			
		} catch (ErroAutenticacao e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	@PostMapping
	public ResponseEntity<Usuario>salvar( @RequestBody UsuarioDTO dto) {
		Usuario usuario = Usuario.builder().nome(dto.getNome())
				.email(dto.getEmail())
				.senha(dto.getSenha()).build();
		
		try {
			
			Usuario usuarioSalvo = service.salvarUsuario(usuario);
			return new ResponseEntity<>(usuarioSalvo, HttpStatus.CREATED);
			
		} catch (RegraNegocioException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	@GetMapping("{id}/saldo")
	public ResponseEntity<BigDecimal> obterSaldo(@PathVariable("id") Long id) {
		Optional<Usuario> usuario = service.obterPorId(id);
		
		if(!usuario.isPresent()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		BigDecimal saldo =lancamentoService.obterSaldoPorUsuario(id);
		return ResponseEntity.ok(saldo);
	}
	
}
