package com.example.demo.OOP;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "OnlineRooms")
@PrimaryKeyJoinColumn(name = "RoomID") // Liên kết với ID từ Room
@Getter
@Setter
@NoArgsConstructor
public class OnlineRooms extends Room {

    @Column(name = "Link", nullable = true, length = 500)
    private String link;


}
