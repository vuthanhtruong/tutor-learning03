package com.example.demo.ControllerGET;

import com.example.demo.ModelOOP.Students;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/")
public class DashboardHocSinh {

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/DashboardHocSinh")
    public String dashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String studentId = authentication.getName();

        Students student = entityManager.find(Students.class, studentId);
        if (student == null) {
            throw new IllegalStateException("Học sinh không tồn tại");
        }

        Long totalStudenComment = entityManager
                .createQuery("SELECT COUNT(c) FROM Comments c WHERE c.commenter.id = :studentId", Long.class)
                .setParameter("studentId", studentId)
                .getSingleResult();

        Long totalStudentMessage = entityManager
                .createQuery("SELECT COUNT(m) FROM Messages m WHERE m.sender.id = :studentId", Long.class)
                .setParameter("studentId", studentId)
                .getSingleResult();

        LocalDate createdDate = student.getCreatedDate();
        LocalDate now = LocalDate.now();
        long daysActive = ChronoUnit.DAYS.between(createdDate, now);

        model.addAttribute("daysActive", daysActive);
        model.addAttribute("totalStudenComment", totalStudenComment);
        model.addAttribute("totalStudentMessage", totalStudentMessage);
        return "DashboardHocSinh";
    }

    @GetMapping("/api/DashboardHocSinh/stats")
    @ResponseBody
    public Map<String, Object> getDashboardStats() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String studentId = authentication.getName();

        Students student = entityManager.find(Students.class, studentId);
        if (student == null) {
            throw new IllegalStateException("Học sinh không tồn tại");
        }

        LocalDate createdDate = student.getCreatedDate();
        LocalDate now = LocalDate.now();
        long daysActive = ChronoUnit.DAYS.between(createdDate, now);

        Long totalStudenComment = entityManager
                .createQuery("SELECT COUNT(c) FROM Comments c WHERE c.commenter.id = :studentId", Long.class)
                .setParameter("studentId", studentId)
                .getSingleResult();

        Long totalStudentMessage = entityManager
                .createQuery("SELECT COUNT(m) FROM Messages m WHERE m.sender.id = :studentId", Long.class)
                .setParameter("studentId", studentId)
                .getSingleResult();

        Map<String, Object> stats = new HashMap<>();
        stats.put("daysActive", daysActive);
        stats.put("totalStudenComment", totalStudenComment);
        stats.put("totalStudentMessage", totalStudentMessage);
        return stats;
    }
}