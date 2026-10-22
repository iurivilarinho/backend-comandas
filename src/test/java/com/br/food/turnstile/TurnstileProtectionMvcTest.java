package com.br.food.turnstile;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/** The filter in front of a protected route: off by default, refuses without a token, passes with one. */
@ExtendWith(MockitoExtension.class)
class TurnstileProtectionMvcTest {

	@RestController
	static class ProtectedController {
		@PostMapping("/auth/login")
		String protectedRoute() {
			return "ok";
		}
	}

	@Mock
	private TurnstileVerifier verifier;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		TurnstileProperties properties = new TurnstileProperties("site", "secret", "https://example.invalid",
				Duration.ofSeconds(3), List.of("/auth/login"));
		TurnstileFilter filter = new TurnstileFilter(verifier, properties,
				List.of(new ProblemJsonTurnstileFailureHandler()));
		mockMvc = MockMvcBuilders.standaloneSetup(new ProtectedController()).addFilters(filter).build();
	}

	@Test
	void withTheCheckOffTheRouteBehavesAsBefore() throws Exception {
		when(verifier.isEnabled()).thenReturn(false);

		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isOk());
	}

	@Test
	void withTheCheckOnARequestWithoutTokenIsRefused() throws Exception {
		when(verifier.isEnabled()).thenReturn(true);
		when(verifier.verify(null, "127.0.0.1")).thenReturn(TurnstileVerdict.MISSING);

		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("human-verification-required"));
	}

	@Test
	void withTheCheckOnARequestWithAValidTokenPasses() throws Exception {
		when(verifier.isEnabled()).thenReturn(true);
		when(verifier.verify("tok", "127.0.0.1")).thenReturn(TurnstileVerdict.PASSED);

		mockMvc.perform(post("/auth/login").header(TurnstileFilter.TOKEN_HEADER, "tok")
				.contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isOk());
	}
}
