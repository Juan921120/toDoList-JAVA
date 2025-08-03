package com.example.demo.filter;

import com.example.demo.dto.ErrorResponse;
import com.example.demo.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtFilter implements Filter {

    @Autowired
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI();

        // —— 白名单：只放行登录注册和静态资源 ——
        if ("/auth/login".equals(path)
                || "/auth/register".equals(path)
                || path.startsWith("/static/")
                || path.matches(".*\\.(css|js|png|jpg|jpeg|gif|ico|html|woff|woff2|ttf|svg|map)$")) {
            chain.doFilter(req, res);
            return;
        }

        // —— 验证 JWT ——
        String auth = request.getHeader("Authorization");
        String prefix = "Bearer ";

        if (auth != null && auth.startsWith(prefix)) {
            String token = auth.substring(prefix.length());
            try {
                Claims claims = jwtUtil.parseToken(token);
                request.setAttribute("username", claims.getSubject());
                chain.doFilter(req, res);
                return;
            } catch (JwtException e) {
                sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_TOKEN", "无效或过期的Token");
                return;
            }
        } else {
            sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "MISSING_TOKEN", "请先登录");
            return;
        }
    }

    private void sendJsonResponse(HttpServletResponse response, int statusCode, String error, String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");

        ErrorResponse errorResponse = ErrorResponse.of(error, message);
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);

        response.getWriter().write(jsonResponse);
    }
}