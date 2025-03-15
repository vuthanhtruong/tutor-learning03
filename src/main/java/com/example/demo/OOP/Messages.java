package com.example.demo.OOP;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "Messages")
@Getter
@Setter
public class Messages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MessagesID")
    private Integer messagesID;

    @ManyToOne
    @JoinColumn(name = "MessageSenderID", nullable = false, foreignKey = @ForeignKey(name = "FK_Messages_Sender"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Person sender;  // Người gửi tin nhắn

    @ManyToOne
    @JoinColumn(name = "MessageRecipientID", nullable = false, foreignKey = @ForeignKey(name = "FK_Messages_Recipient"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Person recipient;  // Người nhận tin nhắn

    @ManyToOne
    @JoinColumn(name = "EventID", nullable = false, foreignKey = @ForeignKey(name = "FK_Messages_Event"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Events event;  // Sự kiện liên quan đến tin nhắn

    @Column(name = "Datetime", nullable = false)
    private LocalDateTime datetime;

    @Column(name = "Text", nullable = false, columnDefinition = "TEXT")
    private String text;

    // Constructors
    public Messages() {
    }

    public Messages(Person sender, Person recipient, Events event, LocalDateTime datetime, String text) {
        this.sender = sender;
        this.recipient = recipient;
        this.event = event;
        this.datetime = datetime;
        this.text = text;
    }
}