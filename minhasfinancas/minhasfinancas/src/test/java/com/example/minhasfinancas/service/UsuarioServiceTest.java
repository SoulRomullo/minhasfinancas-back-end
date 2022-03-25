package com.example.minhasfinancas.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.example.minhasfinancas.exception.ErroAutenticacao;
import com.example.minhasfinancas.exception.RegraNegocioException;
import com.example.minhasfinancas.model.entity.Usuario;
import com.example.minhasfinancas.model.repository.UsuarioRepository;
import com.example.minhasfinancas.service.impl.UsuarioServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class UsuarioServiceTest {
	
	@SpyBean
	UsuarioServiceImpl service;
	
	@MockBean
	UsuarioRepository repository;
	
	//@BeforeEach
	//public void setUp() {
		
		//service = Mockito.spy(UsuarioServiceTest.class);
		
		//O @MockBean substitui o comando abaixo e ele já reconhece no repository
		//repository = Mockito.mock(UsuarioRepository.class);
		//service = new UsuarioServiceImpl(repository);
		
	//}
	
	@Test
	public void deveSalvarUmUsuario() {
		
		//cenário
		Mockito.doNothing().when(service).validarEmail(Mockito.anyString());
		Usuario usuario = Usuario.builder()
				.id(1l)
				.nome("nome")
				.email("email@email.com")
				.senha("senha").build();
		
		Mockito.when(repository.save(Mockito.any(Usuario.class))).thenReturn(usuario);
		
		//ação
		Usuario usuarioSalvo = service.salvarUsuario(new Usuario());
		
		//Verificação
		assertThat(usuarioSalvo).isNotNull();
		assertThat(usuarioSalvo.getId()).isEqualTo(1l);
		assertThat(usuarioSalvo.getNome()).isEqualTo("nome");
		assertThat(usuarioSalvo.getEmail()).isEqualTo("email@email.com");
		assertThat(usuarioSalvo.getSenha()).isEqualTo("senha");
		
		
	}
	
	@Test
	public void naoDeveSalvarUmUsuarioComEmailJaCadastrado() {
			//cenário
			String email = "email@email.com";
			Usuario usuario = Usuario.builder().email(email).build();
			Mockito.doThrow(RegraNegocioException.class).when(service).validarEmail(email);
			
			//ação
			Assertions.assertThrows(RegraNegocioException.class, () -> service.salvarUsuario(usuario)) ;
			
			//Verificação
			Mockito.verify(repository, Mockito.never()).save(usuario);
	}
	
	@Test
	public void deveAutenticarUmUsuarioComSucesso() {
			// cenário
			String email = "email@email.com";
			String senha = "senha";
			
			Usuario usuario = Usuario.builder().email(email).senha(senha).id(1l).build();
			Mockito.when(repository.findByEmail(email)).thenReturn(Optional.of(usuario));
			
			// ação
			Usuario result = service.autenticar(email, senha);
			
			// verificação
			assertThat(result).isNotNull();		
	}
	
	@Test
	public void deveLancarErroQuandoNaoEncontrarUsuarioCadastradoComOEmailInformado() {
		
			//cenário
			Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());
			
			//ação
			Throwable exception =  catchThrowable( () -> service.autenticar("email@email.com", "senha"));
			
			
			//Verificação
			assertThat(exception)
			.isInstanceOf(ErroAutenticacao.class)
			.hasMessage("Usuário não encontrado para o email informado.");
			
			//Esse comando e todo em um única linha mais quebrei as linhas
			//para melhor visualização
	}
	
	@Test
	public void deveLancarErroQuandoSenhaNaoBater() {
		//cenário
		String senha ="senha";
		Usuario usuario = Usuario.builder().email("email@email.com").senha(senha).build();
		Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(usuario));
			
		//ação
		Throwable exception = catchThrowable( () ->  service.autenticar("email@email.com", "123"));
		
		//Verificação
		assertThat(exception).isInstanceOf(ErroAutenticacao.class).hasMessage("Senha inválida.");
	}
		
	
	@Test
	public void deveValidarEmail() {
			// Cenário
			Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(false);
			
			// ação
			service.validarEmail("email@email.com");

	}
	
	
	@Test
	public void deveLancarErroAoValidarEmailQuandoExistirEmailCadastrado() {
			
			// cenário
			Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(true);
			
			// ação
			Assertions.assertThrows(RegraNegocioException.class, () -> service.validarEmail("email@email.com"));
		
	}

}
