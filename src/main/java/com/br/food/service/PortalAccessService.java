package com.br.food.service;

import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.br.food.models.User;

import jakarta.persistence.EntityNotFoundException;

/**
 * Decides who may touch the data of a customer on the routes the digital menu calls without a staff login
 * (CMD-015): the logged-in staff, or the customer who holds the token of that very customer.
 */
@Service
public class PortalAccessService {

	/** Header that carries the customer token. */
	public static final String CUSTOMER_TOKEN_HEADER = "X-Customer-Token";

	private final CustomerAccessTokenService customerAccessTokenService;

	public PortalAccessService(CustomerAccessTokenService customerAccessTokenService) {
		this.customerAccessTokenService = customerAccessTokenService;
	}

	public boolean isStaff() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication != null && authentication.getPrincipal() instanceof User;
	}

	public void requireStaff() {
		if (!isStaff()) {
			throw new AccessDeniedException("Acesso restrito a equipe do estabelecimento.");
		}
	}

	/**
	 * Staff passes; anyone else needs the token of the customer {@code customerId}. A {@code null} owner (an order
	 * without a customer) is staff only.
	 */
	public void requireStaffOrCustomer(String customerToken, Long customerId) {
		if (isStaff()) {
			return;
		}
		Long tokenCustomerId = customerAccessTokenService.resolve(customerToken);
		if (customerId == null || !customerId.equals(tokenCustomerId)) {
			throw new AccessDeniedException("Esse cadastro pertence a outro cliente.");
		}
	}

	/**
	 * Loads something that belongs to a customer (an order) for the staff, or for the customer who owns it. The token
	 * is checked before anything is loaded and a missing record answers like a foreign one, so nobody can probe which
	 * ids exist.
	 */
	public <T> T loadOwned(String customerToken, Supplier<T> loader, Function<T, Long> ownerOf) {
		if (isStaff()) {
			return loader.get();
		}
		Long tokenCustomerId = customerAccessTokenService.resolve(customerToken);
		try {
			T entity = loader.get();
			if (!tokenCustomerId.equals(ownerOf.apply(entity))) {
				throw new AccessDeniedException("Esse cadastro pertence a outro cliente.");
			}
			return entity;
		} catch (EntityNotFoundException notFound) {
			throw new AccessDeniedException("Esse cadastro pertence a outro cliente.");
		}
	}

	/**
	 * The customer filter to apply on a listing: staff may ask for any (or none), a customer only for the own
	 * registration, which is also what is applied when none is asked.
	 */
	public Long resolveCustomerFilter(String customerToken, Long requestedCustomerId) {
		if (isStaff()) {
			return requestedCustomerId;
		}
		Long tokenCustomerId = customerAccessTokenService.resolve(customerToken);
		if (requestedCustomerId != null && !requestedCustomerId.equals(tokenCustomerId)) {
			throw new AccessDeniedException("Esse cadastro pertence a outro cliente.");
		}
		return tokenCustomerId;
	}
}
