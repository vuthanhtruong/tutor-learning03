package com.example.demo.ControllerGET;

import com.example.demo.ModelOOP.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/")
@Transactional
public class StudentGet {
    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/DangKyHocSinh")
    public String DangKyHocSinh(ModelMap model) {
        List<Employees> employees = entityManager.createQuery("from Employees", Employees.class).getResultList();
        model.addAttribute("employees", employees);
        return "DangKyHocSinh";
    }

    @GetMapping("/TrangChuHocSinh")
    public String TrangChuHocSinh(
            @RequestParam(defaultValue = "1") int pageDocs,
            @RequestParam(defaultValue = "1") int pagePosts,
            @RequestParam(defaultValue = "1") int pageMessages,
            @RequestParam(defaultValue = "1") int pageBlogs,
            @RequestParam(defaultValue = "5") int pageSize,
            ModelMap model,
            HttpSession session
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String studentId = authentication.getName();
        Person person = entityManager.find(Person.class, studentId);
        Students student = (Students) person;

        // Lưu pageSize vào session
        session.setAttribute("pageSize", pageSize);

        // Lấy danh sách phòng học của học sinh
        List<Room> studentRooms = entityManager.createQuery(
                        "SELECT cd.room FROM ClassroomDetails cd WHERE cd.member = :student", Room.class)
                .setParameter("student", student)
                .getResultList();

        // Tạo danh sách giáo viên
        Set<Teachers> teachers = new HashSet<>();
        for (Room room : studentRooms) {
            List<ClassroomDetails> members = entityManager.createQuery(
                            "SELECT cd FROM ClassroomDetails cd WHERE cd.room.roomId = :roomId", ClassroomDetails.class)
                    .setParameter("roomId", room.getRoomId())
                    .getResultList();
            for (ClassroomDetails member : members) {
                if (member.getMember() instanceof Teachers) {
                    teachers.add((Teachers) member.getMember());
                }
            }
        }

        // 1. Phân trang cho Documents
        Long totalDocs = (Long) entityManager.createQuery(
                        "SELECT COUNT(d) FROM Documents d " +
                                "WHERE d.creator != :student " + // Loại bỏ tài liệu do học sinh tạo
                                "AND d.post.room IN :studentRooms") // Chỉ lấy từ các phòng của học sinh
                .setParameter("student", student)
                .setParameter("studentRooms", studentRooms)
                .getSingleResult();
        int totalPagesDocs = Math.max(1, (int) Math.ceil((double) totalDocs / pageSize));
        pageDocs = Math.max(1, Math.min(pageDocs, totalPagesDocs));
        int firstResultDocs = (pageDocs - 1) * pageSize;
        List<Documents> documents = entityManager.createQuery(
                        "SELECT d FROM Documents d " +
                                "WHERE d.creator != :student " +
                                "AND d.post.room IN :studentRooms " +
                                "ORDER BY d.event.eventDate DESC", Documents.class) // Sắp xếp theo ngày sự kiện
                .setParameter("student", student)
                .setParameter("studentRooms", studentRooms)
                .setFirstResult(firstResultDocs)
                .setMaxResults(pageSize)
                .getResultList();

        // 2. Phân trang cho Posts
        Long totalPosts = (Long) entityManager.createQuery(
                        "SELECT COUNT(p) FROM Posts p " +
                                "WHERE p.creator != :student " + // Loại bỏ bài đăng do học sinh tạo
                                "AND p.room IN :studentRooms") // Chỉ lấy từ các phòng của học sinh
                .setParameter("student", student)
                .setParameter("studentRooms", studentRooms)
                .getSingleResult();
        int totalPagesPosts = Math.max(1, (int) Math.ceil((double) totalPosts / pageSize));
        pagePosts = Math.max(1, Math.min(pagePosts, totalPagesPosts));
        int firstResultPosts = (pagePosts - 1) * pageSize;
        List<Posts> posts = entityManager.createQuery(
                        "SELECT p FROM Posts p " +
                                "WHERE p.creator != :student " +
                                "AND p.room IN :studentRooms " +
                                "ORDER BY p.event.eventDate DESC", Posts.class)
                .setParameter("student", student)
                .setParameter("studentRooms", studentRooms)
                .setFirstResult(firstResultPosts)
                .setMaxResults(pageSize)
                .getResultList();

        // 3. Phân trang cho Messages
        Long totalMessages = (Long) entityManager.createQuery(
                        "SELECT COUNT(m) FROM Messages m " +
                                "WHERE m.sender != :student AND m.recipient = :student ")// Tin nhắn gửi đến học sinh
                .setParameter("student", student)
                .getSingleResult();
        int totalPagesMessages = Math.max(1, (int) Math.ceil((double) totalMessages / pageSize));
        pageMessages = Math.max(1, Math.min(pageMessages, totalPagesMessages));
        int firstResultMessages = (pageMessages - 1) * pageSize;
        List<Messages> messagesList = entityManager.createQuery(
                        "SELECT m FROM Messages m " +
                                "WHERE m.sender != :student AND m.recipient = :student ", Messages.class)
                .setParameter("student", student)
                .setFirstResult(firstResultMessages)
                .setMaxResults(pageSize)
                .getResultList();
        Collections.reverse(messagesList);

        // 4. Phân trang cho Blogs
        Long totalBlogs = (Long) entityManager.createQuery(
                        "SELECT COUNT(b) FROM Blogs b " +
                                "WHERE b.creator != :student ")
                .setParameter("student", student)
                .getSingleResult();
        int totalPagesBlogs = Math.max(1, (int) Math.ceil((double) totalBlogs / pageSize));
        pageBlogs = Math.max(1, Math.min(pageBlogs, totalPagesBlogs));
        int firstResultBlogs = (pageBlogs - 1) * pageSize;
        List<Blogs> blogs = entityManager.createQuery(
                        "SELECT b FROM Blogs b " +
                                "WHERE b.creator != :student ", Blogs.class)
                .setParameter("student", student)
                .setFirstResult(firstResultBlogs)
                .setMaxResults(pageSize)
                .getResultList();
        Collections.reverse(blogs);

        // Truyền dữ liệu lên giao diện
        model.addAttribute("documents", documents);
        model.addAttribute("posts", posts);
        model.addAttribute("messages", messagesList); // Giữ tên "messages" như HTML yêu cầu
        model.addAttribute("teachers", teachers);
        model.addAttribute("blogs", blogs);

        // Thông tin phân trang
        model.addAttribute("currentPageDocs", pageDocs);
        model.addAttribute("totalPagesDocs", totalPagesDocs);
        model.addAttribute("currentPagePosts", pagePosts);
        model.addAttribute("totalPagesPosts", totalPagesPosts);
        model.addAttribute("currentPageMessages", pageMessages);
        model.addAttribute("totalPagesMessages", totalPagesMessages);
        model.addAttribute("currentPageBlogs", pageBlogs);
        model.addAttribute("totalPagesBlogs", totalPagesBlogs);
        model.addAttribute("pageSize", pageSize);

        return "TrangChuHocSinh";
    }

    @GetMapping("/DangXuatHocSinh")
    public String DangXuatGiaoVien(HttpSession session) {
        return "redirect:/DangNhapHocSinh";
    }


}
