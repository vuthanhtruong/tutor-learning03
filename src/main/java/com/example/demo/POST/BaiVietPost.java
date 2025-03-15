package com.example.demo.POST;


import com.example.demo.OOP.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/")
@Transactional

public class BaiVietPost {
    private static final Logger log = LoggerFactory.getLogger(GiaoVienPost.class);

    @PersistenceContext
    private EntityManager entityManager;
    @Value("${file.upload-dir:C:/uploads}")
    private String uploadDir;

    @Transactional
    @PostMapping("/BaiPost")
    public String handlePost(
            @RequestParam("postContent") String postContent,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("roomId") String roomId,
            RedirectAttributes redirectAttributes) {

        Person person = null;
        try {
            log.info("🔍 Xử lý bài đăng. Nội dung: {}", postContent);

            // 📌 Xác định người dùng hiện tại
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            person = entityManager.find(Person.class, userId);

            if (!(person instanceof Students) && !(person instanceof Teachers)) {
                throw new SecurityException("🚫 Người dùng không hợp lệ.");
            }

            // 📝 Tạo bài đăng mới
            Posts newPost = new Posts();
            newPost.setContent(postContent);
            newPost.setCreator(person);
            newPost.setCreatedAt(LocalDateTime.now());

            // 🏫 Lấy phòng học
            Rooms room = entityManager.find(Rooms.class, roomId);
            if (room == null) {
                throw new IllegalArgumentException("Không tìm thấy phòng học với ID: " + roomId);
            }
            newPost.setRoom(room);

            // 📅 Gán sự kiện "Bài đăng mới"
            Events postEvent = entityManager.find(Events.class, 3);
            newPost.setEvent(postEvent);
            entityManager.persist(newPost);
            log.info("✅ Bài đăng đã được lưu với ID: {}", newPost.getPostId());

            // 📂 Xử lý tệp đính kèm (nếu có)
            if (file != null && !file.isEmpty()) {
                byte[] fileData = file.getBytes();
                log.info("📏 Kích thước tệp (bytes): {}", fileData.length);

                if (fileData.length == 0) {
                    throw new IOException("❌ Tệp rỗng hoặc không đọc được.");
                }

                Documents document = new Documents();
                document.setDocumentTitle(file.getOriginalFilename());
                document.setFileData(fileData);
                document.setFilePath(uploadDir + File.separator + file.getOriginalFilename());
                document.setCreator(person);
                document.setPost(newPost);

                // 📅 Gán sự kiện "Tệp tin đính kèm"
                Events fileEvent = entityManager.find(Events.class, 4);
                document.setEvent(fileEvent);

                entityManager.persist(document);
                log.info("✅ Document đã lưu với ID: {}", document.getDocumentId());
            }

            redirectAttributes.addFlashAttribute("message", "Bài đăng đã được tạo thành công!");

        } catch (IOException e) {
            log.error("❌ Lỗi khi xử lý tệp: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xử lý tệp: " + e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        } catch (Exception e) {
            log.error("🚫 Lỗi không mong muốn: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }

        // 🛠 Điều hướng phù hợp theo vai trò
        return "redirect:/ChiTietLopHocBanThamGia/" + roomId;
    }

}
