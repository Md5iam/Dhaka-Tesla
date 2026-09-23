package bd.edu.seu.dhakatesla.controller;

import bd.edu.seu.dhakatesla.dto.ApiResponse;
import bd.edu.seu.dhakatesla.model.Zone;
import bd.edu.seu.dhakatesla.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/zones")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ZoneController {

    private final ZoneRepository zoneRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Zone>>> getAllZones() {
        return ResponseEntity.ok(ApiResponse.ok(zoneRepository.findAll()));
    }
}
