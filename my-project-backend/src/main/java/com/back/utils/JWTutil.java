package com.back.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.annotation.Resource;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JWTutil {
    @Value("${spring.security.jwt.key}")
    String key;

    @Value("${spring.security.jwt.expire}")
    int expire=2;

    @Resource
    StringRedisTemplate template;

    public boolean invalidateJwt(String headerToken) {
        String token = this.converToken(headerToken);
        if(token==null)return false;
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier verifier = JWT.require(algorithm).build();
        try{
            DecodedJWT jwt = verifier.verify(token);
            String id = jwt.getId();
            return  deleteToken(id,jwt.getExpiresAt());//看是否拉黑成功
        }catch (JWTVerificationException e){
            return false;
        }
    }

    public DecodedJWT resolveJwt(String headertoken) {
        String token = this.converToken(headertoken);

        if(token==null)return null;
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier verifier = JWT.require(algorithm).build();

        try{
            DecodedJWT jwt = verifier.verify(token);//检查是否被篡改
            if(this.isInvalidToken(jwt.getId()))
                return null;//是否存在黑名单
            //检查令牌是否过期
            Date exp = jwt.getExpiresAt();
            return new Date().after(exp) ? null: jwt;
        }catch (JWTVerificationException e){
            e.printStackTrace();
            return null;
        }
    }

    private boolean deleteToken(String uuid,Date time) {
        if(this.isInvalidToken(uuid))return false;
        Date now = new Date();//函数用来拉黑
        long expire = Math.max(time.getTime() - now.getTime(),0);
        template.opsForValue().set(Const.JWT_BLACK_LIST+uuid,"",expire, TimeUnit.MILLISECONDS);
        return true;
    }
    private boolean isInvalidToken(String uudi) {
       return Boolean.TRUE.equals(template.hasKey(Const.JWT_BLACK_LIST + uudi));//查看数据库是否存在黑名单
    }

    public String createJWT(UserDetails userDetails,int id,String username) {
        Algorithm algorithm = Algorithm.HMAC256(key);
        Date expire = this.expireTime();
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("id",id)
                .withClaim("username",username)
                .withClaim("authorities", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .withExpiresAt(expireTime())
                .withIssuedAt(new Date())
                .sign(algorithm);

        
    }
    public Date expireTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, expire*24);
        return calendar.getTime();
    }
    private String converToken(String token) {

        if (token == null || !token.startsWith("Bearer ")) {

            return null;
        }
        return token.substring(7);//返回正确
    }
    public UserDetails toUser(DecodedJWT jwt) {

        Map<String, Claim> claims= jwt.getClaims();
        System.out.println(claims);
        return User
                .withUsername(claims.get("username").asString())
                .password("cwdcw")
                .authorities(claims.get("authorities").asArray(String.class))
                .build();

    }
    public Integer toId(DecodedJWT jwt) {
        Map<String, Claim> claims= jwt.getClaims();
        return claims.get("id").asInt();

    }
}
