package com.example.demo.ControllerFace;

import com.example.demo.ModelOOP.Person;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FaceLoginController {

    @Autowired
    private FaceRecognitionService faceRecognitionService;

    @PersistenceContext
    private EntityManager entityManager;

    @PostMapping("/DangKyKhuonMat")
    @Transactional
    public String dangKyKhuonMat(@RequestParam("faceData") String faceData, Model model, RedirectAttributes redirectAttributes) {
        if (faceData == null || faceData.isEmpty()) {
            redirectAttributes.addFlashAttribute("faceDataInvalid", "Ảnh khuôn mặt không hợp lệ");
            return "redirect:/TrangCaNhan";
        }
        System.out.println("Received face data length: " + faceData.length());

        String personId = faceRecognitionService.findPersonIdByFace(faceData);
        if (personId != null) {
            redirectAttributes.addFlashAttribute("faceAlreadyRegistered", "Khuôn mặt đã được đăng ký trước đó");
            return "redirect:/TrangCaNhan";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            redirectAttributes.addFlashAttribute("notLoggedIn", "Bạn cần đăng nhập để thực hiện thao tác này");
            return "redirect:/TrangChu";
        }

        String userId = authentication.getName();
        Person person = entityManager.find(Person.class, userId);
        if (person == null) {
            redirectAttributes.addFlashAttribute("userNotFound", "Không tìm thấy thông tin người dùng");
            return "redirect:/TrangCaNhan";
        }

        person.setFaceData(faceData);
        try {
            entityManager.merge(person);
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("faceSaveFailed", "Lưu dữ liệu khuôn mặt thất bại");
            return "redirect:/TrangCaNhan";
        }

        redirectAttributes.addFlashAttribute("faceRegisterSuccess", "Đăng ký khuôn mặt thành công");
        return "redirect:/TrangCaNhan";
    }

    @GetMapping("/XoaKhuonMat")
    public String showXoaKhuonMat(Model model, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        Person person = entityManager.find(Person.class, userId);

        redirectAttributes.addFlashAttribute("user", person);
        return "XoaKhuonMat"; // Trả về trang XoaKhuonMat.html
    }

    @PostMapping("/XoaKhuonMat")
    @Transactional
    public String xoaKhuonMat(@RequestParam("faceData") String faceData, Model model, RedirectAttributes redirectAttributes) {
        if (faceData == null || faceData.isEmpty()) {
            redirectAttributes.addFlashAttribute("faceDataInvalid", "Invalid face photo");
            return "redirect:/XoaKhuonMat";
        }
        System.out.println("Received face data length for deletion: " + faceData.length());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            redirectAttributes.addFlashAttribute("notLoggedIn", "You need to login to perform this action.");
            return "redirect:/TrangChu";
        }

        String userId = authentication.getName();
        Person person = entityManager.find(Person.class, userId);
        if (person == null) {
            redirectAttributes.addFlashAttribute("userNotFound", "User information not found");
            return "redirect:/XoaKhuonMat";
        }

        String currentFaceData = person.getFaceData();
        if (currentFaceData == null || currentFaceData.isEmpty()) {
            redirectAttributes.addFlashAttribute("noFaceToDelete", "No facial data to delete");
            return "redirect:/XoaKhuonMat";
        }

        String matchedPersonId = faceRecognitionService.findPersonIdByFace(faceData);
        if (!userId.equals(matchedPersonId)) {
            redirectAttributes.addFlashAttribute("faceNotMatched", "Face does not match current data");
            return "redirect:/XoaKhuonMat";
        }

        person.setFaceData(null);
        try {
            entityManager.merge(person);
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("faceDeleteFailed", "Face data deletion failed");
            return "redirect:/XoaKhuonMat";
        }

        redirectAttributes.addFlashAttribute("faceDeleteSuccess", "Face removal successful");
        return "redirect:/TrangCaNhan";
    }
}