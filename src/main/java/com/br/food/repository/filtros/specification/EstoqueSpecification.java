package com.br.food.repository.filtros.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.br.food.models.Estoque;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.Attribute.PersistentAttributeType;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.Type.PersistenceType;

public class EstoqueSpecification {

	public static Specification<Estoque> buscarEstoquePorCodigoProduto(String codigoProduto) {
		return (root, query, builder) -> builder.equal(root.get("produto").get("codigo"), codigoProduto);
	}

	public static Specification<Estoque> searchAllFields(String searchTerm) {
		return (root, query, criteriaBuilder) -> {
			if (searchTerm == null || searchTerm.trim().isEmpty()) {
				return criteriaBuilder.conjunction(); // Retorna uma condição "true" se o termo de busca for nulo ou
														// vazio
			}

			List<Predicate> predicates = new ArrayList<>();
			String lowerSearchTerm = searchTerm.toLowerCase();

			// Busca nos campos da entidade principal (Estoque)
			for (Attribute<? super Estoque, ?> attribute : root.getModel().getAttributes()) {
				if (attribute.getJavaType().equals(String.class)) {
					predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(attribute.getName())),
							"%" + lowerSearchTerm + "%"));
				}
			}

			// Busca nos campos das entidades relacionadas (relacionamentos @OneToMany e
			// @ManyToMany)
			for (PluralAttribute<? super Estoque, ?, ?> pluralAttribute : root.getModel().getPluralAttributes()) {
				if (pluralAttribute.getElementType().getPersistenceType() == PersistenceType.ENTITY) {
					Join<Estoque, ?> join = root.join(pluralAttribute.getName(), JoinType.LEFT);
					if (join.getModel() instanceof ManagedType<?>) { // Verifica se é uma entidade
						ManagedType<?> managedType = (ManagedType<?>) join.getModel();
						for (Attribute<?, ?> relatedAttribute : managedType.getAttributes()) {
							if (relatedAttribute.getJavaType().equals(String.class)) {
								predicates.add(criteriaBuilder.like(
										criteriaBuilder.lower(join.get(relatedAttribute.getName())),
										"%" + lowerSearchTerm + "%"));
							}
						}
					}
				}
			}

			// Busca nos campos das entidades relacionadas (relacionamentos @OneToOne e
			// @ManyToOne)
			for (SingularAttribute<? super Estoque, ?> singularAttribute : root.getModel().getSingularAttributes()) {
				if (singularAttribute.getPersistentAttributeType() == PersistentAttributeType.MANY_TO_ONE
						|| singularAttribute.getPersistentAttributeType() == PersistentAttributeType.ONE_TO_ONE) {
					Join<Estoque, ?> join = root.join(singularAttribute.getName(), JoinType.LEFT);
					if (join.getModel() instanceof ManagedType<?>) { // Verifica se é uma entidade
						ManagedType<?> managedType = (ManagedType<?>) join.getModel();
						for (Attribute<?, ?> relatedAttribute : managedType.getAttributes()) {
							if (relatedAttribute.getJavaType().equals(String.class)) {
								predicates.add(criteriaBuilder.like(
										criteriaBuilder.lower(join.get(relatedAttribute.getName())),
										"%" + lowerSearchTerm + "%"));
							}
						}
					}
				}
			}

			return predicates.isEmpty() ? criteriaBuilder.conjunction()
					: criteriaBuilder.or(predicates.toArray(new Predicate[0]));
		};
	}
}
