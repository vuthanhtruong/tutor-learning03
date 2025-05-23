package com.example.demo.ControllerPOST;

import com.example.demo.ModelOOP.Comments;
import com.example.demo.ModelOOP.Events;
import com.example.demo.ModelOOP.Person;
import com.example.demo.ModelOOP.Posts;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/")
@Transactional

public class CommmentPost {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @PostMapping("/BinhLuan")
    public String themBinhLuan(@RequestParam("postId") Long postId,
                               @RequestParam("commentText") String commentText,
                               HttpSession session) {
        // Lấy thông tin người dùng hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        Person commenter = entityManager.find(Person.class, userId);

        // Kiểm tra nếu không tìm thấy người dùng
        if (commenter == null) {
            throw new IllegalArgumentException("User is invalid or does not exist.");
        }

        // Lấy thông tin bài đăng
        Posts post = entityManager.find(Posts.class, postId);
        if (post == null) {
            throw new IllegalArgumentException("No posts found with ID: " + postId);
        }

        // Kiểm tra nội dung bình luận có hợp lệ không
        if (commentText == null || commentText.trim().isEmpty()) {
            return "redirect:/error?message=Bình luận không được để trống!";
        }

        // Tạo mới bình luận
        Comments comment = new Comments(commenter, post, commentText);

        // Liên kết với sự kiện nếu cần
        Events event = entityManager.find(Events.class, 6);
        comment.setEvent(event);

        // Lưu vào database
        entityManager.persist(comment);

        return "redirect:/ChiTietLopHocBanThamGia/" + post.getRoom().getRoomId();
    }
}
