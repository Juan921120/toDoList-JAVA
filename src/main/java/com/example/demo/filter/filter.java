// ====== JwtFilter.java ======
@WebFilter(urlPatterns = "/api/*")  // ← 根据项目路由修改拦截范围
public class JwtFilter implements Filter {
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // 取出本次请求的 URI
        String path = request.getRequestURI();  // ← 这里一定要先拿到 path

        // —— 白名单：登录、注册等公开接口放行 —— 
        // 如果你有多个公共前缀，也可以用 path.startsWith("/api/auth/")
        if ("/api/login".equals(path) || "/api/register".equals(path)) {
            chain.doFilter(req, res);
            return;
        }

        // —— 验证 JWT —— 
        String auth   = request.getHeader("Authorization");     // ← Header 名称
        String prefix = "Bearer ";                              // ← 前缀

        if (auth != null && auth.startsWith(prefix)) {
            String token = auth.substring(prefix.length());
            try {
                Claims claims = jwtUtil.parseToken(token);
                // 把用户名放到 request 属性里，后续 controller 拿 request.getAttribute("username")
                request.setAttribute("username", claims.getSubject());
            } catch (JwtException e) {
                // Token 无效或过期，直接返回 401
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "无效或过期的 Token");
                return;
            }
        } else {
            // 没带 Token，也可以直接 401，或者按业务允许匿名访问
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "请先登录");
            return;
        }

        // 放行到后续 Filter 或 Controller
        chain.doFilter(req, res);
    }
}
