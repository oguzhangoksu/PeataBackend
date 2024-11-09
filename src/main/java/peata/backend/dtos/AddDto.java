package peata.backend.dtos;

import java.util.List;
import java.util.Date;
import lombok.Data;

@Data
public class AddDto {
    private Long id;
    private String animal_name;
    private String pCode;
    private String age;
    private String breed;
    private String type;
    private String gender;
    private String description;
    private List<String> images;
    private String city;
    private String district;
    private Date date;
    private String add_type;
    private int status;
    private String phone;
    private String email;
    private Long user_id;
    



}