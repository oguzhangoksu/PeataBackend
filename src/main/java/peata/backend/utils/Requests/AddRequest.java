package peata.backend.utils.Requests;


import lombok.Data;

@Data
public class AddRequest {
    private String animal_name;
    private String age;
    private String breed;
    private String type;
    private String gender;
    private String description;
    private String city;
    private String district;
    private String add_type;
    private Long user_id;

    @Override
    public String toString() {
        return "AddRequest{" +
                "animal_name='" + animal_name + '\'' +
                ", age='" + age + '\'' +
                ", breed='" + breed + '\'' +
                ", type='" + type + '\'' +
                ", gender='" + gender + '\'' +
                ", description='" + description + '\'' +
                ", city='" + city + '\'' +
                ", district='" + district + '\'' +
                ", add_type='" + add_type + '\'' +
                ", user_id=" + user_id +
                '}';
    }

}
