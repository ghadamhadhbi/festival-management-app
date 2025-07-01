package tn.enicarthage.Festiv.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tn.enicarthage.Festiv.entities.Performance;
import tn.enicarthage.Festiv.entities.Spectacle;
import tn.enicarthage.Festiv.repositories.PerformanceRepository;
import tn.enicarthage.Festiv.repositories.SpectacleRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/spectacles")
@CrossOrigin(origins = "*")
public class SpectacleController {

    @Autowired
    private SpectacleRepository spectacleRepository;
    @Autowired
    private PerformanceRepository performanceRepository;

    @GetMapping
    public ResponseEntity<List<Spectacle>> getAllSpectacles() {
        List<Spectacle> spectacles = spectacleRepository.findAll();
        return new ResponseEntity<>(spectacles, HttpStatus.OK);
    }

    
    @GetMapping("/date-range")
    public ResponseEntity<List<Spectacle>> getSpectaclesByDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<Spectacle> spectacles = spectacleRepository.findDistinctByPerformances_DatePerformanceBetween(startDate, endDate);
        return new ResponseEntity<>(spectacles, HttpStatus.OK);
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<Spectacle> getSpectacleById(@PathVariable Long id) {
        Optional<Spectacle> spectacle = spectacleRepository.findById(id);
        return spectacle.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Spectacle>> searchSpectacles(@RequestParam String titre) {
        List<Spectacle> spectacles = spectacleRepository.findByTitreContainingIgnoreCase(titre);
        return new ResponseEntity<>(spectacles, HttpStatus.OK);
    }

    @GetMapping("/lieu/{lieuId}")
    public ResponseEntity<List<Spectacle>> getSpectaclesByLieu(@PathVariable Long lieuId) {
        List<Spectacle> spectacles = spectacleRepository.findByLieuId(lieuId);
        return new ResponseEntity<>(spectacles, HttpStatus.OK);
    }

    @GetMapping("/price-range")
    public ResponseEntity<List<Spectacle>> getSpectaclesByPriceRange(
            @RequestParam Double minPrice, 
            @RequestParam Double maxPrice) {
        List<Spectacle> spectacles = spectacleRepository.findByPriceRange(minPrice, maxPrice);
        return new ResponseEntity<>(spectacles, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Spectacle> createSpectacle(@RequestBody Spectacle spectacle) {
        Spectacle savedSpectacle = spectacleRepository.save(spectacle);
        return new ResponseEntity<>(savedSpectacle, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Spectacle> updateSpectacle(@PathVariable Long id, @RequestBody Spectacle spectacle) {
        if (!spectacleRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        spectacle.setIdSpec(id);
        Spectacle updatedSpectacle = spectacleRepository.save(spectacle);
        return new ResponseEntity<>(updatedSpectacle, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpectacle(@PathVariable Long id) {
        if (!spectacleRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        spectacleRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
