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

    @GetMapping("/DanhSachLopHocGiaoVien")
    public String DanhSachLopHocGiaoVien(ModelMap model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String teacherId = authentication.getName();
        Person person = entityManager.find(Person.class, teacherId);
        Teachers teacher = (Teachers) person;

        if (teacher == null || !(teacher instanceof Teachers)) {
            throw new IllegalArgumentException("Không tìm thấy giáo viên với ID: " + teacherId);
        }

        // Lấy danh sách lớp mà giáo viên này tham gia
        List<ClassroomDetails> classroomDetails = entityManager.createQuery(
                        "from ClassroomDetails where member = :teacher",
                        ClassroomDetails.class)
                .setParameter("teacher", teacher)
                .getResultList();

        List<Rooms> rooms = new ArrayList<>();
        List<OnlineRooms> onlineRooms = new ArrayList<>();

        for (ClassroomDetails classroomDetail : classroomDetails) {
            Room room = classroomDetail.getRoom(); // Lấy đối tượng Room trực tiếp
            if (room instanceof OnlineRooms) {
                onlineRooms.add((OnlineRooms) room);
            } else if (room instanceof Rooms) {
                rooms.add((Rooms) room);
            }
        }

        model.addAttribute("classroomDetails", classroomDetails);
        model.addAttribute("rooms", rooms);
        model.addAttribute("onlineRooms", onlineRooms);

        return "DanhSachLopHocGiaoVien";
    }

    @GetMapping("ChiTietLopHocGiaoVien/{id}")
    public String ChiTietLopHocGiaoVien(@PathVariable String id, ModelMap model) {
        // Tìm lớp học (có thể là Rooms hoặc OnlineRooms)
        Object room = entityManager.find(Rooms.class, id);
        if (room == null) {
            room = entityManager.find(OnlineRooms.class, id);
        }

        if (room == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lớp học không tồn tại!");
        }

        String roomId;
        if (room instanceof Rooms) {
            roomId = ((Rooms) room).getRoomId();
        } else if (room instanceof OnlineRooms) {
            roomId = ((OnlineRooms) room).getRoomId();
        } else {
            throw new IllegalArgumentException("Loại phòng không hợp lệ!");
        }

        model.addAttribute("room", room);

        // Lấy danh sách bài đăng trong lớp
        List<Posts> posts = entityManager.createQuery("SELECT p FROM Posts p WHERE p.room.roomId = :roomId", Posts.class)
                .setParameter("roomId", roomId)
                .getResultList();


        // Lấy danh sách bình luận cho từng bài đăng
        for (Posts post : posts) {
            List<Comments> comments = entityManager.createQuery("SELECT c FROM Comments c WHERE c.post.postId = :postId", Comments.class)
                    .setParameter("postId", post.getPostId())
                    .getResultList();
            post.setComments(comments);
        }

        model.addAttribute("posts", posts);
        return "ChiTietLopHocGiaoVien";
    }

    @GetMapping("/ThanhVienTrongLopGiaoVien/{id}")
    public String ThanhVienTrongLopHoc(HttpSession session, @PathVariable String id, ModelMap model) {
        // Lấy đối tượng Room từ ID
        Room room = entityManager.find(Room.class, id);
        if (room == null) {
            throw new IllegalArgumentException("Không tìm thấy phòng với ID: " + id);
        }

        // Truy vấn danh sách thành viên trong lớp
        List<ClassroomDetails> classroomDetails = entityManager.createQuery(
                        "FROM ClassroomDetails WHERE room = :room", ClassroomDetails.class)
                .setParameter("room", room)
                .getResultList();

        List<Students> students = new ArrayList<>();
        List<Teachers> teachers = new ArrayList<>();
        for (ClassroomDetails classroomDetail : classroomDetails) {
            Person member = classroomDetail.getMember();  // Lấy đối tượng Person thay vì String ID

            if (member instanceof Students) {
                students.add((Students) member);
            } else if (member instanceof Teachers) {
                teachers.add((Teachers) member);
            }
        }

        // Log danh sách sinh viên để kiểm tra
        System.out.println("Students List: " + students);

        model.addAttribute("students", students);
        model.addAttribute("teachers", teachers);
        return "ThanhVienTrongLopGiaoVien";
    }


    @GetMapping("/TinNhanCuaGiaoVien")
    public String TinNhanCuaGiaoVien(HttpSession session, ModelMap model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String teacherId = authentication.getName();
        Person person = entityManager.find(Person.class, teacherId);
        Teachers teacher = (Teachers) person;

        // Truy vấn tin nhắn liên quan đến giáo viên
        List<Messages> messages = entityManager.createQuery(
                        "FROM Messages m WHERE m.sender = :teacher OR m.recipient = :teacher", Messages.class)
                .setParameter("teacher", teacher)
                .getResultList();

        // Tập hợp các liên hệ mà giáo viên đã trò chuyện
        Set<Person> contacts = new HashSet<>();
        for (Messages message : messages) {
            if (!message.getSender().equals(teacher)) {
                contacts.add(message.getSender());  // Người gửi khác giáo viên
            }
            if (!message.getRecipient().equals(teacher)) {
                contacts.add(message.getRecipient());  // Người nhận khác giáo viên
            }
        }
        model.addAttribute("contacts", contacts);  // Danh sách các liên hệ (người đã nhắn tin)
        return "TinNhanCuaGiaoVien";  // Trả về view
    }

    @GetMapping("/ChiTietTinNhanCuaGiaoVien/{id}")
    public String ChiTietTinNhanCuaGiaoVien(HttpSession session, ModelMap model, @PathVariable("id") String id) {
        // Tìm sinh viên bằng ID dạng String
        Students student = entityManager.find(Students.class, id);
        if (student == null) {
            return "redirect:/TinNhanCuaGiaoVien?error=StudentNotFound";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String teacherId = authentication.getName();
        Person person = entityManager.find(Person.class, teacherId);
        Teachers teacher = (Teachers) person;

        if (teacher == null) {
            return "redirect:/TinNhanCuaGiaoVien?error=TeacherNotFound";
        }

        // Truy vấn tin nhắn giữa giáo viên và học sinh
        List<Messages> messages = entityManager.createQuery(
                        "FROM Messages m WHERE " +
                                "(m.sender = :teacher AND m.recipient = :student) " +
                                "OR (m.sender = :student AND m.recipient = :teacher) " +
                                "ORDER BY m.datetime ASC", Messages.class)
                .setParameter("teacher", teacher)
                .setParameter("student", student)
                .getResultList();

        model.addAttribute("student", student);
        model.addAttribute("teacher", teacher);
        model.addAttribute("messages", messages);

        return "ChiTietTinNhanCuaGiaoVien";
    }

    @GetMapping("/TrangCaNhanGiaoVien")
    public String TrangCaNhanGiaoVien(HttpSession session, ModelMap model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String teacherId = authentication.getName();
        Person person = entityManager.find(Person.class, teacherId);
        Teachers teacher = (Teachers) person;
        model.addAttribute("teacher", teacher);
        return "TrangCaNhanGiaoVien";
    }
}
