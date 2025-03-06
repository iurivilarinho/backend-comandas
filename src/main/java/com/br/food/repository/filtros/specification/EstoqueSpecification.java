package com.br.food.repository.filtros.specification;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.domain.Specification;

import com.br.food.models.Estoque;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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
				return criteriaBuilder.conjunction();
			}

			String lowerSearchTerm = searchTerm.toLowerCase();
			List<Predicate> predicates = new ArrayList<>();
			Set<String> visitedEntities = new HashSet<>();

			System.out.println("Iniciando busca com searchTerm: " + lowerSearchTerm);
			collectPredicates(root, criteriaBuilder, lowerSearchTerm, predicates, visitedEntities, "");

			System.out.println("Predicados encontrados: " + predicates.size());
			return predicates.isEmpty() ? criteriaBuilder.conjunction()
					: criteriaBuilder.or(predicates.toArray(new Predicate[0]));
		};
	}

	private static void collectPredicates(Path<?> path, CriteriaBuilder criteriaBuilder, String searchTerm,
			List<Predicate> predicates, Set<String> visitedEntities, String pathPrefix) {

		ManagedType<?> managedType;
		if (path instanceof Root<?>) {
			managedType = (ManagedType<?>) ((Root<?>) path).getModel();
		} else if (path instanceof Join<?, ?>) {
			managedType = (ManagedType<?>) ((Join<?, ?>) path).getModel();
		} else {
			System.out.println("Path " + pathPrefix + " não é Root nem Join, ignorando: " + path.getClass());
			return;
		}

		String currentPath = pathPrefix + managedType.getJavaType().getName();
		if (visitedEntities.contains(currentPath)) {
			System.out.println("Ciclo detectado em: " + currentPath);
			return;
		}
		visitedEntities.add(currentPath);

		System.out.println("Explorando entidade: " + currentPath);

		for (Attribute<?, ?> attribute : managedType.getAttributes()) {
			try {
				System.out.println(
						"Analisando atributo: " + attribute.getName() + " (tipo: " + attribute.getJavaType() + ")");

				if (attribute.getJavaType() != null && attribute.getJavaType().equals(String.class)
						&& attribute instanceof SingularAttribute<?, ?>) {
					Predicate predicate = criteriaBuilder.like(criteriaBuilder.lower(path.get(attribute.getName())),
							"%" + searchTerm + "%");
					predicates.add(predicate);
					System.out.println("Adicionado predicado para " + attribute.getName() + ": " + predicate);
				}

				if (attribute instanceof SingularAttribute<?, ?> singularAttribute) {
					if (singularAttribute.getPersistentAttributeType() == PersistentAttributeType.MANY_TO_ONE
							|| singularAttribute.getPersistentAttributeType() == PersistentAttributeType.ONE_TO_ONE) {
						Join<?, ?> join = ((From<?, ?>) path).join(attribute.getName(), JoinType.LEFT);
						System.out.println("Criando JOIN para " + attribute.getName());
						collectPredicates(join, criteriaBuilder, searchTerm, predicates, visitedEntities,
								currentPath + ".");
					}
				} else if (attribute instanceof PluralAttribute<?, ?, ?> pluralAttribute) {
					if (pluralAttribute.getElementType().getPersistenceType() == PersistenceType.ENTITY) {
						Join<?, ?> join = ((From<?, ?>) path).join(attribute.getName(), JoinType.LEFT);
						System.out.println("Criando JOIN para " + attribute.getName());
						collectPredicates(join, criteriaBuilder, searchTerm, predicates, visitedEntities,
								currentPath + ".");
					}
				}
			} catch (IllegalArgumentException e) {
				System.out.println("Erro ao processar atributo " + attribute.getName() + ": " + e.getMessage());
				continue;
			}
		}
	}
}
