package com.br.food.authentication.models;

import java.time.LocalDateTime;

import com.br.food.models.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Schema(name = "RefreshToken", description = "Token de renovaÃ§Ã£o (opaco) persistido no servidor, com rotaÃ§Ã£o e revogaÃ§Ã£o")
@Entity
@Table(name = "refresh_tokens", indexes = {@Index(name = "IDX_RT_TOKENHASH", columnList = "tokenHash", unique = true), @Index(name = "IDX_RT_USER", columnList = "fk_Id_User")})
public class RefreshToken {

    @Schema(description = "Identificador interno do refresh token", example = "42")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "UsuÃ¡rio dono do refresh token")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_Id_User", nullable = false, foreignKey = @ForeignKey(name = "FK_FROM_TBUSER_FOR_TBREFRESHTOKEN"))
    private User user;

    @Schema(description = "Hash do refresh token (valor em claro nunca Ã© persistido)", example = "yZ0iJ6... (Base64URL)")
    @Column(nullable = false, unique = true, length = 120)
    private String tokenHash;

    @Schema(description = "Data/hora de expiraÃ§Ã£o do refresh token", example = "2025-10-10T23:59:59")
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Schema(description = "Flag de revogaÃ§Ã£o (true = invÃ¡lido)", example = "false")
    @Column(nullable = false)
    private boolean revoked = false;

    @Schema(description = "LigaÃ§Ã£o com o novo token apÃ³s rotaÃ§Ã£o (opcional)", example = "84")
    private Long replacedById;

    @Schema(description = "User-Agent de quem recebeu o token (auditoria)", example = "Mozilla/5.0 ...")
    private String userAgent;

    @Schema(description = "IP de quem recebeu o token (auditoria)", example = "200.147.35.1")
    private String ip;

    public RefreshToken() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUsuario() {
        return user;
    }

    public void setUsuario(User user) {
        this.user = user;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public Long getReplacedById() {
        return replacedById;
    }

    public void setReplacedById(Long replacedById) {
        this.replacedById = replacedById;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}

