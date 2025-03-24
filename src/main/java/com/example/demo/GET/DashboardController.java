package com.example.demo.GET;

import com.example.demo.OOP.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.checkerframework.checker.units.qual.s;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/")
public class DashboardController {

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/Dashboard")
    public String dashboard(Model model) {
        // Tổng số học viên
        Long totalStudents = (Long) entityManager.createQuery("SELECT COUNT(s) FROM Students s").getSingleResult();

        // Tổng số giáo viên
        Long totalTeachers = (Long) entityManager.createQuery("SELECT COUNT(t) FROM Teachers t").getSingleResult();

        // Tổng số nhân viên
        Long totalEmployees = (Long) entityManager.createQuery("SELECT COUNT(e) FROM Employees e").getSingleResult();

        Long totalBlog = (Long) entityManager.createQuery("SELECT COUNT(b) FROM Blogs b").getSingleResult();

        Long OnRoom = (Long) entityManager.createQuery("SELECT COUNT(o) FROM OnlineRooms o").getSingleResult();

        Long OffRoom = (Long) entityManager.createQuery("SELECT COUNT(o) FROM Rooms o").getSingleResult();

        Long totalMessages = (Long) entityManager.createQuery("SELECT COUNT(m) FROM Messages m").getSingleResult();

        // Truyền dữ liệu tổng số vào model
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalTeachers", totalTeachers);
        model.addAttribute("totalEmployees", totalEmployees);
        model.addAttribute("OnRoom", OnRoom);
        model.addAttribute("OffRoom", OffRoom);
        model.addAttribute("totalBlog", totalBlog);
        model.addAttribute("totalMessages", totalMessages);

        return "Dashboard"; // Trả về view Dashboard
    }

    @GetMapping("/api/Dashboard/stats")
    @ResponseBody
    public Map<String, Object> getDashboardStats() {
        // Tổng số học viên
        Long totalStudents = (Long) entityManager.createQuery("SELECT COUNT(s) FROM Students s").getSingleResult();

        // Tổng số giáo viên
        Long totalTeachers = (Long) entityManager.createQuery("SELECT COUNT(t) FROM Teachers t").getSingleResult();

        // Tổng số nhân viên
        Long totalEmployees = (Long) entityManager.createQuery("SELECT COUNT(e) FROM Employees e").getSingleResult();

        Long totalBlog = (Long) entityManager.createQuery("SELECT COUNT(b) FROM Blogs b").getSingleResult();

        Long OnRoom = (Long) entityManager.createQuery("SELECT COUNT(o) FROM OnlineRooms o").getSingleResult();

        Long OffRoom = (Long) entityManager.createQuery("SELECT COUNT(o) FROM Rooms o").getSingleResult();

        Long totalMessages = (Long) entityManager.createQuery("SELECT COUNT(m) FROM Messages m").getSingleResult();

        // Trả về dữ liệu dưới dạng JSON
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudents", totalStudents);
        stats.put("totalTeachers", totalTeachers);
        stats.put("totalEmployees", totalEmployees);
        stats.put("totalBlog", totalBlog);
        stats.put("OnRoom", OnRoom);
        stats.put("OffRoom", OffRoom);
        stats.put("totalMessages", totalMessages);
        return stats;
    }
}