package com.br.food.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.enums.Status.StatusNotaFiscal;
import com.br.food.forms.NotaFiscalForm;
import com.br.food.models.Estoque;
import com.br.food.models.NotaFiscal;
import com.br.food.models.Produto;
import com.br.food.repository.NotaFiscalRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class NotaFiscalService {

	@Autowired
	private NotaFiscalRepository notaFiscalRepository;

	@Autowired
	private ProdutoService produtoService;

	@Transactional
	public NotaFiscal cadastroNotaFiscal(NotaFiscalForm form) {

		validaNotaJaRegistrada(form.getChaveNFE());
		NotaFiscal notaFiscal = new NotaFiscal(form);

		form.getItens().forEach(i -> {
			Produto produto = produtoService.buscarProdutoPorId(i.getIdProduto());
			notaFiscal.getItens().add(new Estoque(i, produto));
		});
		return notaFiscalRepository.save(notaFiscal);
	}

	@Transactional(readOnly = true)
	public NotaFiscal buscarNotaFiscalPorId(Long id) {
		return notaFiscalRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Nota Fiscal não encontrada para ID " + id));
	}

	@Transactional(readOnly = true)
	public void validaNotaJaRegistrada(String chaveNFE) {
		notaFiscalRepository.findByChaveNFEAndStatusNot(chaveNFE, StatusNotaFiscal.CANCELADA).ifPresent(nota -> {
			throw new DataIntegrityViolationException(
					"Já existe nota para esta chave! Verifique a nota e tente novamente.");
		});
	}

	@Transactional
	public NotaFiscal atualizarNotaFiscal(NotaFiscalForm form, Long id) {
		NotaFiscal notaFiscal = buscarNotaFiscalPorId(id);

		if (form.getChaveNFE() != notaFiscal.getChaveNFE()) {
			validaNotaJaRegistrada(form.getChaveNFE());
		}

		notaFiscal.setChaveNFE(form.getChaveNFE());
		notaFiscal.setDataEmissao(form.getDataEmissao());
		notaFiscal.setNumeroNota(form.getNumeroNota());
		notaFiscal.setNumeroSerie(form.getNumeroSerie());

		notaFiscal.getItens().clear();

		form.getItens().forEach(i -> {
			Produto produto = produtoService.buscarProdutoPorId(i.getIdProduto());
			notaFiscal.getItens().add(new Estoque(i, produto));
		});
		return notaFiscalRepository.save(notaFiscal);
	}

	@Transactional
	public void alterarStatusNotaFiscal(StatusNotaFiscal status, Long idNotaFiscal) {
		NotaFiscal notaFiscal = buscarNotaFiscalPorId(idNotaFiscal);

		validarAlteracaoStatus(notaFiscal, status);
		notaFiscal.setStatus(status);

	}

	private void validarAlteracaoStatus(NotaFiscal notaFiscal, StatusNotaFiscal novoStatus) {
		StatusNotaFiscal statusAtual = notaFiscal.getStatus();

		// Impede alterações a partir de CANCELADA
		if (statusAtual == StatusNotaFiscal.CANCELADA) {
			throw new DataIntegrityViolationException("Uma nota fiscal cancelada não pode ter seu status alterado");
		}

		// Impede cancelamento de notas em consumo ou consumidas (validação original)
		boolean isEmConsumoParaCancelada = statusAtual == StatusNotaFiscal.EM_CONSUMO
				&& novoStatus == StatusNotaFiscal.CANCELADA;
		boolean isConsumidaParaCancelada = statusAtual == StatusNotaFiscal.CONSUMIDA
				&& novoStatus == StatusNotaFiscal.CANCELADA;
		if (isEmConsumoParaCancelada || isConsumidaParaCancelada) {
			throw new DataIntegrityViolationException(
					"Não é possível cancelar uma nota fiscal que já entrou em consumo ou foi consumida");
		}

		// Impede retrocesso de CONSUMIDA para outros estados exceto CANCELADA
		if (statusAtual == StatusNotaFiscal.CONSUMIDA && novoStatus != StatusNotaFiscal.CANCELADA) {
			throw new DataIntegrityViolationException(
					"Uma nota fiscal consumida só pode ser cancelada, sem retrocesso");
		}

		// Impede transição direta de EMITIDA para CONSUMIDA
		if (statusAtual == StatusNotaFiscal.ALOCADA && novoStatus == StatusNotaFiscal.CONSUMIDA) {
			throw new DataIntegrityViolationException(
					"Uma nota emitida não pode ser diretamente marcada como consumida");
		}
	}
}
