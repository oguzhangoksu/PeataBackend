package peata.backend.service.concretes;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import peata.backend.dtos.BindingOwnedDto;
import peata.backend.entity.Add;
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


    public Binding findById(Long bindingId) {
        Binding binding = bindingRepository.findById(bindingId)
            .orElseThrow(() -> new RuntimeException("Binding not found with id: " + bindingId));
        return binding;
    }
    public Binding save(Binding binding) {
        return bindingRepository.save(binding);
    }

    public Binding findByIdOrCreate(Long addId, Long requesterId) {
        Add add = addService.findAddById(addId);
        Long ownerId = add.getUser().getId();
        return bindingRepository.findByAddIdAndOwnerIdAndRequesterId(addId, ownerId, requesterId)
            .orElseGet(() -> {
                Binding newBinding = new Binding();
                newBinding.setAddId(addId);
                newBinding.setOwnerId(ownerId);
                newBinding.setRequesterId(requesterId);
                return bindingRepository.save(newBinding);
            });
        
    }
    public List<BindingOwnedDto> findAllBinding(Long userId) {
        List<Binding> ownerChats = bindingRepository.findAllByOwnerId(userId);
        List<BindingOwnedDto> ownerChatDtos = ownerChats.stream()
                    .map(binding -> {
                        Add add = addService.findAddById(binding.getAddId());
                        if(add.isActive()){
                            String imageUrl= addService.firstImageUrl(add.getId());
                            return new BindingOwnedDto(binding, true,imageUrl);
                        }
                        else{
                            return null;
                        }
                    })
                    .collect(Collectors.toList());
        List<Binding> requesterChats = bindingRepository.findAllByRequesterId(userId);
        List<BindingOwnedDto> requesterChatDtos = requesterChats.stream()
                    .map(binding -> {
                        Add add = addService.findAddById(binding.getAddId());
                        if(add.isActive()){
                            String imageUrl= addService.firstImageUrl(add.getId());
                            return new BindingOwnedDto(binding, false,imageUrl);
                        }
                        else{
                            return null;
                        }
                    })
                    .collect(Collectors.toList());
        ownerChatDtos.addAll(requesterChatDtos);
        ownerChatDtos.removeIf(bindingOwnedDto -> bindingOwnedDto == null);
        return ownerChatDtos;
    }
}
