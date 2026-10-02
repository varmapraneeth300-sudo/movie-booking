package com.moviebooking.controller;

import com.moviebooking.dto.*;
import com.moviebooking.service.ScreenService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ScreenController {
    private final ScreenService screenService;

    ScreenController(ScreenService screenService) {
        this.screenService = screenService;
    }

    @PostMapping("/{theatreId}/screens")
    public ScreenResponse createScreen(
            @PathVariable Long theatreId,
            @Valid @RequestBody ScreenRequest request) {

        return screenService.createScreen(theatreId, request);
    }

    @GetMapping("/{theatreId}/screens")
    public List<ScreenResponse> getScreensByTheatre(
            @PathVariable Long theatreId) {

        return screenService.getScreensByTheatre(theatreId);
    }

    @GetMapping("/{screenId}")
    public ScreenResponse getScreenById(@PathVariable Long screenId) {
        return screenService.getScreenById(screenId);
    }

    @PutMapping("/{screenId}")
    public ScreenResponse updateScreen(
            @PathVariable Long screenId,
            @Valid @RequestBody ScreenRequest request) {

        return screenService.updateScreen(screenId, request);
    }

    @DeleteMapping("/{screenId}")
    public DeleteScreenResponse deleteScreen(
            @PathVariable Long screenId) {

        return screenService.deleteScreen(screenId);
    }

    @PatchMapping("/{screenId}/activate")
    public ScreenStatusResponse activateScreen(
            @PathVariable Long screenId) {

        return screenService.activateScreen(screenId);
    }

    @GetMapping("/screens/search/{searchKey}")
    public Page<ScreenResponse> searchScreens(
            @PathVariable String searchKey,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return screenService.getActiveScreensBySearchKey(
                searchKey,
                page,
                size
        );
    }
}
