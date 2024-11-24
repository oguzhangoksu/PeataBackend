package peata.backend.entity;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Table(name="users")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name="username",nullable = false,unique = true)
    private String username;
    
    @Column(name="name",nullable = true)
    private String name;

    @Column(name="surname",nullable = true)
    private String surname;

    @Column(name="password",nullable = false)
    private String password;

    @Column(name="email",nullable = false,unique = true)
    @Email(message = "Email is not valid",regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")
    @NotEmpty(message = "Email cannot be empty")
    private String email;

    @Column(name="phone",nullable = true)
    private String phone;

    @Column(name="city",nullable = false)
    private String city;
    
    @Column(name="district",nullable = false)
    private String district;

    @Column(name="role",nullable = false)
    private String role;

    @Column(name="isAllowedNotification",nullable = false )
    private Boolean isAllowedNotification=false;

    @Column(name="emailValidation",nullable = true  )
    private Boolean emailValidation=false;

    @Column(name="favoriteAdds")
    private List<Long> favoriteAdds= new ArrayList<>();

    @JsonManagedReference
    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL,orphanRemoval = true)
    private Set<Add> ads = new HashSet<>();


    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", favoriteAddsCount=" + (favoriteAdds != null ? favoriteAdds.size() : 0) + // Avoids direct collection reference
                '}';
    }
}
