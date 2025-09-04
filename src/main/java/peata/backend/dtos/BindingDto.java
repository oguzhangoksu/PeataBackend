package peata.backend.dtos;

import com.google.api.client.util.DateTime;

import lombok.Data;
import peata.backend.entity.Binding;

@Data
public class BindingDto {
    private Long id;
    private Long addId;
    private Long ownerId;
    private Long requesterId;
    private DateTime createdAt;
   
    public BindingDto convertFromEntity(Binding binding) {
        this.id = binding.getId();
        this.addId = binding.getAddId();
        this.ownerId = binding.getOwnerId();
        this.requesterId = binding.getRequesterId();
        this.createdAt = new DateTime(binding.getCreatedAt());
        return this;
    }
    
}
