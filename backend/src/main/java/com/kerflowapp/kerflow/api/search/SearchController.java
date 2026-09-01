package com.kerflowapp.kerflow.api.search;

import com.kerflowapp.kerflow.api.search.domain.SearchRequest;
import com.kerflowapp.kerflow.api.search.domain.SearchResponse;
import com.kerflowapp.kerflow.services.search.GooglePlacesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/search")
public class SearchController {

    private final GooglePlacesService googlePlacesService;

    @PostMapping("/places")
    public ResponseEntity<SearchResponse> searchPlaces(@RequestBody @Valid SearchRequest request) {
        SearchResponse response = googlePlacesService.search(request);
        return ResponseEntity.ok(response);
    }
    
}
