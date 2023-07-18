package com.uch.finalproject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uch.finalproject.model.BaseResponse;
import com.uch.finalproject.model.FoodResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter(urlPatterns = "/*")
public class MyFilter extends OncePerRequestFilter {
    // 不需要判斷登入狀態的網址
    Set<String> ALLOW_PATTERNS = new HashSet<>(Arrays.asList("/login", "/ok"));

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 如果目前網址在白名單內，直接跳過filter檢查
        if(ALLOW_PATTERNS.contains(request.getServletPath()) || request.getServletPath().contains(".")) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpSession httpSession = request.getSession();

         // 檢查使用者是否登入
        if(httpSession.getAttribute("loginStatus") == null) {
            ObjectMapper objectMapper = new ObjectMapper();
            String resp = objectMapper.writeValueAsString(new BaseResponse(9, "未登入"));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("utf-8");
            response.getWriter().println(resp);

        } else
            // 已登入，繼續完成API的功能
            filterChain.doFilter(request, response);
    }
    
}
