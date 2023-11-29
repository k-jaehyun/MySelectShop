package com.sparta.myselectshop.mvc;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

public class MockSpringSecurityFilter implements Filter {  // 스프링에서 제공하는 기본 filter. Test하는데 security가 방해가 되기 때문에, 그걸 모방한 필터를 직접 구현.
    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        SecurityContextHolder.getContext()   //securityContextHolder 의 context 호출
                .setAuthentication((Authentication) ((HttpServletRequest) req).getUserPrincipal());  //Authentication을 정의 해줌 -> Principal을 받아서 Authentication 객체로 바꾸고 넣어줌 -> "가짜 인증" 과정
        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
        SecurityContextHolder.clearContext();
    }
}