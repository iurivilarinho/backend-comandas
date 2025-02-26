package com.br.food.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.br.food.enums.Status.StatusPedido;
import com.br.food.enums.Tipos.TipoPedido;
import com.br.food.forms.PedidoForm;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbPedido")
public class Pedido {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_Id_Cliente", foreignKey = @ForeignKey(name = "FK_FROM_TBCLIENTE_FOR_TBPEDIDO"))
	private Cliente cliente;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_Id_Mesa", foreignKey = @ForeignKey(name = "FK_FROM_TBMESA_FOR_TBPEDIDO"))
	private Mesa mesa;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_Id_FormaDePagamento", foreignKey = @ForeignKey(name = "FK_FROM_TBFORMADEPAGAMENTO_FOR_TBPEDIDO"))
	private FormaDePagamento formaDePagamento;

	@OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<ItemPedido> itens = new ArrayList<>();

	@Column(length = 10)
	private String codigo;

	@Enumerated(EnumType.STRING)
	private StatusPedido status;

	@Column(length = 3)
	private BigDecimal percentualDesconto;

	@Column(nullable = false)
	private BigDecimal valorTotal;

	@Column(nullable = false)
	private LocalDateTime horaAbertura = LocalDateTime.now();

	private LocalDateTime horaFim;

	@Enumerated(EnumType.STRING)
	private TipoPedido tipo;

	public Pedido(PedidoForm form, Cliente cliente, String codigo, Mesa mesa) {

		this.cliente = cliente;
		this.codigo = codigo;
		this.mesa = mesa;
		this.valorTotal = BigDecimal.ZERO;
		this.status = StatusPedido.AGUARDANDO_APROVACAO;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public StatusPedido getStatus() {
		return status;
	}

	public void setStatus(StatusPedido status) {
		this.status = status;
	}

	public BigDecimal getPercentualDesconto() {
		return percentualDesconto;
	}

	public void setPercentualDesconto(BigDecimal percentualDesconto) {
		this.percentualDesconto = percentualDesconto;
	}

	public BigDecimal getValorTotal() {
		return valorTotal;
	}

	public void setValorTotal(BigDecimal valorTotal) {
		this.valorTotal = valorTotal;
	}

	public LocalDateTime getHoraAbertura() {
		return horaAbertura;
	}

	public void setHoraAbertura(LocalDateTime horaAbertura) {
		this.horaAbertura = horaAbertura;
	}

	public LocalDateTime getHoraFim() {
		return horaFim;
	}

	public void setHoraFim(LocalDateTime horaFim) {
		this.horaFim = horaFim;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Mesa getMesa() {
		return mesa;
	}

	public void setMesa(Mesa mesa) {
		this.mesa = mesa;
	}

	public FormaDePagamento getFormaDePagamento() {
		return formaDePagamento;
	}

	public void setFormaDePagamento(FormaDePagamento formaDePagamento) {
		this.formaDePagamento = formaDePagamento;
	}

	public List<ItemPedido> getItens() {
		return itens;
	}

	public void setItens(List<ItemPedido> itens) {
		this.itens = itens;
	}

	public TipoPedido getTipo() {
		return tipo;
	}

	public void setTipo(TipoPedido tipo) {
		this.tipo = tipo;
	}

	public Long getId() {
		return id;
	}

}