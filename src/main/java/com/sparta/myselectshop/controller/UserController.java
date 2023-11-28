package com.sparta.myselectshop.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sparta.myselectshop.dto.SignupRequestDto;
import com.sparta.myselectshop.dto.UserInfoDto;
import com.sparta.myselectshop.entity.UserRoleEnum;
import com.sparta.myselectshop.jwt.JwtUtil;
import com.sparta.myselectshop.security.UserDetailsImpl;
import com.sparta.myselectshop.service.FolderService;
import com.sparta.myselectshop.service.KakaoService;
import com.sparta.myselectshop.service.UserService;
import io.jsonwebtoken.Jwt;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final FolderService folderService;
    private final KakaoService kakaoService;

    @GetMapping("/user/login-page")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/user/signup")
    public String signupPage() {
        return "signup";
    }

    @PostMapping("/user/signup")
    public String signup(@Valid SignupRequestDto requestDto, BindingResult bindingResult) {
        // Validation 예외처리
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        if (fieldErrors.size() > 0) {
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                log.error(fieldError.getField() + " 필드 : " + fieldError.getDefaultMessage());
            }
            return "redirect:/api/user/signup";
        }

        userService.signup(requestDto);

        return "redirect:/api/user/login-page";
    }

    // 회원 관련 정보 받기
    @GetMapping("/user-info")
    @ResponseBody
    public UserInfoDto getUserInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        String username = userDetails.getUser().getUsername();
        UserRoleEnum role = userDetails.getUser().getRole();
        boolean isAdmin = (role == UserRoleEnum.ADMIN);

        return new UserInfoDto(username, isAdmin);
    }

    @GetMapping("/user-folder")
    public String getUserInfo(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        // 메서드 이름이 같지만 ㄱㅊ. 오버로딩을 사용한 것임.

        //model을 통해 데이터를 넘겨준다.
        model.addAttribute("folders", folderService.getFolders(userDetails.getUser()));
        //"folders"로 넘기기로 클라이언트와 약속 한 것.

        return "index :: #fragment";  //따로 학습할 필요x, 프로젝트에 동적으로 추가되도록 넣어줌.
    }

    // 카카오에서 제공하는 인가코드를 받아오는 부분
    @GetMapping("/user/kakao/callback")   // 카카오 developers의 어플리케이션 등록 할 때 넣어줬던 path를 여기에 넣어주는 것.
    public String kakaoLogin(@RequestParam String code, HttpServletResponse response) throws JsonProcessingException { // 카카오에서 쿼리스트링방식으로 인가코드를 전해줌 -> requestParam으로 받는다. //JWT를 생성해서 쿠키를 직접 만들고, 그 쿠키에 JWT를 넣어서 전달 -> 브라우저에 자동으로 set 될 수 있도록 만들 예정. (이전엔 헤더에 넣어 보냈다)
        String token = kakaoService.kakaoLogin(code);

        Cookie cookie = new Cookie(JwtUtil.AUTHORIZATION_HEADER, token.substring(7));  // Cookie에는 띄어쓰기 못넣는다. -> "Bearer "로 시작하기 때문에 오류 -> substring해줬음
        cookie.setPath("/");
        response.addCookie(cookie); //successfulAuthentication과 같은 역할

        return "redirect:/";
    }
}