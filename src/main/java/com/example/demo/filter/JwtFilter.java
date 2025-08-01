package com.example.demo.filter;

import com.example.demo.dto.ApiResponse;
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

        // —— 白名单：登录、注册等公开接口放行 ——
        if ("/auth/login".equals(path)
                || "/auth/register".equals(path)
                || path.startsWith("/debug")  // 测试用，生产环境请删除
                || path.startsWith("/static/")
                || path.matches(".*\\.(css|js|png|jpg|jpeg|gif|ico)$")) {
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
                // 把用户名放到 request 属性里，后续 controller 可以获取
                request.setAttribute("username", claims.getSubject());
                chain.doFilter(req, res);
                return;
            } catch (JwtException e) {
                // Token 无效或过期，返回统一格式的JSON响应
                sendJsonResponse(response, "无效或过期的Token");
                return;
            }
        } else {
            // 没带 Token，返回统一格式的JSON响应
            sendJsonResponse(response, "请先登录");
            return;
        }
    }

    /**
     * 发送统一格式的JSON错误响应
     */
    private void sendJsonResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK); // 统一返回200状态码
        response.setContentType("application/json;charset=UTF-8");

        ApiResponse<Void> apiResponse = ApiResponse.fail(message);
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);

        response.getWriter().write(jsonResponse);
    }
}