package com.example.demo.ControllerPOST;

import com.example.demo.GoogleCalendarService.GoogleCalendarService;
import com.example.demo.ModelOOP.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/")
@Transactional
public class NhanVienPost {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private JavaMailSender mailSender; // Không khai báo lại ở nơi khác


    @PostMapping("/DangKyNhanVien")
    @Transactional(rollbackOn = Exception.class)
    public String DangKyNhanVien(
            @RequestParam("EmployeeID") String employeeID,
            @RequestParam("FirstName") String firstName,
            @RequestParam("LastName") String lastName,
            @RequestParam("Email") String email,
            @RequestParam("PhoneNumber") String phoneNumber,
            @RequestParam("Password") String password,
            @RequestParam("ConfirmPassword") String confirmPassword,
            @RequestParam("DateOfBirth") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
            @RequestParam("Gender") Gender gender,
            @RequestParam(value = "Country", required = false) String country,
            @RequestParam(value = "Province", required = false) String province,
            @RequestParam(value = "District", required = false) String district,
            @RequestParam(value = "Ward", required = false) String ward,
            @RequestParam(value = "Street", required = false) String street,
            @RequestParam(value = "PostalCode", required = false) String postalCode,
            Model model) {

        System.out.println("Start registering employee...");

        // Kiểm tra EmployeeID đã tồn tại chưa
        if (entityManager.find(Person.class, employeeID) != null) {
            model.addAttribute("employeeIDError", "Employee ID already exists.");
            addFormDataToModel(model, employeeID, firstName, lastName, email, phoneNumber, password, confirmPassword, dateOfBirth, gender, country, province, district, ward, street, postalCode);
            return "DangKyNhanVien";
        }

        // Kiểm tra Email có hợp lệ không
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            model.addAttribute("emailFormatError", "Invalid email.");
            addFormDataToModel(model, employeeID, firstName, lastName, email, phoneNumber, password, confirmPassword, dateOfBirth, gender, country, province, district, ward, street, postalCode);
            return "DangKyNhanVien";
        }

        // Kiểm tra Email đã tồn tại chưa
        List<Person> existingEmployeesByEmail = entityManager.createQuery(
                        "SELECT e FROM Person e WHERE e.email = :email", Person.class)
                .setParameter("email", email)
                .getResultList();
        if (!existingEmployeesByEmail.isEmpty()) {
            model.addAttribute("emailError", "This email is already used.");
            addFormDataToModel(model, employeeID, firstName, lastName, email, phoneNumber, password, confirmPassword, dateOfBirth, gender, country, province, district, ward, street, postalCode);
            return "DangKyNhanVien";
        }

        // Kiểm tra định dạng số điện thoại
        if (!phoneNumber.matches("^[0-9]+$")) {
            model.addAttribute("phoneError", "Phone number can only contain numbers!");
            addFormDataToModel(model, employeeID, firstName, lastName, email, phoneNumber, password, confirmPassword, dateOfBirth, gender, country, province, district, ward, street, postalCode);
            return "DangKyNhanVien";
        } else if (!phoneNumber.matches("^\\d{9,10}$")) {
            model.addAttribute("phoneError", "Phone number must be 9-10 digits!");
            addFormDataToModel(model, employeeID, firstName, lastName, email, phoneNumber, password, confirmPassword, dateOfBirth, gender, country, province, district, ward, street, postalCode);
            return "DangKyNhanVien";
        }

        // Kiểm tra số điện thoại đã tồn tại chưa
        List<Person> existingEmployeesByPhone = entityManager.createQuery(
                        "SELECT e FROM Person e WHERE e.phoneNumber = :phoneNumber", Person.class)
                .setParameter("phoneNumber", phoneNumber)
                .getResultList();
        if (!existingEmployeesByPhone.isEmpty()) {
            model.addAttribute("phoneDuplicateError", "This phone number is already used.");
            addFormDataToModel(model, employeeID, firstName, lastName, email, phoneNumber, password, confirmPassword, dateOfBirth, gender, country, province, district, ward, street, postalCode);
            return "DangKyNhanVien";
        }

        // Kiểm tra mật khẩu có khớp không
        if (!password.equals(confirmPassword)) {
            model.addAttribute("passwordError", "Password does not match.");
            addFormDataToModel(model, employeeID, firstName, lastName, email, phoneNumber, password, confirmPassword, dateOfBirth, gender, country, province, district, ward, street, postalCode);
            return "DangKyNhanVien";
        }

        // Kiểm tra độ mạnh của mật khẩu
        if (!isValidPassword(password)) {
            model.addAttribute("passwordStrengthError", "Password must be at least 8 characters, including uppercase, lowercase, numbers and special characters.");
            addFormDataToModel(model, employeeID, firstName, lastName, email, phoneNumber, password, confirmPassword, dateOfBirth, gender, country, province, district, ward, street, postalCode);
            return "DangKyNhanVien";
        }

        // Kiểm tra tuổi (phải >= 18 tuổi)
        LocalDate today = LocalDate.now();
        int age = Period.between(dateOfBirth, today).getYears();
        if (age < 18) {
            model.addAttribute("dobError", "You must be at least 18 years old to register.");
            addFormDataToModel(model, employeeID, firstName, lastName, email, phoneNumber, password, confirmPassword, dateOfBirth, gender, country, province, district, ward, street, postalCode);
            return "DangKyNhanVien";
        }

        // Kiểm tra ngày sinh có hợp lệ không (phải trong quá khứ)
        if (dateOfBirth.isAfter(LocalDate.now())) {
            model.addAttribute("dobError", "Invalid date of birth.");
            addFormDataToModel(model, employeeID, firstName, lastName, email, phoneNumber, password, confirmPassword, dateOfBirth, gender, country, province, district, ward, street, postalCode);
            return "DangKyNhanVien";
        }

        // Lấy Admin
        List<Admin> admins = entityManager.createQuery("FROM Admin", Admin.class).getResultList();
        if (admins.isEmpty()) {
            model.addAttribute("adminError", "Admin not found.");
            addFormDataToModel(model, employeeID, firstName, lastName, email, phoneNumber, password, confirmPassword, dateOfBirth, gender, country, province, district, ward, street, postalCode);
            return "DangKyNhanVien";
        }
        Admin admin = admins.get(0);

        // Tạo nhân viên mới
        Employees employees = new Employees();
        employees.setId(employeeID);
        employees.setFirstName(firstName);
        employees.setLastName(lastName);
        employees.setEmail(email);
        employees.setPassword(password); // Mã hóa mật khẩu
        employees.setPhoneNumber(phoneNumber);
        employees.setBirthDate(dateOfBirth);
        employees.setGender(gender);
        employees.setCountry(country);
        employees.setProvince(province);
        employees.setDistrict(district);
        employees.setWard(ward);
        employees.setStreet(street);
        employees.setPostalCode(postalCode);
        employees.setAdmin(admin);

        try {
            entityManager.persist(employees);
            System.out.println("Employee registered successfully!");
        } catch (Exception e) {
            System.out.println("Error saving employee: " + e.getMessage());
            model.addAttribute("databaseError", "Error saving data: " + e.getMessage());
            addFormDataToModel(model, employeeID, firstName, lastName, email, phoneNumber, password, confirmPassword, dateOfBirth, gender, country, province, district, ward, street, postalCode);
            return "DangKyNhanVien";
        }

        return "redirect:/TrangChu";
    }

    // Phương thức hỗ trợ để gửi dữ liệu vào model
    private void addFormDataToModel(Model model, String employeeID, String firstName, String lastName,
                                    String email, String phoneNumber, String password, String confirmPassword,
                                    LocalDate dateOfBirth, Gender gender, String country, String province,
                                    String district, String ward, String street, String postalCode) {
        model.addAttribute("EmployeeID", employeeID);
        model.addAttribute("FirstName", firstName);
        model.addAttribute("LastName", lastName);
        model.addAttribute("Email", email);
        model.addAttribute("PhoneNumber", phoneNumber);
        model.addAttribute("Password", password);
        model.addAttribute("ConfirmPassword", confirmPassword);
        model.addAttribute("DateOfBirth", dateOfBirth);
        model.addAttribute("Gender", gender);
        model.addAttribute("Country", country);
        model.addAttribute("Province", province);
        model.addAttribute("District", district);
        model.addAttribute("Ward", ward);
        model.addAttribute("Street", street);
        model.addAttribute("PostalCode", postalCode);
    }


    // Hàm kiểm tra độ mạnh của mật khẩu
    private boolean isValidPassword(String password) {
        return password.length() >= 8 &&
                Pattern.compile("[0-9]").matcher(password).find() &&
                Pattern.compile("[!@#$%^&*(),.?\":{}|<>]").matcher(password).find();
    }

    @PostMapping("/ThemGiaoVienCuaBan")
    @Transactional(rollbackOn = Exception.class)
    public String themGiaoVienCuaBan(
            @RequestParam("teacherID") String teacherID,
            @RequestParam("password") String password,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam(value = "misID", required = false) String misId,
            RedirectAttributes redirectAttributes) {

        // Lấy thông tin nhân viên từ Authenticationg
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String employeeId = authentication.getName();
        Person person = entityManager.find(Person.class, employeeId);
        if (!(person instanceof Employees employee)) {
            redirectAttributes.addFlashAttribute("error", "You do not have permission to add a teacher!");
            return "redirect:/ThemGiaoVienCuaBan";
        }

        // Lấy Admin
        Admin admin = entityManager.createQuery("SELECT a FROM Admin a", Admin.class)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);
        if (admin == null) {
            redirectAttributes.addFlashAttribute("errorAdmin", "Admin not found!");
            return "redirect:/ThemGiaoVienCuaBan";
        }

        // Kiểm tra TeacherID đã tồn tại chưa
        if (entityManager.find(Person.class, teacherID) != null) {
            redirectAttributes.addFlashAttribute("errorTeacherID", "Teacher ID already exists!");
            return "redirect:/ThemGiaoVienCuaBan";
        }

        // Kiểm tra Email hợp lệ
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            redirectAttributes.addFlashAttribute("errorEmail", "Invalid email!");
            return "redirect:/ThemGiaoVienCuaBan";
        }

        // Kiểm tra Email đã tồn tại chưa
        boolean emailExists = entityManager.createQuery(
                        "SELECT COUNT(t) > 0 FROM Person t WHERE t.email = :email", Boolean.class)
                .setParameter("email", email)
                .getSingleResult();
        if (emailExists) {
            redirectAttributes.addFlashAttribute("errorEmail", "This email is already used!");
            return "redirect:/ThemGiaoVienCuaBan";
        }

        // Kiểm tra định dạng số điện thoại
        if (!phoneNumber.matches("^[0-9]+$")) {
            redirectAttributes.addFlashAttribute("errorPhone", "Phone number can only contain numbers!");
            return "redirect:/ThemGiaoVienCuaBan";
        } else if (!phoneNumber.matches("^\\d{9,10}$")) {
            redirectAttributes.addFlashAttribute("errorPhone", "Phone number must be 9-10 digits!");
            return "redirect:/ThemGiaoVienCuaBan";
        }

        // Kiểm tra số điện thoại đã tồn tại chưa
        boolean phoneExists = entityManager.createQuery(
                        "SELECT COUNT(t) > 0 FROM Person t WHERE t.phoneNumber = :phoneNumber", Boolean.class)
                .setParameter("phoneNumber", phoneNumber)
                .getSingleResult();
        if (phoneExists) {
            redirectAttributes.addFlashAttribute("errorPhone", "This phone number is already used!");
            return "redirect:/ThemGiaoVienCuaBan";
        }

        // Kiểm tra mật khẩu
        if (!isValidPassword(password)) {
            redirectAttributes.addFlashAttribute("errorPassword", "Password must be at least 8 characters, including uppercase, lowercase, numbers and special characters!");
            return "redirect:/ThemGiaoVienCuaBan";
        }

        // Kiểm tra tên chỉ chứa chữ cái
        String formattedFirstName = formatName(firstName);
        String formattedLastName = formatName(lastName);
        if (formattedFirstName == null || formattedLastName == null) {
            redirectAttributes.addFlashAttribute("errorName", "Name can only contain letters!");
            return "redirect:/ThemGiaoVienCuaBan";
        }

        // Tạo đối tượng giáo viên
        Teachers teacher = new Teachers();
        teacher.setId(teacherID);
        teacher.setFirstName(formattedFirstName);
        teacher.setLastName(formattedLastName);
        teacher.setEmail(email);
        teacher.setPhoneNumber(phoneNumber); // Lưu số điện thoại đầy đủ với +84
        teacher.setMisID(misId);
        teacher.setPassword(password);
        teacher.setEmployee(employee);
        teacher.setAdmin(admin);

        // Lưu giáo viên vào database
        entityManager.persist(teacher);

        redirectAttributes.addFlashAttribute("successMessage", "Teacher added successfully!");
        return "redirect:/DanhSachGiaoVienCuaBan";
    }

    @PostMapping("/CapNhatSoSlot")
    public String capNhatSoSlot(@RequestParam("roomId") String roomId, @RequestParam("slotQuantity") Integer slotQuantity, RedirectAttributes redirectAttributes) {
        Room room = entityManager.find(Room.class, roomId);
        if (room != null) {
            room.setSlotQuantity(slotQuantity);
            entityManager.merge(room);
        }
        if (room.getSlotQuantity() == null || room.getSlotQuantity() <= 0) {
            redirectAttributes.addFlashAttribute("error", "Slot quantity cannot be null or less than 0");
            return "redirect:/BoTriLopHoc";
        }
        return "redirect:/BoTriLopHoc";
    }

    @PostMapping("/ThemHocSinhCuaBan")
    @Transactional(rollbackOn = Exception.class)
    public String ThemHocSinhCuaBan(
            @RequestParam("studentID") String studentID,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam(value = "misID", required = false) String misId,
            RedirectAttributes redirectAttributes) {

        // Lấy thông tin nhân viên từ Authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String employeeId = authentication.getName();
        Person person = entityManager.find(Person.class, employeeId);
        if (!(person instanceof Employees employee)) {
            redirectAttributes.addFlashAttribute("error", "You do not have permission to add a student!");
            return "redirect:/ThemHocSinhCuaBan";
        }

        // Lấy Admin
        Admin admin = entityManager.createQuery("SELECT a FROM Admin a", Admin.class)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);
        if (admin == null) {
            redirectAttributes.addFlashAttribute("errorAdmin", "Admin not found!");
            return "redirect:/ThemHocSinhCuaBan";
        }

        // Kiểm tra StudentID đã tồn tại chưa
        if (entityManager.find(Person.class, studentID) != null) {
            redirectAttributes.addFlashAttribute("errorStudentID", "Student ID already exists!");
            return "redirect:/ThemHocSinhCuaBan";
        }

        // Kiểm tra định dạng Email
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            redirectAttributes.addFlashAttribute("errorEmail", "Invalid email!");
            return "redirect:/ThemHocSinhCuaBan";
        }

        // Kiểm tra Email đã tồn tại chưa
        boolean emailExists = entityManager.createQuery(
                        "SELECT COUNT(s) > 0 FROM Person s WHERE s.email = :email", Boolean.class)
                .setParameter("email", email)
                .getSingleResult();
        if (emailExists) {
            redirectAttributes.addFlashAttribute("errorEmail", "This email is already used!");
            return "redirect:/ThemHocSinhCuaBan";
        }


        // Kiểm tra định dạng số điện thoại
        if (!phoneNumber.matches("^[0-9]+$")) {
            redirectAttributes.addFlashAttribute("errorPhone", "Phone number can only contain numbers!");
            return "redirect:/ThemHocSinhCuaBan";
        } else if (!phoneNumber.matches("^\\d{9,10}$")) {
            redirectAttributes.addFlashAttribute("errorPhone", "Phone number must be 9-10 digits!");
            return "redirect:/ThemHocSinhCuaBan";
        }

        // Kiểm tra số điện thoại đã tồn tại chưa
        boolean phoneExists = entityManager.createQuery(
                        "SELECT COUNT(s) > 0 FROM Person s WHERE s.phoneNumber = :phoneNumber", Boolean.class)
                .setParameter("phoneNumber", phoneNumber)
                .getSingleResult();
        if (phoneExists) {
            redirectAttributes.addFlashAttribute("errorPhone", "This phone number is already used!");
            return "redirect:/ThemHocSinhCuaBan";
        }

        // Kiểm tra mật khẩu
        if (!isValidPassword(password)) {
            redirectAttributes.addFlashAttribute("errorPassword", "Password must be at least 8 characters, including uppercase, lowercase, numbers and special characters!");
            return "redirect:/ThemHocSinhCuaBan";
        }

        // Chuẩn hóa họ tên
        String formattedFirstName = formatName(firstName);
        String formattedLastName = formatName(lastName);
        if (formattedFirstName == null || formattedLastName == null) {
            redirectAttributes.addFlashAttribute("errorName", "Name can only contain letters!");
            return "redirect:/ThemHocSinhCuaBan";
        }

        // Tạo mới student
        Students student = new Students();
        student.setId(studentID);
        student.setFirstName(formattedFirstName);
        student.setLastName(formattedLastName);
        student.setEmail(email);
        student.setPhoneNumber(phoneNumber); // Lưu số điện thoại đầy đủ với +84
        student.setPassword(password); // Mã hóa mật khẩu
        student.setMisId(misId);
        student.setEmployee(employee);
        student.setAdmin(admin);

        // Lưu vào database
        entityManager.persist(student);

        // Chuyển hướng với thông báo thành công
        redirectAttributes.addFlashAttribute("successMessage", "Student added successfully!");
        return "redirect:/DanhSachHocSinhCuaBan";
    }

    // Phương thức định dạng tên (chỉ giữ chữ cái)
    private String formatName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null; // Thay vì trả về "" để báo lỗi nếu tên rỗng
        }

        String[] words = name.trim().toLowerCase().split("\\s+");
        StringBuilder formattedName = new StringBuilder();

        for (String word : words) {
            if (!word.matches("[a-zA-ZÀ-Ỵà-ỵ]+")) { // Kiểm tra ký tự hợp lệ (bao gồm tiếng Việt)
                return null; // Tên không hợp lệ
            }
            formattedName.append(Character.toUpperCase(word.charAt(0))) // Viết hoa chữ cái đầu
                    .append(word.substring(1)) // Phần còn lại viết thường
                    .append(" ");
        }
        return formattedName.toString().trim();
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        String phoneRegex = "^\\+?[0-9]{10,15}$";
        return phoneNumber.matches(phoneRegex);
    }

    @PostMapping("/SuaGiaoVienCuaBan")
    public String SuaGiaoVienCuaBan(@RequestParam("teacherID") String teacherID,
                                    @RequestParam("firstName") String firstName,
                                    @RequestParam("lastName") String lastName,
                                    @RequestParam("email") String email,
                                    @RequestParam("phoneNumber") String phoneNumber,
                                    RedirectAttributes redirectAttributes) {

        // Kiểm tra định dạng email
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            redirectAttributes.addFlashAttribute("error", "Invalid email!");
            return "redirect:/SuaGiaoVienCuaBan/" + teacherID;  // Redirect back to the edit page with error message
        }

        // Kiểm tra định dạng số điện thoại (chỉ gồm 10-11 chữ số)
        if (!phoneNumber.matches("^\\d{10,11}$")) {
            redirectAttributes.addFlashAttribute("error", "Invalid phone number!");
            return "redirect:/SuaGiaoVienCuaBan/" + teacherID;  // Redirect back to the edit page with error message
        }

        // Kiểm tra định dạng tên (chỉ chứa chữ cái và khoảng trắng)
        if (!firstName.matches("^[A-Za-zÀ-ỹ\\s]+$") || !lastName.matches("^[A-Za-zÀ-ỹ\\s]+$")) {
            redirectAttributes.addFlashAttribute("error", "Name can only contain letters!");
            return "redirect:/SuaGiaoVienCuaBan/" + teacherID;  // Redirect back to the edit page with error message
        }

        // Định dạng tên
        firstName = formatName(firstName);
        lastName = formatName(lastName);

        // Tìm giáo viên theo ID
        Person teacher = entityManager.find(Person.class, teacherID);
        if (teacher == null) {
            redirectAttributes.addFlashAttribute("error", "Teacher not found!");
            return "redirect:/DanhSachGiaoVienCuaBan";  // Redirect to the list page if teacher not found
        }

        // Kiểm tra trùng email với giáo viên khác
        List<Person> teachersWithEmail = entityManager.createQuery(
                        "SELECT t FROM Person t WHERE t.email = :email AND t.id <> :teacherID", Person.class)
                .setParameter("email", email)
                .setParameter("teacherID", teacherID)
                .getResultList();

        if (!teachersWithEmail.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "This email is already used by another teacher!");
            return "redirect:/SuaGiaoVienCuaBan/" + teacherID;  // Redirect back to the edit page with error message
        }

        // Kiểm tra trùng số điện thoại với giáo viên khác
        List<Person> teachersWithPhone = entityManager.createQuery(
                        "SELECT t FROM Person t WHERE t.phoneNumber = :phoneNumber AND t.id <> :teacherID", Person.class)
                .setParameter("phoneNumber", phoneNumber)
                .setParameter("teacherID", teacherID)
                .getResultList();

        if (!teachersWithPhone.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "This phone number is already used by another teacher!");
            return "redirect:/SuaGiaoVienCuaBan/" + teacherID;  // Redirect back to the edit page with error message
        }

        // Cập nhật thông tin giáo viên
        teacher.setFirstName(firstName);
        teacher.setLastName(lastName);
        teacher.setEmail(email);
        teacher.setPhoneNumber(phoneNumber);

        entityManager.merge(teacher); // Lưu vào database

        redirectAttributes.addFlashAttribute("successMessage", "Teacher updated successfully!");
        return "redirect:/DanhSachGiaoVienCuaBan";  // Redirect to the list page after success
    }


    @PostMapping("/SuaHocSinhCuaBan")
    public String SuaHocSinhCuaBan(@RequestParam("studentID") String studentID,
                                   @RequestParam("firstName") String firstName,
                                   @RequestParam("lastName") String lastName,
                                   @RequestParam("email") String email,
                                   @RequestParam("phoneNumber") String phoneNumber,
                                   @RequestParam(value = "misId", required = false) String misId,
                                   RedirectAttributes redirectAttributes) {

        // Kiểm tra định dạng email
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            redirectAttributes.addFlashAttribute("error", "Invalid email!");
            return "redirect:/SuaHocSinhCuaBan/" + studentID;  // Redirect back to the edit page
        }

        // Kiểm tra định dạng số điện thoại
        if (!phoneNumber.matches("^\\d{10,11}$")) {
            redirectAttributes.addFlashAttribute("error", "Invalid phone number!");
            return "redirect:/SuaHocSinhCuaBan/" + studentID;  // Redirect back to the edit page
        }

        // Kiểm tra định dạng tên (chỉ chứa chữ cái và khoảng trắng)
        if (!firstName.matches("^[A-Za-zÀ-ỹ\\s]+$") || !lastName.matches("^[A-Za-zÀ-ỹ\\s]+$")) {
            redirectAttributes.addFlashAttribute("error", "Name can only contain letters!");
            return "redirect:/SuaHocSinhCuaBan/" + studentID;  // Redirect back to the edit page
        }

        // Định dạng tên
        firstName = formatName(firstName);
        lastName = formatName(lastName);

        // Tìm học sinh theo ID
        Students student = entityManager.find(Students.class, studentID);
        if (student == null) {
            redirectAttributes.addFlashAttribute("error", "Student not found!");
            return "redirect:/DanhSachHocSinhCuaBan";  // Redirect to the list page if student not found
        }

        // Kiểm tra trùng email với học sinh khác
        List<Person> studentsWithEmail = entityManager.createQuery(
                        "SELECT s FROM Person s WHERE s.email = :email AND s.id <> :studentID", Person.class)
                .setParameter("email", email)
                .setParameter("studentID", studentID)
                .getResultList();

        if (!studentsWithEmail.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "This email is already used by another student!");
            return "redirect:/SuaHocSinhCuaBan/" + studentID;  // Redirect back to the edit page
        }

        // Kiểm tra trùng số điện thoại với học sinh khác
        List<Person> studentsWithPhone = entityManager.createQuery(
                        "SELECT s FROM Person s WHERE s.phoneNumber = :phoneNumber AND s.id <> :studentID", Person.class)
                .setParameter("phoneNumber", phoneNumber)
                .setParameter("studentID", studentID)
                .getResultList();

        if (!studentsWithPhone.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "This phone number is already used by another student!");
            return "redirect:/SuaHocSinhCuaBan/" + studentID;  // Redirect back to the edit page
        }

        // Cập nhật thông tin học sinh
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setEmail(email);
        student.setPhoneNumber(phoneNumber);
        student.setMisId(misId);

        entityManager.merge(student); // Lưu vào database

        redirectAttributes.addFlashAttribute("successMessage", "Student updated successfully!");
        return "redirect:/DanhSachHocSinhCuaBan";  // Redirect to the list page after success
    }


    @PostMapping("/ThemPhongHoc")
    public String ThemPhongHoc(@RequestParam("roomId") String roomId,
                               @RequestParam("roomName") String roomName,

                               RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String employeeId = authentication.getName();
        Person person = entityManager.find(Person.class, employeeId);
        Employees employee = (Employees) person;
        // Kiểm tra trùng ID
        Rooms existingRoomById = entityManager.find(Rooms.class, roomId);
        if (existingRoomById != null) {
            redirectAttributes.addFlashAttribute("error", "Room ID already exists!");
            return "redirect:/ThemPhongHoc";
        }

        // Kiểm tra trùng tên phòng
        TypedQuery<Rooms> query = entityManager.createQuery(
                "SELECT r FROM Rooms r WHERE r.roomName = :roomName", Rooms.class);
        query.setParameter("roomName", roomName);
        List<Rooms> existingRoomsByName = query.getResultList();
        if (!existingRoomsByName.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Room name already exists!");
            return "redirect:/ThemPhongHoc";
        }

        // Thêm phòng học mới
        Rooms newRoom = new Rooms();
        newRoom.setRoomId(roomId);
        newRoom.setRoomName(roomName);
        newRoom.setEmployee(employee);
        newRoom.setCreatedAt(LocalDateTime.now());
        entityManager.persist(newRoom);

        redirectAttributes.addFlashAttribute("success", "Room added successfully!");
        return "redirect:/DanhSachPhongHoc";
    }

    @PostMapping("/ThemPhongHocOnline")
    @Transactional
    public String LuuPhongHocOnline(
            @RequestParam("roomId") String roomId,
            @RequestParam("roomName") String roomName,
            RedirectAttributes redirectAttributes) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String employeeId = authentication.getName();
        Person person = entityManager.find(Person.class, employeeId);
        if (!(person instanceof Employees employee)) {
            redirectAttributes.addFlashAttribute("error", "You do not have permission to add a room!");
            return "redirect:/ThemPhongHocOnline";
        }

        // Kiểm tra ID phòng
        OnlineRooms existingRoomById = entityManager.find(OnlineRooms.class, roomId);
        if (existingRoomById != null) {
            redirectAttributes.addFlashAttribute("error", "Room ID already exists!");
            return "redirect:/ThemPhongHocOnline";
        }

        // Kiểm tra tên phòng
        TypedQuery<OnlineRooms> query = entityManager.createQuery(
                "SELECT r FROM OnlineRooms r WHERE r.roomName = :roomName", OnlineRooms.class);
        query.setParameter("roomName", roomName);
        if (!query.getResultList().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Room name already exists!");
            return "redirect:/ThemPhongHocOnline";
        }

        // Tạo link Google Meet
        String meetLink;
        try {
            GoogleCalendarService calendarService = new GoogleCalendarService();
            meetLink = calendarService.createGoogleMeetLink(roomName, LocalDateTime.now());
        } catch (IOException | GeneralSecurityException e) {
            redirectAttributes.addFlashAttribute("error", "Error creating Google Meet link: " + e.getMessage());
            return "redirect:/ThemPhongHocOnline";
        }

        // Tạo và lưu phòng học
        OnlineRooms newRoom = new OnlineRooms();
        newRoom.setRoomId(roomId);
        newRoom.setRoomName(roomName);
        newRoom.setEmployee(employee);
        newRoom.setCreatedAt(LocalDateTime.now());
        newRoom.setLink(meetLink);

        entityManager.persist(newRoom);
        redirectAttributes.addFlashAttribute("success", "Add classroom successfully with link: " + meetLink);

        return "redirect:/DanhSachPhongHoc";
    }


    @PostMapping("/SuaPhongHocOffline")
    public String CapNhatPhongHoc(@RequestParam("roomId") String roomId,
                                  @RequestParam("roomName") String roomName) {
        // Lấy thông tin phòng từ database
        Rooms room = entityManager.find(Rooms.class, roomId);
        if (room == null) {
            return "redirect:/DanhSachPhongHoc?error=RoomNotFound";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String employeeId = authentication.getName();
        Person person = entityManager.find(Person.class, employeeId);
        Employees employee = (Employees) person;

        // Kiểm tra xem tên phòng mới đã tồn tại chưa
        TypedQuery<Rooms> query = entityManager.createQuery(
                "SELECT r FROM Rooms r WHERE r.roomName = :roomName AND r.roomId <> :roomId",
                Rooms.class
        );
        query.setParameter("roomName", roomName);
        query.setParameter("roomId", roomId);

        List<Rooms> existingRooms = query.getResultList();
        if (!existingRooms.isEmpty()) {
            return "redirect:/SuaPhongHocOffline?roomId=" + roomId + "&error=RoomNameExists";
        }

        // Cập nhật thông tin phòng học
        room.setRoomName(roomName);
        room.setEmployee(employee);
        entityManager.merge(room);  // Lưu thay đổi

        return "redirect:/DanhSachPhongHoc?success=RoomUpdated";
    }

    @PostMapping("/SuaPhongHocOnline")
    public String CapNhatPhongHocOnline(
            @RequestParam("roomId") String roomId,
            @RequestParam("roomName") String roomName) {

        // Kiểm tra phòng học có tồn tại không
        OnlineRooms room = entityManager.find(OnlineRooms.class, roomId);
        if (room == null) {
            return "redirect:/DanhSachPhongHoc?error=RoomNotFound";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String employeeId = authentication.getName();
        Person person = entityManager.find(Person.class, employeeId);
        Employees employee = (Employees) person;


        // Kiểm tra trùng tên phòng (trừ phòng hiện tại)
        TypedQuery<OnlineRooms> query = entityManager.createQuery(
                "SELECT r FROM OnlineRooms r WHERE r.roomName = :roomName AND r.roomId <> :roomId", OnlineRooms.class);
        query.setParameter("roomName", roomName);
        query.setParameter("roomId", roomId);

        List<OnlineRooms> duplicateRooms = query.getResultList();
        if (!duplicateRooms.isEmpty()) {
            return "redirect:/SuaPhongHocOnline?error=RoomNameExists&roomId=" + roomId + "&roomName=" + roomName;
        }

        // Cập nhật thông tin phòng học online
        room.setRoomName(roomName);
        room.setEmployee(employee);
        entityManager.merge(room);  // Lưu thay đổi

        return "redirect:/DanhSachPhongHoc?success=RoomUpdated";
    }

    @PostMapping("/ThemGiaoVienVaoLop")
    @Transactional
    public String ThemGiaoVienVaoLop(@RequestParam String roomId, @RequestParam(name = "teacherIds", required = false) List<String> teacherIds, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String employeeId = authentication.getName(); // EmployeeID đã đăng nhập

        // Tìm Employee trong database bằng EntityManager
        Employees employee = entityManager.find(Employees.class, employeeId);
        // Tìm Room theo roomId
        Room room = entityManager.find(Room.class, roomId);
        if (room == null) {
            throw new IllegalArgumentException("Room not found with ID: " + roomId);
        }

        if (teacherIds == null || teacherIds.isEmpty() || teacherIds.size() == 0) {
            redirectAttributes.addFlashAttribute("error", "Please select at least 1 teacher");
            return "redirect:/ChiTietLopHoc/" + roomId;
        }

        if (teacherIds.size() > 1) {
            redirectAttributes.addFlashAttribute("error", "Only one teacher can be added");
            return "redirect:/ChiTietLopHoc/" + roomId;
        }

        for (String teacherId : teacherIds) {
            // Tìm Teacher theo teacherId
            Person teacher = entityManager.find(Person.class, teacherId);
            if (teacher == null || !(teacher instanceof Teachers)) {
                throw new IllegalArgumentException("Teacher not found with ID: " + teacherId);
            }

            // Kiểm tra xem giáo viên đã có trong lớp chưa
            Long count = entityManager.createQuery(
                            "SELECT COUNT(cd) FROM ClassroomDetails cd WHERE cd.room = :room AND cd.member = :teacher",
                            Long.class)
                    .setParameter("room", room)
                    .setParameter("teacher", teacher)
                    .getSingleResult();

            if (count == 0) {
                // Thêm giáo viên vào lớp
                ClassroomDetails classroomDetail = new ClassroomDetails(room, teacher);
                Events event = entityManager.find(Events.class, 9); // Giả định event ID 9 là sự kiện mặc định
                classroomDetail.setEvent(event);
                entityManager.persist(classroomDetail);

                // Gửi email thông báo cho giáo viên
                String subject = "Welcome to the " + room.getRoomName() + " at xAI Education";

                String message = "<html><body style='font-family: Georgia, serif; line-height: 1.8; color: #333333; max-width: 700px; margin: 0 auto; background-color: #F9F9F9; padding: 20px;'>" +
                        "<p style='font-size: 18px; color: #1A3C5E;'><b>Dear " + teacher.getFullName() + ", esteemed guide,</b></p>" +
                        "<p style='color: #4A4A4A;'>First of all, we would like to extend our most sincere greetings to you, as the moonlit path of knowledge shines brightly. We are grateful for your presence in our educational journey, and today marks a special occasion when you officially join the <i style='color: #D35400;'>" + room.getRoomName() + "</i> at xAI Education, opening a new chapter of significance in your mission.</p>" +
                        "<p style='color: #4A4A4A;'>Your presence is not only a great honor for us, but also a flame that lights up the hearts of young talents, guiding the way for their dreams. This is the beginning of a journey where your talent, passion, and experience will become an endless source of inspiration, contributing to the vibrant and complete education landscape.</p>" +
                        "<p style='color: #4A4A4A;'>To help you better understand this new mission, we would like to share some information, as the words engraved on the memorial stone of a memorable journey:</p>" +
                        "<ul style='list-style-type: none; padding-left: 20px; color: #4A4A4A;'>" +
                        "   <li><b style='color: #2E7D32;'>✦ Classroom Code:</b> " + roomId + "</li>" +
                        "   <li><b style='color: #2E7D32;'>✦ Classroom Name:</b> " + room.getRoomName() + "</li>" +
                        "   <li><b style='color: #2E7D32;'>✦ Time of joining:</b> " + new java.util.Date() + "</li>" +
                        "   <li><b style='color: #2E7D32;'>✦ Message from us:</b> We warmly welcome you to the new knowledge house, where each lecture of you will be a brick building the future.</li>" +
                        "</ul>" +
                        "<p style='color: #4A4A4A;'>We understand that each new role comes with great expectations and responsibilities. Therefore, if you have any questions about this class, or simply want to exchange ideas to prepare for the journey ahead, please do not hesitate to contact us via <a href='mailto:vuthanhtruong1280@gmail.com' style='color: #2980B9; text-decoration: none; font-weight: bold;'>vuthanhtruong1280@gmail.com</a> or phone number <b style='color: #C0392B;'>0394444107</b>. Our team is always ready to accompany you, with all our dedication and respect.</p>" +
                        "<p style='color: #4A4A4A;'>Dear Teacher, the educational journey is a long symphony, and you are the conductor of the talented, leading the notes of knowledge to resonate in the hearts of the students. We believe that, with your dedication and enthusiasm, this class will become a fertile ground where knowledge seeds are planted and bloom brightly.</p>" +
                        "<p style='color: #4A4A4A;'>Before closing this letter, we send you our warmest wishes: May you always be healthy, full of inspiration, and find endless joy in your noble mission. You are not just a teacher, but a dream-lifting light, a guiding star for the generations.</p>" +
                        "<p style='margin-top: 30px; text-align: center; color: #7F8C8D;'><i>Respectfully,</i></p>" +
                        "<p style='text-align: center; color: #34495E;'>" +
                        "<b>" + employee.getFirstName() + " " + employee.getLastName() + "</b><br>" +
                        "System Administrator<br>" +
                        "Education Management System - xAI Education<br>" +
                        "Email: <a href='mailto:vuthanhtruong1280@gmail.com' style='color: #2980B9; text-decoration: none;'>support@xaiedu.com</a> | Hotline: <span style='color: #C0392B;'>0394444107</span></p>" +
                        "</body></html>";
                sendNotification(teacher.getId(), room.getRoomId(), "Automatically send notifications when adding users to class", employee, teacher.getEmail());
                sendEmail(teacher.getEmail(), subject, message);
            }
        }

        return "redirect:/ChiTietLopHoc/" + roomId + "?success=updated";
    }

    @PostMapping("/ThemHocSinhVaoLop")
    @Transactional
    public String ThemHocSinhVaoLop(@RequestParam String roomId, @RequestParam(name = "studentIds", required = false) List<String> studentIds, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String employeeId = authentication.getName(); // EmployeeID đã đăng nhập

        // Tìm Employee trong database bằng EntityManager
        Employees employee = entityManager.find(Employees.class, employeeId);
        // Tìm Room theo roomId

        if (studentIds == null || studentIds.isEmpty() || studentIds.size() == 0) {
            System.out.println("List of students received: " + studentIds);

            redirectAttributes.addFlashAttribute("error", "Please select 1 or more students");
            return "redirect:/ChiTietLopHoc/" + roomId;
        }


        Room room = entityManager.find(Room.class, roomId);
        if (room == null) {
            throw new IllegalArgumentException("Room not found with ID: " + roomId);
        }

        for (String studentId : studentIds) {
            // Tìm Student theo studentId
            Person student = entityManager.find(Person.class, studentId);
            if (student == null || !(student instanceof Students)) {
                throw new IllegalArgumentException("Student not found with ID: " + studentId);
            }

            // Kiểm tra xem học sinh đã có trong lớp chưa
            Long count = entityManager.createQuery(
                            "SELECT COUNT(cd) FROM ClassroomDetails cd WHERE cd.room = :room AND cd.member = :student",
                            Long.class)
                    .setParameter("room", room)
                    .setParameter("student", student)
                    .getSingleResult();

            if (count == 0) {
                // Thêm học sinh vào lớp
                ClassroomDetails classroomDetail = new ClassroomDetails(room, student);
                Events event = entityManager.find(Events.class, 10); // Giả định event ID 10 là sự kiện mặc định
                classroomDetail.setEvent(event);
                entityManager.persist(classroomDetail);

                // Gửi email thông báo cho học sinh
                String subject = "Welcome to the " + room.getRoomName() + " at xAI Education";

                String message = "<html><body style='font-family: Georgia, serif; line-height: 1.8; color: #333333; max-width: 700px; margin: 0 auto; background-color: #F5F6F5; padding: 20px;'>" +
                        "<p style='font-size: 18px; color: #154360;'><b>Dear " + student.getFullName() + ", a new companion worth celebrating,</b></p>" +
                        "<p style='color: #4A4A4A;'>First of all, we send you a warm greeting, like a spring breeze passing through blooming flowers, carrying joy and hope. Today is a special day, when you officially become a part of the <i style='color: #D35400;'>" + room.getRoomName() + "</i> at xAI Education, the Education Management System.</p>" +
                        "<p style='color: #4A4A4A;'>This is not just the beginning, but an open door leading you to new horizons of knowledge, where each step of yours will be marked by effort, passion, and bright dreams. We are extremely honored to welcome you, a precious piece, to join us in writing beautiful stories in this meaningful educational journey.</p>" +
                        "<p style='color: #4A4A4A;'>To help you better understand this new mission, we would like to share some information, as the words engraved on the memorial stone of a memorable journey:</p>" +
                        "<ul style='list-style-type: none; padding-left: 20px; color: #4A4A4A;'>" +
                        "   <li><b style='color: #2E7D32;'>✦ Classroom Code:</b> " + roomId + "</li>" +
                        "   <li><b style='color: #2E7D32;'>✦ Classroom Name:</b> " + room.getRoomName() + "</li>" +
                        "   <li><b style='color: #2E7D32;'>✦ Time of joining:</b> " + new java.util.Date() + "</li>" +
                        "   <li><b style='color: #2E7D32;'>✦ Message from us:</b> We warmly welcome you to the new knowledge house, where each lecture of you will be a brick building the future.</li>" +
                        "</ul>" +
                        "<p style='color: #4A4A4A;'>We believe that, with your talent, passion, and thirst for knowledge, this class will become a fertile ground where you plant knowledge seeds, waiting for the day to bloom into beautiful flowers. If you have any questions about the journey ahead, or simply want to chat to get to know each other, please do not hesitate to contact us via <a href='mailto:vuthanhtruong1280@gmail.com' style='color: #2980B9; text-decoration: none; font-weight: bold;'>vuthanhtruong1280@gmail.com</a> or phone number <b style='color: #C0392B;'>0394444107</b>. Our team is always ready to accompany you, with all our dedication and respect.</p>" +
                        "<p style='color: #4A4A4A;'>Dear Student, the educational journey is a long symphony, and you are the conductor of the talented, leading the notes of knowledge to resonate in the hearts of the students. We believe that, with your dedication and enthusiasm, this class will become a fertile ground where knowledge seeds are planted and bloom brightly.</p>" +
                        "<p style='color: #4A4A4A;'>Before closing this letter, we send you our warmest wishes: May you always be strong as the waves of the sea, radiant as the sunrise, and full of energy to write the wonderful story of youth. Welcome back, once again, to <i style='color: #D35400;'>" + room.getRoomName() + "</i>!</p>" +
                        "<p style='margin-top: 30px; text-align: center; color: #7F8C8D;'><i>Respectfully,</i></p>" +
                        "<p style='text-align: center; color: #34495E;'>" +
                        "<b>" + employee.getFirstName() + " " + employee.getLastName() + "</b><br>" +
                        "System Administrator<br>" +
                        "Education Management System - xAI Education<br>" +
                        "Email: <a href='mailto:vuthanhtruong1280@gmail.com' style='color: #2980B9; text-decoration: none;'>support@xaiedu.com</a> | Hotline: <span style='color: #C0392B;'>0394444107</span></p>" +
                        "</body></html>";
                sendNotification(student.getId(), room.getRoomId(), "Automatically send notifications when adding users to class", employee, student.getEmail());
                sendEmail(student.getEmail(), subject, message);
            }
        }

        return "redirect:/ChiTietLopHoc/" + roomId + "?success=updated";
    }

    @PostMapping("/CapNhatLichTrinh")
    @Transactional
    public String capNhatLichTrinh(
            @RequestParam("roomId") String roomId,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime) {

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            LocalDateTime thoiGianBatDau = LocalDateTime.parse(startTime, formatter);
            LocalDateTime thoiGianKetThuc = LocalDateTime.parse(endTime, formatter);

            Room room = entityManager.find(Room.class, roomId);
            if (room instanceof Rooms) {
                room.setStartTime(thoiGianBatDau);
                room.setEndTime(thoiGianKetThuc);
            } else if (room instanceof OnlineRooms) {
                room.setStartTime(thoiGianBatDau);
                room.setEndTime(thoiGianKetThuc);
            } else {
                return "redirect:/BoTriLopHoc?error=notfound";
            }

            entityManager.merge(room);
            return "redirect:/BoTriLopHoc?success=updated";

        } catch (DateTimeParseException e) {
            return "redirect:/BoTriLopHoc?error=invalid_datetime";
        }
    }

    @PostMapping("/CapNhatDiaChi")
    @Transactional
    public String capNhatDiaChi(
            @RequestParam("roomId") String roomId,
            @RequestParam("roomAddress") String diaChiMoi) {

        Rooms room = entityManager.find(Rooms.class, roomId);
        if (room != null) {
            room.setAddress(diaChiMoi);
            entityManager.merge(room);
            return "redirect:/BoTriLopHoc?success=updated";
        } else {
            return "redirect:/BoTriLopHoc?error=notfound";
        }
    }

    // Cập nhật link phòng học Online
    @PostMapping("/CapNhatLink")
    @Transactional
    public String capNhatLink(
            @RequestParam("roomId") String roomId,
            @RequestParam("roomLink") String linkMoi) {

        OnlineRooms room = entityManager.find(OnlineRooms.class, roomId);
        if (room != null) {
            room.setLink(linkMoi);
            entityManager.merge(room);
            return "redirect:/BoTriLopHoc?success=updated";
        } else {
            return "redirect:/BoTriLopHoc?error=notfound";
        }
    }

    @PostMapping("/DanhSachHocSinhCuaBan")
    public String DanhSachHocSinhCuaBan(@RequestParam("pageSize") int pageSize, HttpSession session) {
        session.setAttribute("pageSize", pageSize); // Đặt session với đúng key
        return "redirect:/DanhSachHocSinhCuaBan";
    }

    @PostMapping("/DanhSachGiaoVienCuaBan")
    public String DanhSachGiaoVienCuaBan(@RequestParam("pageSize") int pageSize, HttpSession session) {
        session.setAttribute("pageSize2", pageSize);
        return "redirect:/DanhSachGiaoVienCuaBan"; // Đúng trang cần quay lại
    }

    @PostMapping("/DanhSachPhongHoc")
    public String setPageSize(@RequestParam("pageSize") int pageSize, HttpSession session) {
        session.setAttribute("pageSize3", pageSize); // Thống nhất key với GET request
        return "redirect:/DanhSachPhongHoc";
    }

    @PostMapping("/DanhSachNguoiDungHeThong")
    public String DanhSachNguoiDungHeThong1(@RequestParam("pageSize") int pageSize, HttpSession session) {
        session.setAttribute("pageSize4", pageSize); // Thống nhất key với GET request
        return "redirect:/DanhSachNguoiDungHeThong";
    }

    @PostMapping("/TimKiemGiaoVienCuaBan")
    public String timKiemGiaoVienCuaBan(@RequestParam("searchType") String searchType,
                                        @RequestParam("keyword") String keyword,
                                        ModelMap model) {
        List<Teachers> searchResults;

        if (searchType.equalsIgnoreCase("name")) {
            searchResults = entityManager.createQuery(
                            "SELECT t FROM Teachers t " +
                                    "WHERE LOWER(t.firstName) LIKE LOWER(:keyword) " +
                                    "OR LOWER(t.lastName) LIKE LOWER(:keyword) " +
                                    "OR LOWER(CONCAT(t.firstName, ' ', t.lastName)) LIKE LOWER(:keyword)", Teachers.class)
                    .setParameter("keyword", "%" + keyword + "%")
                    .getResultList();
        } else if (searchType.equalsIgnoreCase("id")) {
            Teachers teacher = entityManager.find(Teachers.class, keyword);
            searchResults = (teacher != null) ? List.of(teacher) : List.of();
        } else {
            searchResults = List.of();
        }

        model.addAttribute("keyword", keyword);
        model.addAttribute("teachers", searchResults);
        return "DanhSachTimKiemGiaoVienCuaBan";
    }

    @PostMapping("/TimKiemHocSinhCuaBan")
    public String timKiemHocSinhCuaBan(@RequestParam("searchType") String searchType,
                                       @RequestParam("keyword") String keyword,
                                       ModelMap model) {
        List<Students> searchResults = List.of(); // Mặc định danh sách rỗng

        if (keyword != null && !keyword.trim().isEmpty()) {
            if ("id".equalsIgnoreCase(searchType)) {
                Students student = entityManager.find(Students.class, keyword);
                if (student != null) {
                    searchResults = List.of(student);
                }
            } else if ("name".equalsIgnoreCase(searchType)) {
                searchResults = entityManager.createQuery("""
                                SELECT s FROM Students s 
                                WHERE LOWER(s.firstName) LIKE :keyword 
                                   OR LOWER(s.lastName) LIKE :keyword
                                   OR LOWER(CONCAT(s.firstName, ' ', s.lastName)) LIKE :keyword
                                """, Students.class)
                        .setParameter("keyword", "%" + keyword.toLowerCase() + "%")
                        .getResultList();
            }
        }

        model.addAttribute("keyword", keyword);
        model.addAttribute("students", searchResults);
        return "DanhSachTimKiemHocSinhCuaBan";
    }

    @PostMapping("/DanhSachTimKiemPhongHoc")
    public String searchRooms(
            @RequestParam("searchType") String searchType,
            @RequestParam("keyword") String keyword,
            Model model) {

        List<Rooms> roomsOffline;
        List<OnlineRooms> roomsOnline;

        if ("id".equals(searchType)) {
            roomsOffline = entityManager.createQuery(
                            "FROM Rooms r WHERE r.roomId = :keyword", Rooms.class)
                    .setParameter("keyword", keyword)
                    .getResultList();

            roomsOnline = entityManager.createQuery(
                            "FROM OnlineRooms r WHERE r.roomId = :keyword", OnlineRooms.class)
                    .setParameter("keyword", keyword)
                    .getResultList();
        } else {
            roomsOffline = entityManager.createQuery(
                            "FROM Rooms r WHERE r.roomName LIKE :keyword", Rooms.class)
                    .setParameter("keyword", "%" + keyword + "%")
                    .getResultList();

            roomsOnline = entityManager.createQuery(
                            "FROM OnlineRooms r WHERE r.roomName LIKE :keyword", OnlineRooms.class)
                    .setParameter("keyword", "%" + keyword + "%")
                    .getResultList();
        }

        model.addAttribute("roomsOffline", roomsOffline);
        model.addAttribute("roomsOnline", roomsOnline);
        model.addAttribute("keyword", keyword);

        return "DanhSachTimKiemPhongHoc"; // Đảm bảo có file HTML này trong templates
    }

    public void sendEmail(String recipientEmail, String subject, String htmlMessage) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(htmlMessage, true); // true = nội dung là HTML
            helper.setFrom("vuthanhtruong1280@gmail.com");

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Sending email failed: " + e.getMessage());
        }
    }

    private void sendNotification(String memberId, String roomId, String message, Employees sender, String email) {
        // Tìm đối tượng Person từ memberId
        Person member = entityManager.find(Person.class, memberId);
        if (member == null) {
            throw new IllegalArgumentException("Member not found with ID: " + memberId);
        }

        // Tìm đối tượng Room từ roomId
        Room room = entityManager.find(Room.class, roomId);
        if (room == null) {
            throw new IllegalArgumentException("Room not found with ID: " + roomId);
        }

        // Tạo thông báo mới
        ScheduleNotifications scheduleNotifications = new ScheduleNotifications();
        scheduleNotifications.setMember(member);
        scheduleNotifications.setRoom(room);
        scheduleNotifications.setMessage(message);
        scheduleNotifications.setSender(sender);
        entityManager.persist(scheduleNotifications);
    }

}
