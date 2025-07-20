package com.example.demo.filter;

import com.example.demo.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 去掉@WebFilter，使用@Component让Spring管理
@Component
public class JwtFilter implements Filter {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * ServletRequest req：HTTP请求对象
     * ServletResponse res：HTTP响应对象
     * FilterChain chain：过滤器链
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        //将通用的ServletRequest转换为HttpServletRequest，以便使用HTTP特有的方法
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // 取出本次请求的 URI
        String path = request.getRequestURI();

        // —— 白名单：登录、注册等公开接口放行 ——
        if ("/auth/login".equals(path)
                || "/auth/register".equals(path)
                // 放过 /static/ 目录下所有资源（Spring Boot 默认把 resources/static 下的文件映射到 /static/**）
                || path.startsWith("/static/")
                // 或者根据后缀放行
                || path.matches(".*\\.(css|js|png|jpg|jpeg|gif|ico)$")) {
            chain.doFilter(req, res);
            return;
        }

        // —— 验证 JWT ——
        String auth = request.getHeader("Authorization");
        //Bearer 是官方默认的“通行证”标签；JWT 就藏在 Authorization 请求头里的 Bearer 后面
        String prefix = "Bearer ";

        if (auth != null && auth.startsWith(prefix)) { //检查Authorization头是否存在且以"Bearer "开头
            String token = auth.substring(prefix.length()); //提取Token部分（去掉"Bearer "前缀
            try {
                Claims claims = jwtUtil.parseToken(token); //使用JwtUtil解析Token
                // 把用户名放到 request 属性里，后续 controller 可以获取
                request.setAttribute("username", claims.getSubject());
            } catch (JwtException e) {
                // Token 无效或过期，直接返回 401
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "无效或过期的 Token");
                return;
            }
        } else {
            // 没带 Token，返回 401
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "请先登录");
            return;
        }

        // 放行到后续 Filter 或 Controller
        chain.doFilter(req, res);
    }
}