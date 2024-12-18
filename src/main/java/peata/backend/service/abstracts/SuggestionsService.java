package peata.backend.service.abstracts;

import peata.backend.entity.Suggestions;
import peata.backend.utils.Requests.SuggestionRequest;

public interface SuggestionsService {

    public Suggestions save(SuggestionRequest suggestionRequest);
    public Suggestions mapperToSuggestion(SuggestionRequest suggestionRequest);

}
