package com.example.demo.ControllerGET;

import com.example.demo.ModelOOP.Teachers;

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
public class DashboardGiaoVien {

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/DashboardGiaoVien")
    public String dashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String teacherId = authentication.getName();

        Teachers teacher = entityManager.find(Teachers.class, teacherId);
        if (teacher == null) {
            throw new IllegalStateException("Giáo Viên không tồn tại");
        }

        Long totalTeacherComment = entityManager
                .createQuery("SELECT COUNT(c) FROM Comments c WHERE c.commenter.id = :teacherId", Long.class)
                .setParameter("teacherId", teacherId)
                .getSingleResult();

        Long totalTeacherMessage = entityManager
                .createQuery("SELECT COUNT(m) FROM Messages m WHERE m.sender.id = :teacherId", Long.class)
                .setParameter("teacherId", teacherId)
                .getSingleResult();

        LocalDate createdDate = teacher.getCreatedDate();
        LocalDate now = LocalDate.now();
        long daysActive = ChronoUnit.DAYS.between(createdDate, now);

        model.addAttribute("daysActive", daysActive);
        model.addAttribute("totalTeacherComment", totalTeacherComment);
        model.addAttribute("totalTeacherMessage", totalTeacherMessage);
        return "DashboardGiaoVien";
    }

    @GetMapping("/api/DashboardGiaoVien/stats")
    @ResponseBody
    public Map<String, Object> getDashboardStats() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String teacherId = authentication.getName();

        Teachers teacher = entityManager.find(Teachers.class, teacherId);
        if (teacher == null) {
            throw new IllegalStateException("Giáo Viên không tồn tại");
        }

        Long totalTeacherComment = entityManager
                .createQuery("SELECT COUNT(c) FROM Comments c WHERE c.commenter.id = :teacherId", Long.class)
                .setParameter("teacherId", teacherId)
                .getSingleResult();

        Long totalTeacherMessage = entityManager
                .createQuery("SELECT COUNT(m) FROM Messages m WHERE m.sender.id = :teacherId", Long.class)
                .setParameter("teacherId", teacherId)
                .getSingleResult();

        LocalDate createdDate = teacher.getCreatedDate();
        LocalDate now = LocalDate.now();
        long daysActive = ChronoUnit.DAYS.between(createdDate, now);

        Map<String, Object> stats = new HashMap<>();
        stats.put("daysActive", daysActive);
        stats.put("totalTeacherComment", totalTeacherComment);
        stats.put("totalTeacherMessage", totalTeacherMessage);
        return stats;
    }
}