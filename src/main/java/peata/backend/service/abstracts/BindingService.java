package peata.backend.service.abstracts;

import peata.backend.entity.Binding;

public interface BindingService {
    public Long findById(Long bindingId);
    public Binding save(Binding binding);
    public Binding findByIdOrCreate(Long bindingId, Long addId, Long requesterId);
}
