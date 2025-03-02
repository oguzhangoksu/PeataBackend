
package peata.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Table(name="districts")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString

public class District {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="district_name")
    private String district_name;

    @Column(name="cityId")
    private Long cityId;

    @Column(name="countryId")
    private Long countryId;

}
