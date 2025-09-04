package peata.backend.service.abstracts;

import java.util.List;

import peata.backend.entity.Suggestions;
import peata.backend.utils.Requests.SuggestionRequest;

public interface SuggestionsService {

    public Suggestions save(SuggestionRequest suggestionRequest);
    public Suggestions mapperToSuggestion(SuggestionRequest suggestionRequest);
    public Suggestions findById(Long id);
    public List<Suggestions> findAll();

}
