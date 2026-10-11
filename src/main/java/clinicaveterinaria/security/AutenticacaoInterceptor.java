package clinicaveterinaria.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** Bloqueia (HTTP 401) qualquer chamada a /api/** sem usuário logado, exceto /api/auth/**. */
@Component
public class AutenticacaoInterceptor implements HandlerInterceptor {

    private final SessaoUsuario sessaoUsuario;

    public AutenticacaoInterceptor(SessaoUsuario sessaoUsuario) {
        this.sessaoUsuario = sessaoUsuario;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        sessaoUsuario.atual(request.getSession(false));
        return true;
    }
}
