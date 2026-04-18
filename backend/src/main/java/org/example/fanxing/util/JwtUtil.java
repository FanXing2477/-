package org.example.fanxing.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {

    private static final String KEY = "FanXing";

    // 接受业务逻辑，生成token并返回
    public static String getToken(Map<String, Object> personId) {
        return JWT.create()
                .withClaim("claims",personId)
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000*60*60*12))
                .sign(Algorithm.HMAC256(KEY));
    }
    //接受token，验证token，并返回业务数据
    public static Map<String, Object> parseToken(String token){
        return JWT.require(Algorithm.HMAC256(KEY))
                .build()
                .verify(token)
                .getClaim("claims")
                .asMap();
    }
    //重新生成token
    public static String regenerateToken(String personId){
        Map<String, Object> claims = new HashMap<>();
        claims.put("claims",personId);

        return getToken(claims);

    }
}
