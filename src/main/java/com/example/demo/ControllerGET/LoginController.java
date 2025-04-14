package com.example.demo.ControllerGET;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("usernamePasswordError", "Incorrect account or password");
        }
        return "TrangChu";  // Trả về trang login.html
    }

    @GetMapping("/redirect")
    public String redirectAfterLogin(Authentication authentication, HttpServletRequest request, RedirectAttributes redirectAttributes)
            throws ServletException {
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addAttribute("usernamePasswordError", "Tài khoản hoặc mật khẩu không chính xác");
            return "redirect:/TrangChu";
        }

        System.out.println("✅ Đăng nhập: " + authentication.getName());
        authentication.getAuthorities().forEach(auth ->
                System.out.println("🔹 Quyền: " + auth.getAuthority())
        );

        String redirectUrl = determineRedirectUrl(authentication.getAuthorities());
        if (redirectUrl == null) {
            System.out.println("❌ Không tìm thấy quyền hợp lệ, đăng xuất...");
            request.logout();
            redirectAttributes.addAttribute("roleError", "Không tìm thấy quyền hợp lệ");
            return "redirect:/TrangChu";
        }

        return "redirect:" + redirectUrl;
    }

    // Xử lý đăng nhập bằng khuôn mặt
    @PostMapping("/auth/verify-face-login")
    public String verifyFaceLogin(@RequestParam("image") String faceData, Model model) {
        if (faceData == null || faceData.isEmpty()) {
            model.addAttribute("faceError", "Ảnh khuôn mặt không hợp lệ");
            return "redirect:/TrangChu";
        }
        System.out.println("Received face data length: " + faceData.length());

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken("face-login", faceData)
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            String redirectUrl = determineRedirectUrl(auth.getAuthorities());
            return "redirect:" + (redirectUrl != null ? redirectUrl : "/TrangChu");
        } catch (AuthenticationException e) {
            e.printStackTrace();
            model.addAttribute("faceError", "Không nhận diện được khuôn mặt hoặc không tìm thấy người dùng");
            return "redirect:/TrangChu";
        }
    }

    // Xử lý đăng nhập bằng giọng nói
    @PostMapping("/DangNhapBangGiongNoi")
    public String dangNhapBangGiongNoi(@RequestParam("voiceData") String voiceData, RedirectAttributes redirectAttributes) {
        if (voiceData == null || voiceData.isEmpty()) {
            redirectAttributes.addAttribute("voiceError", "Giọng nói không hợp lệ");
            return "redirect:/TrangChu";
        }
        System.out.println("Received voice data length: " + voiceData.length());

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken("voice-login", voiceData)
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            String redirectUrl = determineRedirectUrl(auth.getAuthorities());
            return "redirect:" + (redirectUrl != null ? redirectUrl : "/TrangChu");
        } catch (AuthenticationException e) {
            e.printStackTrace();
            redirectAttributes.addAttribute("voiceError", "Giọng nói không khớp hoặc không tìm thấy người dùng");
            return "redirect:/TrangChu";
        }
    }

    // Phương thức chung để xác định URL chuyển hướng dựa trên role
    private String determineRedirectUrl(Iterable<? extends GrantedAuthority> authorities) {
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals("ROLE_ADMIN")) {
                return "/TrangChuAdmin";
            } else if (authority.getAuthority().equals("ROLE_EMPLOYEE")) {
                return "/TrangChuNhanVien";
            } else if (authority.getAuthority().equals("ROLE_TEACHER")) {
                return "/TrangChuGiaoVien";
            } else if (authority.getAuthority().equals("ROLE_STUDENT")) {
                return "/TrangChuHocSinh";
            }
        }
        System.err.println("No valid role found, defaulting to /TrangChu");
        return null;
    }
}