package com.br.food.authentication.token;


import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.authentication.models.RefreshToken;
import com.br.food.authentication.repository.RefreshTokenRepository;
import com.br.food.models.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Schema(name = "RefreshTokenService", description = "Regras de negÃ³cio de emissÃ£o, rotaÃ§Ã£o, revogaÃ§Ã£o e cookies do refresh token")
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${api.refresh.expiration.days:15}")
    private int refreshExpirationDays;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /** Emite e persiste um refresh token para o usuÃ¡rio; retorna o valor em claro (para cookie) */
    @Transactional
    public String issue(User user, String ip, String userAgent) {
        String raw = TokenUtil.generateOpaqueToken();
        String hash = TokenUtil.sha256(raw);

        RefreshToken rt = new RefreshToken();
        rt.setUsuario(user);
        rt.setTokenHash(hash);
        rt.setIp(ip);
        rt.setUserAgent(userAgent);
        rt.setExpiresAt(LocalDateTime.now().plusDays(refreshExpirationDays));
        refreshTokenRepository.save(rt);

        return raw;
    }

    /** Rotaciona: revoga o atual, cria novo e referencia via replacedById */
    @Transactional
    public String rotate(String rawToken, String ip, String userAgent) {
        RefreshToken current = mustFindActive(rawToken);

        current.setRevoked(true);
        refreshTokenRepository.save(current);

        String newRaw = issue(current.getUsuario(), ip, userAgent);

        Optional<RefreshToken> newSaved = refreshTokenRepository.findByTokenHash(TokenUtil.sha256(newRaw));
        newSaved.ifPresent(n -> {
            current.setReplacedById(n.getId());
            refreshTokenRepository.save(current);
        });

        return newRaw;
    }

    /** Revoga o refresh atual (logout do dispositivo atual) */
    @Transactional
    public void revoke(String rawToken) {
        RefreshToken rt = mustFindActive(rawToken);
        rt.setRevoked(true);
        refreshTokenRepository.save(rt);
    }

    /** Revoga todos os refresh tokens de um usuÃ¡rio (logout de todos os dispositivos) */
    @Transactional
    public void revokeAllForUser(User user) {
        refreshTokenRepository.findAll().stream().filter(t -> t.getUsuario().getId().equals(user.getId()) && !t.isRevoked())
                .forEach(t -> {
                    t.setRevoked(true);
                    refreshTokenRepository.save(t);
                });
    }

    /** ObtÃ©m o registro ativo (nÃ£o revogado e nÃ£o expirado) a partir do valor em claro do cookie */
    public RefreshToken mustFindActive(String rawToken) {
        String hash = TokenUtil.sha256(rawToken);
        RefreshToken rt = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new AccessDeniedException("refresh token invÃ¡lido"));

        if (rt.isRevoked() || rt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AccessDeniedException("refresh token expirado ou revogado");
        }
        return rt;
    }

    // Helpers de cookie (usar em controllers)
    public void writeRefreshCookie(HttpServletResponse resp, String rawToken, boolean secure) {
        Cookie cookie = new Cookie("refresh_token", rawToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * refreshExpirationDays);
        cookie.setAttribute("SameSite", secure ? "None" : "Lax");
        resp.addCookie(cookie);
    }

    public void clearRefreshCookie(HttpServletResponse resp, boolean secure) {
        Cookie cookie = new Cookie("refresh_token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", secure ? "None" : "Lax");
        resp.addCookie(cookie);
    }
}

