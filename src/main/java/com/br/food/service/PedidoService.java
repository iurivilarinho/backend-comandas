package com.br.food.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.AccessDeniedException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.enums.Status.StatusItem;
import com.br.food.enums.Status.StatusPedido;
import com.br.food.enums.Tipos.TipoPagamento;
import com.br.food.enums.Tipos.TipoPedido;
import com.br.food.forms.PedidoForm;
import com.br.food.models.Cliente;
import com.br.food.models.Evento;
import com.br.food.models.FormaDePagamento;
import com.br.food.models.ItemPedido;
import com.br.food.models.Mesa;
import com.br.food.models.Pedido;
import com.br.food.repository.PedidoRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PedidoService {

	@Autowired
	private PedidoRepository pedidoRepository;

	@Autowired
	private ClienteService clienteService;

	@Autowired
	private MesaService mesaService;

	@Autowired
	private EventoService eventoService;

	@Autowired
	private FormaDePagamentoService formaDePagamentoService;

	@Transactional
	public Pedido cadastrarPedido(PedidoForm form) throws AccessDeniedException {

		Cliente cliente = clienteService.buscarClientePorId(form.getClienteId());
		Mesa mesa = mesaService.buscarMesaPorNumero(form.getNumeroMesa());
		Pedido pedido = new Pedido(form, cliente, gerarNumeroPedido(), mesa);

		validaCobrancaEventoPedido(pedido);
		mesaService.reservarMesa(mesa.getId());

		return pedidoRepository.save(pedido);
	}

	public void validaCobrancaEventoPedido(Pedido pedido) {

		if (pedido.getTipo().equals(TipoPedido.LOCAL)) {
			Evento evento = eventoService.validaEventoAberto();
			if (evento != null) {
				pedido.getItens().add(new ItemPedido(pedido, evento, 1));
			}
		}
	}

	@Transactional
	public Pedido adicionarItemPedido(Long idPedido, List<ItemPedido> itens) {
		Pedido pedido = buscarPedidoPorId(idPedido);

		itens.forEach(i -> {
			i.setPedido(pedido);
			i.setStatus(StatusItem.PENDENTE);
		});
		pedido.getItens().addAll(itens);
		return pedidoRepository.save(pedido);
	}

	@Transactional(readOnly = true)
	public String gerarNumeroPedido() {
		Pedido pedidoMaiorNumero = pedidoRepository.findTopByOrderByCodigoDesc();
		int novoNumero = pedidoMaiorNumero != null ? Integer.parseInt(pedidoMaiorNumero.getCodigo()) + 1 : 1;

		return String.valueOf(novoNumero);
	}

	@Transactional
	public Pedido editarPedido(Long id, PedidoForm form) throws AccessDeniedException {
		Pedido pedido = buscarPedidoPorId(id);

		Mesa mesa = mesaService.buscarMesaPorNumero(form.getNumeroMesa());
		FormaDePagamento formaDePagamento = formaDePagamentoService.buscarPorTipoPagamento(form.getTipoPagamento());

		mesaService.reservarMesa(mesa.getId());

		pedido.setMesa(mesa);
		pedido.setFormaDePagamento(formaDePagamento);
		pedido.setPercentualDesconto(form.getPercentualDesconto());
		pedido.setTipo(form.getTipo());

		return pedidoRepository.save(pedido);
	}

	@Transactional(readOnly = true)
	public Pedido buscarPedidoPorId(Long id) {
		return pedidoRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado para ID " + id));
	}

	public Page<Pedido> listarTodosPedidos(Pageable pageable) {
		return pedidoRepository.findAll(pageable);
	}

	@Transactional
	public void alterarStatusPedido(Long id, StatusPedido status) {
		Pedido pedido = buscarPedidoPorId(id);
		pedido.setStatus(status);
		pedidoRepository.save(pedido);
	}

	public BigDecimal calcularValorItens(Pedido pedido) {
		return pedido.getItens().stream().map(i -> {
			BigDecimal valorUnitario = i.getProduto() != null ? i.getProduto().getValor()
					: i.getEvento() != null ? i.getEvento().getValor() : BigDecimal.ZERO;
			return valorUnitario.multiply(BigDecimal.valueOf(i.getQuantidade()));
		}).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public BigDecimal aplicarDesconto(BigDecimal valorTotal, BigDecimal porcentagem) {
		BigDecimal valorDesconto = valorTotal.multiply(porcentagem).divide(new BigDecimal("100"), 2,
				RoundingMode.HALF_UP);

		return valorTotal.subtract(valorDesconto);
	}

	public void validaItensEmPreparo(Pedido pedido) {
		List<String> itensEmPreparo = pedido.getItens().stream()
				.filter(i -> i.getStatus().equals(StatusItem.EM_PREPARO)).map(i -> i.getProduto().getDescricao())
				.toList();

		if (!itensEmPreparo.isEmpty()) {
			throw new DataIntegrityViolationException(
					"Os seguintes itens se encontram em preparo, favor finalizar para seguir: "
							+ String.join(", ", itensEmPreparo));
		}
	}

	@Transactional
	public void fecharPedido(Long id, TipoPagamento tipoPagamento) {

		Pedido pedido = buscarPedidoPorId(id);
		pedido.setStatus(StatusPedido.FINALIZADO);
		validaItensEmPreparo(pedido);

		BigDecimal valorItens = calcularValorItens(pedido);
		BigDecimal valorTotal = aplicarDesconto(valorItens, pedido.getPercentualDesconto());

		pedido.setFormaDePagamento(new FormaDePagamento(tipoPagamento, valorTotal));

		mesaService.liberarMesa(pedido.getMesa().getId());

		pedidoRepository.save(pedido);
	}
}
