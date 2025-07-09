package peata.backend.entity;

import java.sql.Timestamp;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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

@Table(name = "bindings")
public class Binding {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name="add_id", nullable = false)
    private Long addId;

    @Column(name="owner_id", nullable = false)
    private Long ownerId;

    @Column(name="requester_id",nullable = false)
    private Long requesterId;

    @Column(name="created_at", nullable = false)
    @CreationTimestamp
    private Timestamp createdAt;

    @OneToMany(mappedBy = "binding")
    private List<ChatMessage> chatMessages;

}
