package tn.star.Pfe.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.star.Pfe.dto.pole.PoleResponse;
import tn.star.Pfe.dto.pole.PoleRequest;
import tn.star.Pfe.service.PoleService;

import java.util.List;

@RestController
@RequestMapping("/api/poles")
@RequiredArgsConstructor
public class PoleController {

    private final PoleService poleService;

    @GetMapping
    public ResponseEntity<List<PoleResponse>> lister(){
        return ResponseEntity.ok(poleService.listerTous());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PoleResponse> creer(@Valid @RequestBody PoleRequest request){
        return ResponseEntity.status(201).body(poleService.creer(request));
    }
}
