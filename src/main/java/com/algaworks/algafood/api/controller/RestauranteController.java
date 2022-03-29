package com.algaworks.algafood.api.controller;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.algaworks.algafood.domain.model.Restaurante;
import com.algaworks.algafood.domain.repository.RestauranteRepository;
import com.algaworks.algafood.domain.service.CadastroRestauranteService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/restaurantes")
public class RestauranteController {

	@Autowired
	private RestauranteRepository restauranteRepository;

	@Autowired
	private CadastroRestauranteService cadastroRestauranteService;

	@GetMapping
	public List<Restaurante> listar() {
		return restauranteRepository.findAll();
	}

	@GetMapping("/{restauranteID}")
	public Restaurante buscar(@PathVariable Long restauranteID) {
		return cadastroRestauranteService.buscarOuFalhar(restauranteID);

	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Restaurante adicionar(@RequestBody Restaurante restaurante) {
		return cadastroRestauranteService.salvar(restaurante);

	}

	@PutMapping("/{restauranteId}")
	public Restaurante atualizar(@PathVariable Long restauranteId, @RequestBody Restaurante restaurante) {
		Restaurante restauranteAtual = cadastroRestauranteService.buscarOuFalhar(restauranteId);

		BeanUtils.copyProperties(restaurante, restauranteAtual, "id", "formasPagamento", "endereco", "dataCadastro",
				"produtos");
		return cadastroRestauranteService.salvar(restauranteAtual);

	}

	@PatchMapping("/{restauranteId}")
	public Restaurante atualizarParcial(@PathVariable Long restauranteId, @RequestBody Map<String, Object> campos) {
		Restaurante restauranteAtual = cadastroRestauranteService.buscarOuFalhar(restauranteId);

		merge(campos, restauranteAtual);

		return atualizar(restauranteId, restauranteAtual);
	}

	/**
	 * Cada propriedade dos dadosOrigem que veio no mapa deve ser passada para
	 * restauranteDestino usando a API de Reflections do Spring
	 */
	private void merge(Map<String, Object> dadosOrigem, Restaurante restauranteDestino) {
		/*
		 * ObjectMapper responsavel por converter(serializar) json em Obj Java ou vice versa
		 * objectMapper.convertValue(dadosOrigem, Restaurante.class) esta convertendo o que esta vindo
		 * no corpo para restaurante
		 */
		ObjectMapper objectMapper = new ObjectMapper();
		Restaurante restauranteOrigem = objectMapper.convertValue(dadosOrigem, Restaurante.class);

		dadosOrigem.forEach((nomePropriedade, valorPropriedade)-> {
			/*Declaração do Field do java.lang
			 * field representa o atributo de restaurante que queremos modificar
			 * ReflectionUtils é do Spring
			 * o findField busca na classe Restaurante a propriedade que esta vindo em tempo de execução
			 * REtorna a instancia de um campo
			 */
			Field field = ReflectionUtils.findField(Restaurante.class, nomePropriedade);

			//Usado para permitir acesso aos metodos privados da classe Restaurante. Torna a variavel privada acessivel
			field.setAccessible(true);

			/*
			 * retorna o valor de um campo, o valor da propriedade
			 */
			Object novoValor = ReflectionUtils.getField(field, restauranteOrigem);

			/*pega o valor na recuperado na instancia Field field e modifica para o novo valor
			 *
			 */
			ReflectionUtils.setField(field, restauranteDestino, novoValor);
		});



	}
}
