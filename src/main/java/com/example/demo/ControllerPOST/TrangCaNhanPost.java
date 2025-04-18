package com.example.demo.ControllerPOST;

import com.example.demo.ModelOOP.Gender;
import com.example.demo.ModelOOP.Person;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Controller
@RequestMapping("/")
public class TrangCaNhanPost {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @PostMapping("/LuuThongTinCaNhan")
    public String luuThongTinCaNhan(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String phoneNumber,
            @RequestParam(required = false) String birthDate,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) String street,
            @RequestParam(required = false) String postalCode,
            RedirectAttributes redirectAttributes) {

        // [1] Xác thực người dùng
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            redirectAttributes.addFlashAttribute("error", "You need to login to perform this action");
            return "redirect:/TrangChu";
        }

        // [2] Tìm thông tin người dùng
        String userId = authentication.getName();
        Person person = entityManager.find(Person.class, userId);
        if (person == null) {
            redirectAttributes.addFlashAttribute("error", "User information not found");
            return "redirect:/TrangCaNhan";
        }

        // [3] Validate dữ liệu cơ bản
        if (firstName.trim().isEmpty() || lastName.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "First and last name cannot be empty");
            return "redirect:/TrangCaNhan";
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            redirectAttributes.addFlashAttribute("error", "Invalid email");
            return "redirect:/TrangCaNhan";
        }
        if (!phoneNumber.matches("^\\+?[0-9]{9,15}$")) {
            redirectAttributes.addFlashAttribute("error", "Invalid phone number");
            return "redirect:/TrangCaNhan";
        }

        // [4] Kiểm tra trùng lặp email và số điện thoại
        long emailCount = (long) entityManager.createQuery("SELECT COUNT(p) FROM Person p WHERE p.email = :email AND p.id <> :userId")
                .setParameter("email", email)
                .setParameter("userId", userId)
                .getSingleResult();
        if (emailCount > 0) {
            redirectAttributes.addFlashAttribute("error", "This email is already used by another user");
            return "redirect:/TrangCaNhan";
        }

        long phoneCount = (long) entityManager.createQuery("SELECT COUNT(p) FROM Person p WHERE p.phoneNumber = :phoneNumber AND p.id <> :userId")
                .setParameter("phoneNumber", phoneNumber)
                .setParameter("userId", userId)
                .getSingleResult();
        if (phoneCount > 0) {
            redirectAttributes.addFlashAttribute("error", "This phone number is already used by another user");
            return "redirect:/TrangCaNhan";
        }

        // [5] Cập nhật thông tin chính
        person.setFirstName(firstName.trim());
        person.setLastName(lastName.trim());
        person.setEmail(email.trim());
        person.setPhoneNumber(phoneNumber.trim());

        // [6] Xử lý ngày sinh
        if (birthDate != null && !birthDate.trim().isEmpty()) {
            try {
                LocalDate parsedDate = LocalDate.parse(birthDate);
                if (parsedDate.isAfter(LocalDate.now())) {
                    redirectAttributes.addFlashAttribute("error", "Birth date cannot be a future date");
                    return "redirect:/TrangCaNhan";
                }
                person.setBirthDate(parsedDate);
            } catch (DateTimeParseException e) {
                redirectAttributes.addFlashAttribute("error", "Invalid birth date format");
                return "redirect:/TrangCaNhan";
            }
        }

        // [7] Xử lý giới tính
        if (gender != null && !gender.trim().isEmpty()) {
            try {
                person.setGender(Gender.valueOf(gender.toUpperCase()));
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("error", "Invalid gender");
                return "redirect:/TrangCaNhan";
            }
        }

        // [8] Cập nhật địa chỉ (CHỈ KHI CÓ GIÁ TRỊ)
        if (country != null && !country.isBlank()) person.setCountry(country);
        if (province != null && !province.isBlank()) person.setProvince(province);
        if (district != null && !district.isBlank()) person.setDistrict(district);
        if (ward != null && !ward.isBlank()) person.setWard(ward);
        if (street != null && !street.isBlank()) person.setStreet(street);
        if (postalCode != null && !postalCode.isBlank()) person.setPostalCode(postalCode);

        // [9] Lưu vào database
        try {
            entityManager.merge(person);
            entityManager.flush(); // Ensure data is written immediately
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error when saving information: " + e.getMessage());
            return "redirect:/TrangCaNhan";
        }

        redirectAttributes.addFlashAttribute("success", "Information updated successfully");
        return "redirect:/TrangCaNhan";
    }
}