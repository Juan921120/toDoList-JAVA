// ====== JwtUtil.java ======
@Component
public class JwtUtil {
    // ← 建议把这串密钥放到 application.properties 或者环境变量里
    @Value("${jwt.secret}")  // 从配置文件读取
    private  String SECRET;                  
    private final long EXP = 1000L * 60 * 60;                     // ← 有效期，毫秒，1h = 3600000，可改

    /** 生成 Token，载荷里只放了 username，如需加别的字段（角色、ID），可在 claims 里添加 */
    public String generateToken(String username) {
        return Jwts.builder()
            .setSubject(username)                                // ← 设置用户唯一标识
            .setIssuedAt(new Date())                             // ← 签发时间，通常不改
            .setExpiration(new Date(System.currentTimeMillis() + EXP)) // ← 到期时间
            .signWith(SignatureAlgorithm.HS512, SECRET)          // ← 算法 & 密钥
            .compact();
    }

    /** 解析并验证 Token，抛出异常则说明无效或过期 */
    public Claims parseToken(String token) {
        return Jwts.parser()
            .setSigningKey(SECRET)                               // ← 同上，使用相同密钥解析
            .parseClaimsJws(token)
            .getBody();
    }
}
