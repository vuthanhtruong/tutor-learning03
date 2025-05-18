package com.example.demo.ControllerPOST;

import com.example.demo.ModelOOP.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
@RequestMapping("/")
@Transactional
public class StudentPost {
    private static final Logger log = LoggerFactory.getLogger(GiaoVienPost.class);
    @PersistenceContext
    private EntityManager entityManager;

    @PostMapping("/DangKyHocSinh")
    @Transactional(rollbackOn = Exception.class)
    public String dangKyHocSinh(
            @RequestParam("StudentID") String studentID,
            @RequestParam("FirstName") String firstName,
            @RequestParam("LastName") String lastName,
            @RequestParam("Email") String email,
            @RequestParam("PhoneNumber") String phoneNumber,
            @RequestParam("BirthDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate,
            @RequestParam(value = "MisId", required = false) String misId,
            @RequestParam("Password") String password,
            @RequestParam("ConfirmPassword") String confirmPassword,
            @RequestParam("Gender") Gender gender,
            @RequestParam(value = "Country", required = false) String country,
            @RequestParam(value = "Province", required = false) String province,
            @RequestParam(value = "District", required = false) String district,
            @RequestParam(value = "Ward", required = false) String ward,
            @RequestParam(value = "Street", required = false) String street,
            @RequestParam(value = "PostalCode", required = false) String postalCode,
            Model model) {

        System.out.println("Bắt đầu đăng ký học sinh...");

        // Kiểm tra mật khẩu khớp nhau
        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorPassword", "Re-enter password does not match!");
            addFormDataToModel(model, studentID, firstName, lastName, email, phoneNumber, birthDate, misId, gender, country, province, district, ward, street, postalCode);
            return "DangKyHocSinh";
        }

        // Kiểm tra StudentID đã tồn tại chưa
        if (entityManager.find(Person.class, studentID) != null) {
            model.addAttribute("errorStudentID", "Student code already exists!");
            addFormDataToModel(model, studentID, firstName, lastName, email, phoneNumber, birthDate, misId, gender, country, province, district, ward, street, postalCode);
            return "DangKyHocSinh";
        }

        // Kiểm tra Email có tồn tại chưa
        boolean emailExists = !entityManager.createQuery(
                        "SELECT s FROM Person s WHERE s.email = :email", Person.class)
                .setParameter("email", email)
                .getResultList().isEmpty();
        if (emailExists) {
            model.addAttribute("errorEmail", "Email đã tồn tại!");
            addFormDataToModel(model, studentID, firstName, lastName, email, phoneNumber, birthDate, misId, gender, country, province, district, ward, street, postalCode);
            return "DangKyHocSinh";
        }

        // Kiểm tra định dạng số điện thoại
        if (!phoneNumber.matches("^[0-9]+$")) {
            model.addAttribute("errorPhone", "Phone numbers must contain only numbers!");
            addFormDataToModel(model, studentID, firstName, lastName, email, phoneNumber, birthDate, misId, gender, country, province, district, ward, street, postalCode);
            return "DangKyHocSinh";
        } else if (!phoneNumber.matches("^\\d{9,10}$")) {
            model.addAttribute("errorPhone", "Phone number must be 9-10 digits!");
            addFormDataToModel(model, studentID, firstName, lastName, email, phoneNumber, birthDate, misId, gender, country, province, district, ward, street, postalCode);
            return "DangKyHocSinh";
        }

        // Kiểm tra số điện thoại có tồn tại chưa
        boolean phoneExists = !entityManager.createQuery(
                        "SELECT s FROM Person s WHERE s.phoneNumber = :phoneNumber", Person.class)
                .setParameter("phoneNumber", phoneNumber)
                .getResultList().isEmpty();
        if (phoneExists) {
            model.addAttribute("errorPhone", "Phone number is already registered!");
            addFormDataToModel(model, studentID, firstName, lastName, email, phoneNumber, birthDate, misId, gender, country, province, district, ward, street, postalCode);
            return "DangKyHocSinh";
        }

        // Kiểm tra mật khẩu có đủ mạnh không
        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$")) {
            model.addAttribute("errorPassword", "Password must be at least 8 characters, including uppercase, lowercase, numbers and special characters!");
            addFormDataToModel(model, studentID, firstName, lastName, email, phoneNumber, birthDate, misId, gender, country, province, district, ward, street, postalCode);
            return "DangKyHocSinh";
        }

        // Kiểm tra ngày sinh có hợp lệ không (phải trong quá khứ)
        LocalDate today = LocalDate.now();
        if (birthDate.isAfter(today)) {
            model.addAttribute("errorBirthDate", "Invalid date of birth!");
            addFormDataToModel(model, studentID, firstName, lastName, email, phoneNumber, birthDate, misId, gender, country, province, district, ward, street, postalCode);
            return "DangKyHocSinh";
        }

        // Kiểm tra độ tuổi hợp lệ (ít nhất 6 tuổi)
        if (ChronoUnit.YEARS.between(birthDate, today) < 6) {
            model.addAttribute("errorBirthDate", "Students must be 6 years of age or older!");
            addFormDataToModel(model, studentID, firstName, lastName, email, phoneNumber, birthDate, misId, gender, country, province, district, ward, street, postalCode);
            return "DangKyHocSinh";
        }

        // Lấy Admin
        List<Admin> adminList = entityManager.createQuery("FROM Admin", Admin.class).getResultList();
        if (adminList.isEmpty()) {
            model.addAttribute("errorAdmin", "Không tìm thấy Admin!");
            addFormDataToModel(model, studentID, firstName, lastName, email, phoneNumber, birthDate, misId, gender, country, province, district, ward, street, postalCode);
            return "DangKyHocSinh";
        }
        Admin admin = adminList.get(0);

        // Tìm nhân viên có ít học sinh nhất
        Employees selectedEmployee = findEmployeeWithFewestStudents();
        if (selectedEmployee == null) {
            model.addAttribute("errorEmployee", "No staff found to allocate!");
            addFormDataToModel(model, studentID, firstName, lastName, email, phoneNumber, birthDate, misId, gender, country, province, district, ward, street, postalCode);
            return "DangKyHocSinh";
        }

        // Tạo đối tượng học sinh
        Students student = new Students();
        student.setId(studentID);
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setEmail(email);
        student.setPhoneNumber(phoneNumber);
        student.setBirthDate(birthDate);
        student.setMisId(misId);
        student.setPassword(password); // Mã hóa mật khẩu
        student.setGender(gender);
        student.setCountry(country);
        student.setProvince(province);
        student.setDistrict(district);
        student.setWard(ward);
        student.setStreet(street);
        student.setPostalCode(postalCode);
        student.setEmployee(selectedEmployee);
        student.setAdmin(admin);

        // Lưu vào database
        try {
            entityManager.persist(student);
            System.out.println("Student registration successful! Assign to employee: " + selectedEmployee.getId());
        } catch (Exception e) {
            System.out.println("Lỗi khi lưu học sinh: " + e.getMessage());
            model.addAttribute("error", "Error saving data: " + e.getMessage());
            addFormDataToModel(model, studentID, firstName, lastName, email, phoneNumber, birthDate, misId, gender, country, province, district, ward, street, postalCode);
            return "DangKyHocSinh";
        }

        model.addAttribute("successMessage", "Registration successful! Please login.");
        return "redirect:/TrangChu";
    }

    // Phương thức hỗ trợ để gửi dữ liệu vào model
    private void addFormDataToModel(Model model, String studentID, String firstName, String lastName,
                                    String email, String phoneNumber, LocalDate birthDate, String misId, Gender gender,
                                    String country, String province, String district, String ward, String street, String postalCode) {
        model.addAttribute("StudentID", studentID);
        model.addAttribute("FirstName", firstName);
        model.addAttribute("LastName", lastName);
        model.addAttribute("Email", email);
        model.addAttribute("PhoneNumber", phoneNumber);
        model.addAttribute("BirthDate", birthDate);
        model.addAttribute("MisId", misId);
        model.addAttribute("Gender", gender);
        model.addAttribute("Country", country);
        model.addAttribute("Province", province);
        model.addAttribute("District", district);
        model.addAttribute("Ward", ward);
        model.addAttribute("Street", street);
        model.addAttribute("PostalCode", postalCode);
    }

    // Phương thức phụ để tìm nhân viên có ít học sinh nhất
    private Employees findEmployeeWithFewestStudents() {
        // Lấy tất cả nhân viên
        List<Employees> employeesList = entityManager.createQuery("FROM Employees", Employees.class).getResultList();
        if (employeesList.isEmpty()) {
            return null;
        }

        Employees selectedEmployee = null;
        Long minStudentCount = Long.MAX_VALUE;

        // Duyệt qua từng nhân viên và đếm số học sinh họ quản lý
        for (Employees employee : employeesList) {
            Long studentCount = entityManager.createQuery(
                            "SELECT COUNT(s) FROM Students s WHERE s.employee = :employee", Long.class)
                    .setParameter("employee", employee)
                    .getSingleResult();

            if (studentCount < minStudentCount) {
                minStudentCount = studentCount;
                selectedEmployee = employee;
            }
        }

        return selectedEmployee;
    }

    @PostMapping("/GuiNhanXetGiaoVien")
    @Transactional
    public String guiNhanXetGiaoVien(@RequestParam("teacherId") String teacherId,
                                     @RequestParam("roomId") String roomId,
                                     @RequestParam("text") String text,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        try {
            log.info("📝 Nhận xét giáo viên với ID: {}, phòng học: {}", teacherId, roomId);

            // Lấy ID học sinh từ phiên đăng nhập
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String studentId = authentication.getName();

            // Kiểm tra nếu sinh viên chưa đăng nhập
            if (studentId == null || studentId.isEmpty()) {
                throw new IllegalArgumentException("Bạn chưa đăng nhập!");
            }

            // Tìm sinh viên trong database
            Students student = entityManager.find(Students.class, studentId);
            if (student == null) {
                throw new IllegalArgumentException("Không tìm thấy học sinh với ID: " + studentId);
            }

            // Tìm giáo viên
            Teachers teacher = entityManager.find(Teachers.class, teacherId);
            if (teacher == null) {
                throw new IllegalArgumentException("Không tìm thấy giáo viên với ID: " + teacherId);
            }

            // Tìm phòng học
            Room room = entityManager.find(Room.class, roomId);
            if (room == null) {
                throw new IllegalArgumentException("Không tìm thấy phòng học với ID: " + roomId);
            }

            // Lấy nhân viên nhận phản hồi (nếu có)
            Employees receiver = student.getEmployee(); // Hoặc entityManager.find(Employees.class, someId);

            // Tạo nhận xét mới
            Feedbacks feedback = new Feedbacks();
            feedback.setReviewer(student);
            feedback.setTeacher(teacher);
            feedback.setReceiver(receiver);
            feedback.setText(text.trim());
            feedback.setRoom(room);
            feedback.setCreatedAt(LocalDateTime.now());

            // Xác định sự kiện phản hồi
            Events events = entityManager.find(Events.class, 3);
            feedback.setEvent(events);

            // Lưu nhận xét vào database
            entityManager.persist(feedback);
            log.info("✅ Nhận xét đã được lưu thành công.");

            // Gửi thông báo về giao diện
            redirectAttributes.addFlashAttribute("message", "Your comment has been submitted!");

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Lỗi kiểm tra dữ liệu: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

        } catch (Exception e) {
            log.error("❌ Lỗi khi gửi nhận xét: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "An error occurred, please try again.");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return "redirect:/TrangChuHocSinh";
    }
}
