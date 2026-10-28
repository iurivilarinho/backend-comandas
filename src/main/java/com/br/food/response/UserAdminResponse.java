package com.br.food.response;

import java.time.LocalDateTime;

import com.br.food.enums.Types.StaffRole;
import com.br.food.models.User;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Usuário da equipe na tela de usuários")
public record UserAdminResponse(
		@Schema(description = "Identificador do usuário") Long id,
		@Schema(description = "Nome") String name,
		@Schema(description = "E-mail") String email,
		@Schema(description = "Papel; vazio quando o usuário não tem nenhum papel da equipe") StaffRole role,
		@Schema(description = "Verdadeiro quando o acesso está liberado") boolean active,
		@Schema(description = "Data de criação") LocalDateTime createdAt,
		@Schema(description = "Verdadeiro para o próprio usuário logado") boolean self) {

	public static UserAdminResponse from(User user, Long loggedUserId) {
		StaffRole role = user.getRoles().stream().map(r -> StaffRole.fromAuthority(r.getName())).flatMap(java.util.Optional::stream)
				.min(java.util.Comparator.comparingInt(Enum::ordinal)).orElse(null);
		return new UserAdminResponse(user.getId(), user.getName(), user.getEmail(), role,
				!Boolean.FALSE.equals(user.getActive()), user.getCreatedAt(), user.getId().equals(loggedUserId));
	}
}
