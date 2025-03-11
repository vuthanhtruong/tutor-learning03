package com.example.demo.POST;

import com.example.demo.OOP.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/")
@Transactional
public class StudentPost {
    private static final Logger log = LoggerFactory.getLogger(GiaoVienPost.class);
    @PersistenceContext
    private EntityManager entityManager;
    @Value("${file.upload-dir:C:/uploads}")
    private String uploadDir;

    @PostMapping("/DangKyHocSinh")
    public String DangKyHocSinh(
            @RequestParam("EmployeeID") String employeeID,
            @RequestParam("StudentID") String studentID,
            @RequestParam("FirstName") String firstName,
            @RequestParam("LastName") String lastName,
            @RequestParam("Email") String email,
            @RequestParam("PhoneNumber") String phoneNumber,
            @RequestParam(value = "MisId", required = false) String misId, // Fix tên biến
            @RequestParam("Password") String password,
            @RequestParam("ConfirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {
        // Kiểm tra mật khẩu
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorPassword", "Mật khẩu nhập lại không khớp!");
            return "redirect:/DangKyHocSinh";
        }

        // Kiểm tra nhân viên
        Employees employee = entityManager.find(Employees.class, employeeID);
        if (employee == null) {
            redirectAttributes.addFlashAttribute("errorEmployee", "Mã nhân viên không hợp lệ!");
            return "redirect:/DangKyHocSinh";
        }

        // Kiểm tra StudentID đã tồn tại chưa
        if (entityManager.find(Students.class, studentID) != null) {
            redirectAttributes.addFlashAttribute("errorStudentID", "Mã học sinh đã tồn tại!");
            return "redirect:/DangKyHocSinh";
        }

        // Kiểm tra Email
        TypedQuery<Students> emailQuery = entityManager.createQuery(
                "SELECT s FROM Students s WHERE s.email = :email", Students.class);
        emailQuery.setParameter("email", email);
        List<Students> existingStudents = emailQuery.getResultList();

        if (!existingStudents.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorEmail", "Email đã tồn tại!");
            return "redirect:/DangKyHocSinh";
        }

        // Kiểm tra độ mạnh của mật khẩu
        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$")) {
            redirectAttributes.addFlashAttribute("errorPassword", "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt!");
            return "redirect:/DangKyHocSinh";
        }

        // Lấy admin
        List<Admin> admins = entityManager.createQuery("FROM Admin", Admin.class).getResultList();
        Admin admin = admins.get(0);

        // Tạo học sinh mới
        Students student = new Students();
        student.setId(studentID);
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setEmail(email);
        student.setPhoneNumber(phoneNumber);
        student.setPassword(password);
        student.setMisId(misId);
        student.setEmployee(employee);
        student.setAdmin(admin);

        // Lưu vào database
        entityManager.persist(student);

        // Chuyển hướng đến trang đăng nhập với thông báo thành công
        redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
        return "redirect:/DangNhapHocSinh";
    }

    @PostMapping("/DangNhapHocSinh")
    public String DangNhapHocSinh(@RequestParam("studentID") String studentID,
                                  @RequestParam("password") String password,
                                  ModelMap model,
                                  HttpSession session) {
        try {
            Students student = entityManager.find(Students.class, studentID);

            if (student != null && student.getPassword().equals(password)) {
                return "redirect:/TrangChuHocSinh";
            } else {
                model.addAttribute("error", "Mã học sinh hoặc mật khẩu không đúng!");
                return "redirect:/DangNhapHocSinh";
            }
        } catch (NoResultException e) {
            model.addAttribute("error", "Mã học sinh không tồn tại!");
            return "redirect:/DangNhapHocSinh";
        }
    }

    @Transactional
    @PostMapping("/BinhLuanHocSinh")
    public String themBinhLuan(@RequestParam("postId") Long postId,
                               @RequestParam("commentText") String commentText, SessionStatus sessionStatus, HttpSession session) {
        // Lấy thông tin người bình luận
        Person commenter = entityManager.find(Person.class, session.getAttribute("StudentID"));

        // Lấy thông tin bài đăng
        Posts post = entityManager.find(Posts.class, postId);
        if (post == null) {
            throw new IllegalArgumentException("Không tìm thấy bài đăng với ID: " + postId);
        }

        // Tạo mới bình luận
        Comments comment = new Comments(commenter, post, commentText);
        Events event = entityManager.find(Events.class, 5);
        comment.setEvent(event);

        // Lưu vào database
        entityManager.persist(comment);
        return "redirect:/ChiTietLopHocHocSinh/" + post.getRoom().getRoomId();
    }



}
