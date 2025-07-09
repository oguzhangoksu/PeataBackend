package peata.backend.entity;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString

@Table(
    name = "chat_message",
    indexes = {
        @Index(name = "idx_binding_id", columnList = "binding_id, sent_at")
    }
    )
public class ChatMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="sender_id", nullable = false)
    private Long senderId;

    @Column(name="content",nullable = false)
    private String content;

    @Column(name="sent_at", nullable = false)
    @CreationTimestamp
    private Timestamp sentAt;

    @Column(name="is_read", nullable = false)
    private boolean isRead = false;

    @ManyToOne
    @JoinColumn(name="binding_id", nullable = false)
    private Binding binding; 

}
