package com.back.filter;

import com.back.entity.RestBean;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.back.utils.Const;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Order(Const.ORDER_LIMIT)
public class FlowLimitFilter extends HttpFilter {

    @Resource
    StringRedisTemplate template;

    @Override
    protected void doFilter(HttpServletRequest request,
                            HttpServletResponse response,
                            FilterChain chain) throws IOException, ServletException {
        String address = request.getRemoteAddr();
        if(this.tryCount(address)){
            chain.doFilter(request, response);
        }else {
            this.writeBlockMessage(response);
        }

    }
    private void writeBlockMessage(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.write(RestBean.failure(429, "请求频率过快，请稍后再试").asJsonString());
    }

    private boolean tryCount(String address) {
        synchronized (address.intern()){
        if(Boolean.TRUE.equals(template.hasKey(Const.FLOW_LIMIT_BLOCK + address))){
            return false;
        }
        return this.limitPeriodCheck(address);
    }

    }

    public boolean limitPeriodCheck(String address) {
       if(Boolean.TRUE.equals(template.hasKey(Const.FLOW_LIMIT_COUNTER + address))) {
           long increment = Optional.ofNullable(template.opsForValue().increment(Const.FLOW_LIMIT_COUNTER + address)).orElse(0L) ;
           if(increment >10) {
               template.opsForValue().set(Const.FLOW_LIMIT_BLOCK + address,   "",30, TimeUnit.SECONDS);
               return false;
           }
       }else {
           template.opsForValue().set(Const.FLOW_LIMIT_COUNTER+address,"1",3, TimeUnit.SECONDS);
       }
       return true;
    }
}
