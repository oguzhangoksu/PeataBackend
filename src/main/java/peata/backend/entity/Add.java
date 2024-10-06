package peata.backend.entity;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Table(name="adds")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString

public class Add {


    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="animal_name",nullable = false)
    private String animal_name;
    
    @Column(name="age",nullable = false)
    private String age;

    @Column(name="breed",nullable = false)
    private String breed;

    @Column(name="type",nullable = false)
    private String type;

    @Column(name="gender",nullable = false)
    private String gender;

    @Column(name="description",nullable = false)
    private String description;

    @Column(name="images",nullable = false)
    private List<String> images;
    
    @Column(name="city",nullable = false)
    private String city;

    @Column(name="district",nullable = false)
    private String district;

    @Column(name="date",nullable = false)
    private Date date;

    @Column(name="add_type",nullable = false)
    private String add_type;

    @Column(name="status",nullable = false)
    private boolean status = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;


}