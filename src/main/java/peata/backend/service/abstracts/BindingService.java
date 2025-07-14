package peata.backend.service.abstracts;

import peata.backend.entity.Binding;

public interface BindingService {
    public Binding findById(Long bindingId);
    public Binding save(Binding binding);
    public Binding findByIdOrCreate(Long addId, Long requesterId);
}
