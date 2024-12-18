package peata.backend.service.concretes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import peata.backend.entity.Suggestions;
import peata.backend.repositories.SuggestionsRepository;
import peata.backend.service.abstracts.SuggestionsService;
import peata.backend.utils.Requests.SuggestionRequest;

@Service
public class SuggestionSerivceImpl implements SuggestionsService{
    
    @Autowired
    private SuggestionsRepository suggestionsRepository;


    public Suggestions save(SuggestionRequest suggestionRequest){
        return suggestionsRepository.save(mapperToSuggestion(suggestionRequest));
    }


    public Suggestions mapperToSuggestion(SuggestionRequest suggestionRequest) {
        if (suggestionRequest == null) {
            return null;
        }
    
        Suggestions suggestion = new Suggestions();
        suggestion.setEmail(suggestionRequest.getEmail());
        suggestion.setSuggestion(suggestionRequest.getSuggestion()); 
    
        return suggestion;
    }
    
}
