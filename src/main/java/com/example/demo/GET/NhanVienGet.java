package com.example.demo.GET;

import com.example.demo.OOP.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/")
@Transactional
public class NhanVienGet {

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private JavaMailSender mailSender; // Không khai báo lại ở nơi khác


    @GetMapping("/DangKyNhanVien")
    public String DangKyNhanVien() {
        return "DangKyNhanVien";
    }
    @GetMapping("/TrangChuNhanVien")
    public String TrangChuNhanVien(ModelMap model, HttpSession session) {
        if(session.getAttribute("EmployeeID") == null) {
            return "redirect:/DangNhapNhanVien";
        }
        Employees employee = entityManager.find(Employees.class, session.getAttribute("EmployeeID"));
        model.addAttribute("employee", employee);
        return "TrangChuNhanVien";
    }
    @GetMapping("/DangXuatNhanVien")
    public String DangXuatGiaoVien(HttpSession session) {
        session.invalidate();
        return "redirect:/DangNhapNhanVien";
    }
    @GetMapping("/DanhSachGiaoVienCuaBan")
    public String DanhSachGiaoVienCuaBan(ModelMap model, HttpSession session) {
        if(session.getAttribute("EmployeeID") == null) {
            return "redirect:/DangNhapNhanVien";
        }
        List<Teachers> teachers = entityManager.createQuery("from Teachers").getResultList();
        model.addAttribute("teachers", teachers);
        return "DanhSachGiaoVienCuaBan";
    }
    @GetMapping("/ThemGiaoVienCuaBan")
    public String ThemGiaoVienCuaBan(HttpSession session, ModelMap model) {
        if(session.getAttribute("EmployeeID") == null) {
            return "redirect:/DangNhapNhanVien";
        }
        return "ThemGiaoVienCuaBan";
    }
    @GetMapping("/DanhSachHocSinhCuaBan")
    public String DanhSachHocSinhCuaBan(ModelMap model, HttpSession session) {
        if(session.getAttribute("EmployeeID") == null) {
            return "redirect:/DangNhapNhanVien";
        }
        List<Students> students = entityManager.createQuery("from Students ").getResultList();
        model.addAttribute("students", students);
        return "DanhSachHocSinhCuaBan";
    }
    @GetMapping("/ThemHocSinhCuaBan")
    public String ThemHocSinhCuaBan(ModelMap model, HttpSession session) {
        if(session.getAttribute("EmployeeID") == null) {
            return "redirect:/DangNhapNhanVien";
        }
        return "ThemHocSinhCuaBan";
    }
    @GetMapping("/DanhSachNguoiDungHeThong")
    public String DanhSachNguoiDungHeThong(HttpSession session, ModelMap model) {
        if(session.getAttribute("EmployeeID") == null) {
            return "redirect:/DangNhapNhanVien";
        }
        if(session.getAttribute("EmployeeID") == null) {
            return "redirect:/DangNhapNhanVien";
        }
        List<Employees> employee = entityManager.createQuery("from Employees ").getResultList();
        List<Teachers> teachers = entityManager.createQuery("from Teachers ").getResultList();
        List<Students> students = entityManager.createQuery("from Students ").getResultList();
        Employees employee1 = entityManager.find(Employees.class, session.getAttribute("EmployeeID"));
        Admin admin = employee1.getAdmin();
        model.addAttribute("employee", employee);
        model.addAttribute("teachers", teachers);
        model.addAttribute("students", students);
        model.addAttribute("employee", employee);
        model.addAttribute("admin", admin);
        return "DanhSachNguoiDungHeThong";
    }
    @GetMapping("/XoaGiaoVienCuaBan/{id}")
    @Transactional
    public String XoaGiaoVienCuaBan(@PathVariable String id, ModelMap model, HttpSession session) {
        if (session.getAttribute("EmployeeID") == null) {
            return "redirect:/DangNhapNhanVien";
        }

        Teachers teacher = entityManager.find(Teachers.class, id);
        if (teacher == null) {
            model.addAttribute("error", "Không tìm thấy giáo viên cần xóa!");
            return "DanhSachGiaoVienCuaBan";
        }

        // Xóa giáo viên sau khi đã xóa dữ liệu liên quan
        entityManager.remove(teacher);

        return "redirect:/DanhSachGiaoVienCuaBan";
    }

    @GetMapping("/XoaHocSinhCuaBan/{id}")
    @Transactional
    public String XoaHocSinhCuaBan(@PathVariable String id, ModelMap model, HttpSession session) {
        if (session.getAttribute("EmployeeID") == null) {
            return "redirect:/DangNhapNhanVien";
        }

        Students student = entityManager.find(Students.class, id);
        if (student == null) {
            model.addAttribute("error", "Không tìm thấy học sinh cần xóa!");
            return "DanhSachHocSinhCuaBan";
        }

        // Xóa tin nhắn liên quan trước
        entityManager.createQuery("DELETE FROM Messages m WHERE m.recipient.id = :studentID OR m.sender.id = :studentID")
                .setParameter("studentID", id)
                .executeUpdate();

        // Sau đó mới xóa học sinh
        entityManager.remove(student);

        return "redirect:/DanhSachHocSinhCuaBan";
    }


    @GetMapping("/SuaGiaoVienCuaBan/{id}")
    public String SuaGiaoVienCuaBan(ModelMap model, @PathVariable("id") String id, HttpSession session) {
        if(session.getAttribute("EmployeeID") == null) {
            return "redirect:/DangNhapNhanVien";
        }
        Teachers teachers=entityManager.find(Teachers.class, id);
        model.addAttribute("teachers", teachers);
        return "SuaGiaoVienCuaBan";
    }
    @GetMapping("/SuaHocSinhCuaBan/{id}")
    public String SuaHocSinhCuaBan(ModelMap model, @PathVariable("id") String id, HttpSession session) {
        if(session.getAttribute("EmployeeID") == null) {
            return "redirect:/DangNhapNhanVien";
        }
        Students students=entityManager.find(Students.class, id);
        model.addAttribute("students", students);
        return "SuaHocSinhCuaBan";
    }
    @GetMapping("/DanhSachPhongHoc")
    public String DanhSachPhongHoc(ModelMap model, HttpSession session) {
        if (session.getAttribute("EmployeeID") == null) {
            return "redirect:/DangNhapNhanVien";
        }

        // Lấy danh sách phòng học offline
        List<Rooms> offlineRooms = entityManager.createQuery("from Rooms", Rooms.class).getResultList();

        // Lấy danh sách phòng học online
        List<OnlineRooms> onlineRooms = entityManager.createQuery("from OnlineRooms", OnlineRooms.class).getResultList();


        model.addAttribute("rooms", offlineRooms);
        model.addAttribute("roomsonline", onlineRooms);
        return "DanhSachPhongHoc";
    }

    @GetMapping("/ThemPhongHoc")
    public String ThemPhongHoc(ModelMap model, HttpSession session) {
        if(session.getAttribute("EmployeeID") == null) {
            return "redirect:/DangNhapNhanVien";
        }
        return "ThemPhongHoc";
    }
    @GetMapping("/ThemPhongHocOnline")
    public String ThemPhongHocOnline(ModelMap model, HttpSession session) {
        if(session.getAttribute("EmployeeID") == null) {
            return "redirect:/DangNhapNhanVien";
        }
        return "ThemPhongHocOnline";
    }
    @GetMapping("/SuaPhongHocOffline/{id}")
    public String SuaPhongHoc(ModelMap model, @PathVariable("id") String id, HttpSession session) {
        if(session.getAttribute("EmployeeID") == null) {
            return "redirect:/DangNhapNhanVien";
        }
        Rooms room = entityManager.find(Rooms.class, id);

        if (room == null) {
            return "redirect:/DanhSachPhongHoc?error=RoomNotFound";
        }
        model.addAttribute("room", room);

        return "SuaPhongHoc";
    }
    @GetMapping("/SuaPhongHocOnline/{id}")
    public String SuaPhongHocOnline(@PathVariable("id") String roomId, ModelMap model, HttpSession session) {
        if (session.getAttribute("EmployeeID") == null) {
            return "redirect:/DangNhapNhanVien";
        }

        // Tìm phòng học online theo ID
        OnlineRooms room = entityManager.find(OnlineRooms.class, roomId);
        if (room == null) {
            return "redirect:/DanhSachPhongHoc?error=RoomNotFound";
        }

        model.addAttribute("room", room);
        return "SuaPhongHocOnline";  // Trả về trang chỉnh sửa phòng online
    }

    @Transactional
    @GetMapping("/XoaPhongHocOffline/{id}")
    public String XoaPhongHocOffline(@PathVariable("id") String id) {
        // Tìm phòng học theo ID
        Rooms room = entityManager.find(Rooms.class, id);

        if (room == null) {
            return "redirect:/DanhSachPhongHoc?error=RoomNotFound";
        }

        try {
            // Xóa tất cả ClassroomDetails liên quan đến phòng học này
            int deletedDetails = entityManager.createQuery("DELETE FROM ClassroomDetails c WHERE c.room = :room")
                    .setParameter("room", room)
                    .executeUpdate();
            System.out.println("Deleted " + deletedDetails + " ClassroomDetails records.");

            // Xóa tất cả ScheduleNotifications liên quan đến phòng học này
            int deletedNotifications = entityManager.createQuery("DELETE FROM ScheduleNotifications s WHERE s.room = :room")
                    .setParameter("room", room)
                    .executeUpdate();
            System.out.println("Deleted " + deletedNotifications + " ScheduleNotifications records.");

            // Xóa phòng học
            entityManager.remove(room);

            return "redirect:/DanhSachPhongHoc?success=RoomDeleted";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/DanhSachPhongHoc?error=DeleteFailed";
        }
    }



    @Transactional
    @GetMapping("/XoaPhongHocOnline/{id}")
    public String XoaPhongHocOnline(@PathVariable("id") String id) {
        // Tìm phòng học online theo ID
        OnlineRooms room = entityManager.find(OnlineRooms.class, id);

        if (room == null) {
            return "redirect:/DanhSachPhongHoc?error=RoomNotFound";
        }

        try {
            // Xóa tất cả ClassroomDetails liên quan đến phòng học này
            int deletedDetails = entityManager.createQuery("DELETE FROM ClassroomDetails c WHERE c.room = :room")
                    .setParameter("room", room)
                    .executeUpdate();

            System.out.println("Deleted " + deletedDetails + " ClassroomDetails records.");

            // Xóa tất cả ScheduleNotifications liên quan đến phòng học này
            int deletedNotifications = entityManager.createQuery("DELETE FROM ScheduleNotifications s WHERE s.room = :room")
                    .setParameter("room", room)
                    .executeUpdate();

            System.out.println("Deleted " + deletedNotifications + " ScheduleNotifications records.");

            // Xóa phòng học online
            entityManager.remove(room);

            return "redirect:/DanhSachPhongHoc?success=RoomDeleted";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/DanhSachPhongHoc?error=DeleteFailed";
        }
    }


    @GetMapping("/BoTriLopHoc")
    public String BoTriLopHoc(ModelMap model, HttpSession session) {
        if (session.getAttribute("EmployeeID") == null) {
            return "redirect:/DangNhapNhanVien";
        }

        // Lấy danh sách phòng học offline
        List<Rooms> offlineRooms = entityManager.createQuery("FROM Rooms", Rooms.class).getResultList();

        // Lấy danh sách phòng học online
        List<OnlineRooms> onlineRooms = entityManager.createQuery("FROM OnlineRooms", OnlineRooms.class).getResultList();

        // Đưa từng danh sách vào model
        model.addAttribute("offlineRooms", offlineRooms);
        model.addAttribute("onlineRooms", onlineRooms);

        return "BoTriLopHoc";
    }
    @GetMapping("/ChiTietLopHoc/{id}")
    public String ChiTietLopHoc(ModelMap model, @PathVariable("id") String id, HttpSession session) {
        // Kiểm tra xem EmployeeID có trong session không
        if (session.getAttribute("EmployeeID") == null) {
            return "redirect:/DangNhapNhanVien";
        }

        // Xác định loại phòng (Online hoặc Offline)
        Room room = entityManager.find(Room.class, id);
        if (room == null) {
            return "redirect:/DanhSachLopHoc?error=notfound";
        }

        model.addAttribute("room", room);

        // Lấy danh sách ClassroomDetails liên kết với phòng học này
        List<ClassroomDetails> classroomDetails = entityManager.createQuery(
                        "FROM ClassroomDetails WHERE room = :room", ClassroomDetails.class)
                .setParameter("room", room)
                .getResultList();

        // Danh sách Teachers và Students
        List<Teachers> teachersInClass = new ArrayList<>();
        List<Students> studentsInClass = new ArrayList<>();

        // Phân loại thành viên thành Teachers và Students
        for (ClassroomDetails detail : classroomDetails) {
            Person member = detail.getMember();
            if (member instanceof Teachers) {
                teachersInClass.add((Teachers) member);
            } else if (member instanceof Students) {
                studentsInClass.add((Students) member);
            }
        }

        // Lấy danh sách tất cả giáo viên và học sinh trong hệ thống
        List<Teachers> allTeachers = entityManager.createQuery("FROM Teachers", Teachers.class).getResultList();
        List<Students> allStudents = entityManager.createQuery("FROM Students", Students.class).getResultList();

        // Đưa dữ liệu vào ModelMap để hiển thị trong View
        model.addAttribute("teachersInClass", teachersInClass);
        model.addAttribute("studentsInClass", studentsInClass);
        model.addAttribute("allTeachers", allTeachers);
        model.addAttribute("allStudents", allStudents);
        model.addAttribute("classroomDetails", classroomDetails);

        return "ChiTietLopHoc";
    }


    @Transactional
    @GetMapping("/XoaGiaoVienTrongLop")
    public String XoaGiaoVienTrongLop(@RequestParam String teacherId, @RequestParam String roomId, HttpSession session) {
        if (session.getAttribute("EmployeeID") == null) {
            return "redirect:/DangNhapNhanVien";
        }

        // Tìm đối tượng Room theo ID
        Room room = entityManager.find(Room.class, roomId);
        if (room == null) {
            return "redirect:/ChiTietLopHoc/" + roomId + "?error=RoomNotFound";
        }

        // Tìm đối tượng Teacher theo ID
        Person teacher = entityManager.find(Person.class, teacherId);
        if (teacher == null || !(teacher instanceof Teachers)) {
            return "redirect:/ChiTietLopHoc/" + roomId + "?error=TeacherNotFound";
        }

        try {
            int deletedCount = entityManager.createQuery(
                            "DELETE FROM ClassroomDetails WHERE room = :room AND member = :teacher")
                    .setParameter("room", room)  // Truyền object Room thay vì roomId
                    .setParameter("teacher", teacher)  // Truyền object Teacher thay vì teacherId
                    .executeUpdate();

            if (deletedCount == 0) {
                return "redirect:/ChiTietLopHoc/" + roomId + "?error=TeacherNotFound";
            }

            return "redirect:/ChiTietLopHoc/" + roomId + "?success=TeacherRemoved";
        } catch (Exception e) {
            return "redirect:/ChiTietLopHoc/" + roomId + "?error=DeleteFailed";
        }
    }


    @Transactional
    @GetMapping("/XoaHocSinhTrongLop")
    public String XoaHocSinhTrongLop(@RequestParam String studentId, @RequestParam String roomId, HttpSession session) {
        if (session.getAttribute("EmployeeID") == null) {
            return "redirect:/DangNhapNhanVien";
        }

        Room room = entityManager.find(Room.class, roomId);
        if (room == null) {
            return "redirect:/ChiTietLopHoc/" + roomId + "?error=RoomNotFound";
        }

        // Tìm đối tượng Student theo ID
        Person student = entityManager.find(Person.class, studentId);
        if (student == null || !(student instanceof Students)) {
            return "redirect:/ChiTietLopHoc/" + roomId + "?error=StudentNotFound";
        }

        try {
            int deletedCount = entityManager.createQuery(
                            "DELETE FROM ClassroomDetails WHERE room = :room AND member = :student")
                    .setParameter("room", room)  // Dùng object Room thay vì roomId
                    .setParameter("student", student)  // Dùng object Student thay vì studentId
                    .executeUpdate();

            if (deletedCount == 0) {
                return "redirect:/ChiTietLopHoc/" + roomId + "?error=StudentNotFound";
            }

            return "redirect:/ChiTietLopHoc/" + roomId + "?success=StudentRemoved";
        } catch (Exception e) {
            return "redirect:/ChiTietLopHoc/" + roomId + "?error=DeleteFailed";
        }
    }
}
