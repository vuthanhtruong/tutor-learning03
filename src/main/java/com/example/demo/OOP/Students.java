package com.example.demo.OOP;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "Students")
@PrimaryKeyJoinColumn(name = "ID") // Liên kết với khóa chính của Person
@Getter
@Setter
public class Students extends Person {

    @Column(name = "Password", nullable = false, length = 255)
    private String password;

    @Column(name = "MIS_ID", length = 50)
    private String misId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EmployeeID", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Employees employee;  // Liên kết với Employee (có thể NULL)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AdminID", nullable = true) // Có thể NULL nếu không có Admin trực tiếp quản lý
    private Admin admin;

    // Constructor có tham số
    public Students(String id, String password, String firstName, String lastName, String email, String phoneNumber, String misId, Employees employee, Admin admin) {
        super(); // Gọi constructor của Person
        this.setId(id);
        this.setFirstName(firstName);
        this.setLastName(lastName);
        this.setEmail(email);
        this.setPhoneNumber(phoneNumber);
        this.password = password;
        this.misId = misId;
        this.employee = employee;
        this.admin = admin;
    }

    // Constructor không tham số (cần thiết cho JPA)
    public Students() {
    }
}
