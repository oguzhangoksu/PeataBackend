package peata.backend.utils.Requests;


import lombok.Data;

@Data
public class UpdateAddInfoRequest {

    private Long id;
    private String animal_name;
    private String age;
    private String breed;
    private String gender;
    private String type;
    private String add_type;
    private String description;
    private String city;
    private String district;
    private String phone;
    private String email;
    private Long user_id;

    
}

