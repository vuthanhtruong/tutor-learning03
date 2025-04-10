package com.example.demo.ControllerPOST;

import com.example.demo.ModelOOP.*;
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
import java.util.List;

@Controller
@RequestMapping("/")
@Transactional
public class BaiVietPost {

    private static final Logger logger = LoggerFactory.getLogger(BaiVietPost.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${upload.dir:/var/www/uploads}") // Giá trị mặc định cho production
    private String uploadDir;

    @PostMapping("/BaiPost")
    public String handlePost(
            @RequestParam("postContent") String postContent,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam("roomId") String roomId,
            RedirectAttributes redirectAttributes) {

        Person person = null;
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            person = entityManager.find(Person.class, userId);

            if (!(person instanceof Students) && !(person instanceof Teachers)) {
                throw new SecurityException("Người dùng không hợp lệ.");
            }

            Posts newPost = new Posts();
            newPost.setContent(postContent);
            newPost.setCreator(person);
            newPost.setCreatedAt(LocalDateTime.now());

            Room room = entityManager.find(Room.class, roomId);
            if (room == null) {
                throw new IllegalArgumentException("Không tìm thấy phòng học với ID: " + roomId);
            }
            newPost.setRoom(room);

            Events postEvent = entityManager.find(Events.class, 3);
            newPost.setEvent(postEvent);
            entityManager.persist(newPost);

            if (files != null && !files.isEmpty()) {
                File dir = new File(uploadDir);
                if (!dir.exists()) {
                    boolean created = dir.mkdirs();
                    if (!created) {
                        throw new IOException("Không thể tạo thư mục upload: " + uploadDir);
                    }
                }
                if (!dir.canWrite()) {
                    throw new IOException("Không có quyền ghi vào thư mục: " + uploadDir);
                }

                for (MultipartFile file : files) {
                    if (file != null && !file.isEmpty()) {
                        byte[] fileData = file.getBytes();
                        if (fileData.length == 0) {
                            throw new IOException("Tệp rỗng hoặc không đọc được: " + file.getOriginalFilename());
                        }

                        String filePath = uploadDir + File.separator + System.currentTimeMillis() + "_"
                                + file.getOriginalFilename();
                        File destFile = new File(filePath);
                        file.transferTo(destFile);
                        logger.info("Đã upload file: " + filePath);

                        Documents document = new Documents();
                        document.setDocumentTitle(file.getOriginalFilename());
                        document.setFileData(fileData);
                        document.setFilePath(filePath);
                        document.setCreator(person);
                        document.setPost(newPost);

                        Events fileEvent = entityManager.find(Events.class, 4);
                        document.setEvent(fileEvent);

                        entityManager.persist(document);
                    }
                }
            }

            redirectAttributes.addFlashAttribute("message", "Bài đăng đã được tạo thành công!");

        } catch (IOException e) {
            logger.error("Lỗi khi xử lý tệp: ", e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xử lý tệp: " + e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        } catch (Exception e) {
            logger.error("Lỗi hệ thống: ", e);
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }

        return "redirect:/ChiTietLopHocBanThamGia/" + roomId;
    }

    @PostMapping("/UpdateBaiPost")
    public String updatePost(
            @RequestParam("postId") Long postId,
            @RequestParam("postContent") String postContent,
            @RequestParam("roomId") String roomId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            RedirectAttributes redirectAttributes) {

        try {
            Posts existingPost = entityManager.find(Posts.class, postId);
            if (existingPost == null) {
                throw new IllegalArgumentException("Không tìm thấy bài đăng với ID: " + postId);
            }

            existingPost.setContent(postContent);
            entityManager.merge(existingPost);

            List<Documents> existingDocuments = entityManager.createQuery(
                            "SELECT d FROM Documents d WHERE d.post.postId = :postId", Documents.class)
                    .setParameter("postId", postId)
                    .getResultList();

            for (Documents doc : existingDocuments) {
                File oldFile = new File(doc.getFilePath());
                if (oldFile.exists()) {
                    oldFile.delete();
                }
                entityManager.remove(doc);
            }
            entityManager.flush();

            if (file != null && !file.isEmpty()) {
                File dir = new File(uploadDir);
                if (!dir.exists()) {
                    boolean created = dir.mkdirs();
                    if (!created) {
                        throw new IOException("Không thể tạo thư mục upload: " + uploadDir);
                    }
                }
                if (!dir.canWrite()) {
                    throw new IOException("Không có quyền ghi vào thư mục: " + uploadDir);
                }

                byte[] fileData = file.getBytes();
                if (fileData.length == 0) {
                    throw new IOException("Tệp rỗng hoặc không đọc được: " + file.getOriginalFilename());
                }

                String filePath = uploadDir + File.separator + System.currentTimeMillis() + "_"
                        + file.getOriginalFilename();
                File destFile = new File(filePath);
                file.transferTo(destFile);
                logger.info("Đã upload file mới: " + filePath);

                Documents newDocument = new Documents();
                newDocument.setDocumentTitle(file.getOriginalFilename());
                newDocument.setFileData(fileData);
                newDocument.setFilePath(filePath);
                newDocument.setCreator(existingPost.getCreator());
                newDocument.setPost(existingPost);

                Events fileEvent = entityManager.find(Events.class, 4);
                newDocument.setEvent(fileEvent);

                entityManager.persist(newDocument);
            }

            redirectAttributes.addFlashAttribute("message", "Bài đăng đã được cập nhật thành công!");

        } catch (IOException e) {
            logger.error("Lỗi khi xử lý tệp: ", e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xử lý tệp: " + e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        } catch (Exception e) {
            logger.error("Lỗi hệ thống: ", e);
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }

        return "redirect:/BaiDangCaNhan/" + roomId;
    }
}