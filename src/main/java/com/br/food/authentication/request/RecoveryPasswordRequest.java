package com.br.food.authentication.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "RecoveryPasswordRequest", description = "Request payload used to confirm a password recovery code and set a new password.")
public class RecoveryPasswordRequest {

	@NotNull(message = "O campo 'Senha' nÃ£o pode ser nulo.")
	@Size(min = 8, message = "A senha deve ter no mÃ­nimo 8 caracteres.")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#\\-])[A-Za-z\\d@$!%*?&#\\-]{8,}$", message = "A senha deve conter pelo menos uma letra maiÃºscula, uma letra minÃºscula, um nÃºmero e um caractere especial.")
	@Schema(description = "New password to be set. Must have at least 8 characters, uppercase, lowercase, number, and special character.", example = "MyNewPass@123", accessMode = Schema.AccessMode.WRITE_ONLY)
	private String newPassword;

	@NotBlank(message = "O cÃ³digo nÃ£o pode estar em branco")
	@Schema(description = "Password recovery code.", example = "1234")
	private String code;

	@NotBlank(message = "O email nÃ£o pode estar em branco")
	@Schema(description = "User email address.", example = "user@company.com")
	private String email;

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
