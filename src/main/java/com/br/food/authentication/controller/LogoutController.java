package com.br.food.authentication.controller;

import com.br.food.authentication.token.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth")
@RestController
@RequestMapping("/auth")
public class LogoutController {

    private final RefreshTokenService refreshService;

    public LogoutController(RefreshTokenService refreshService) {
        this.refreshService = refreshService;
    }

    @Operation(summary = "Logout", description = "Revoga o refresh token atual e apaga cookies de autenticaÃ§Ã£o.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        boolean secure = isSecureRequest(request);
        String refreshRaw = readCookie(request, "refresh_token");
        if (refreshRaw != null) {
            try {
                refreshService.revoke(refreshRaw);
            } catch (Exception ignored) {
            }
        }
        // limpa refresh
        refreshService.clearRefreshCookie(response, secure);

        // limpa access
        Cookie access = new Cookie("token", "");
        access.setHttpOnly(true);
        access.setSecure(secure);
        access.setPath("/");
        access.setMaxAge(0);
        access.setAttribute("SameSite", secure ? "None" : "Lax");
        response.addCookie(access);

        return ResponseEntity.noContent().build();
    }

    private boolean isSecureRequest(HttpServletRequest request) {
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        return request.isSecure() || (forwardedProto != null && forwardedProto.equalsIgnoreCase("https"));
    }

    private String readCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie c : request.getCookies()) if (name.equals(c.getName())) return c.getValue();
        return null;
    }
}

