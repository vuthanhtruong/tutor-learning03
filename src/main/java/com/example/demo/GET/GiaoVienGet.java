package com.example.demo.GET;

import com.example.demo.OOP.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Controller
@RequestMapping("/")
@Transactional
public class GiaoVienGet {

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/DangKyGiaoVien")
    public String DangKyGiaoVien(ModelMap model) {
        // Sử dụng EntityManager để thực thi truy vấn
        List<Employees> employees = entityManager.createQuery("from Employees", Employees.class).getResultList();
        model.addAttribute("employees", employees);
        return "DangKyGiaoVien";
    }

    @GetMapping("/TrangChuGiaoVien")
    public String TrangChuGiaoVien(ModelMap model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String teacherId = authentication.getName();
        Person person = entityManager.find(Person.class, teacherId);
        Teachers teachers = (Teachers) person;
        // Lấy danh sách tài liệu từ cơ sở dữ liệu
        List<Documents> documents = entityManager.createQuery(
                        "SELECT d FROM Documents d " +
                                "WHERE d.creator != :teacher AND EXISTS ( " +
                                "   SELECT 1 FROM ClassroomDetails cd " +
                                "   WHERE cd.member = d.creator AND cd.room IN ( " +
                                "       SELECT cd2.room FROM ClassroomDetails cd2 WHERE cd2.member = :teacher " +
                                "   ) " +
                                ")", Documents.class)
                .setParameter("teacher", teachers)
                .getResultList();
        Collections.reverse(documents);
        List<Posts> posts = entityManager.createQuery(
                        "SELECT p FROM Posts p " +
                                "WHERE p.creator != :teacher AND EXISTS ( " +
                                "   SELECT 1 FROM ClassroomDetails cd " +
                                "   WHERE cd.member = p.creator AND cd.room IN ( " +
                                "       SELECT cd2.room FROM ClassroomDetails cd2 WHERE cd2.member = :teacher " +
                                "   ) " +
                                ")", Posts.class)
                .setParameter("teacher", teachers)
                .getResultList();
        Collections.reverse(posts);

        List<Messages> messagesList = entityManager.createQuery(
                        "SELECT m FROM Messages m " +
                                "WHERE m.sender != :teacher AND m.recipient = :teacher", Messages.class)
                .setParameter("teacher", teachers)
                .getResultList();
        Collections.reverse(messagesList);
        model.addAttribute("teacher", teachers);
        model.addAttribute("documents", documents);
        model.addAttribute("posts", posts);
        model.addAttribute("messagesList", messagesList);
        return "TrangChuGiaoVien";
    }

