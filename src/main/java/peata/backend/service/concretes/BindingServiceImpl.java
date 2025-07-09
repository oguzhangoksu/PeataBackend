package peata.backend.service.concretes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import peata.backend.entity.Binding;
import peata.backend.repositories.BindingRepository;
import peata.backend.service.abstracts.AddService;
import peata.backend.service.abstracts.BindingService;

@Service
public class BindingServiceImpl implements BindingService{

    @Autowired
    private BindingRepository bindingRepository;
    
    @Autowired
    private AddService addService;

    public Long findById(Long bindingId) {
        Binding binding = bindingRepository.findById(bindingId)
            .orElseThrow(() -> new RuntimeException("Binding not found with id: " + bindingId));
        return binding.getId();
    }
    public Binding save(Binding binding) {
        return bindingRepository.save(binding);
    }
    public Binding findByIdOrCreate(Long bindingId, Long addId, Long requesterId) {
        Long ownerId = addService.findAddById(requesterId).getUser().getId();
        return bindingRepository.findById(bindingId)
            .orElseGet(() -> {
                Binding newBinding = new Binding();
                newBinding.setId(bindingId);
                newBinding.setAddId(addId);
                newBinding.setOwnerId(ownerId);
                newBinding.setRequesterId(requesterId);
                return bindingRepository.save(newBinding);
            });
    }
}
