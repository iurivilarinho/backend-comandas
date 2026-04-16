package com.br.food.authentication;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.br.food.models.User;
import com.br.food.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class AutenticationService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(username)
                .orElseThrow(() -> new EntityNotFoundException("UsuÃ¡rio nÃ£o encontrado para o login: " + username));
        if (handleValidCredentialUser(user)) {
            throw new AccessDeniedException(
                    "Senha expirada. Por favor, altere sua senha na opção 'Esqueci minha senha'.");
        }

        return user;

    }

    private boolean handleValidCredentialUser(User user) {
        if (user.isForcePasswordChange() || (user.getPasswordLastChanged() != null
                ? user.getPasswordLastChanged().isBefore(LocalDateTime.now().minusDays(60))
                : false)) {
            return true;
        } else {
            return false;
        }
    }

}

