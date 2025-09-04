
package peata.backend.dtos;
import lombok.Data;
import peata.backend.entity.Binding;

@Data
public class BindingOwnedDto {
    private Long id;
    private Long ownerId;
    private Long requesterId;
    private boolean owned;
    private String imageUrl;

    public BindingOwnedDto(Binding binding, boolean owned, String imageUrl) {
        this.id = binding.getId();
        this.ownerId = binding.getOwnerId();
        this.requesterId = binding.getRequesterId();
        this.owned = owned;
        this.imageUrl = imageUrl;
    }

}