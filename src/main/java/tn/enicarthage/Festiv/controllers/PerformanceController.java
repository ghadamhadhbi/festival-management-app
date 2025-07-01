package tn.enicarthage.Festiv.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import tn.enicarthage.Festiv.entities.Lieu;
import tn.enicarthage.Festiv.entities.Performance;
import tn.enicarthage.Festiv.entities.Spectacle;
import tn.enicarthage.Festiv.entities.SpectaclePricesDTO;
import tn.enicarthage.Festiv.repositories.PerformanceRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/performances")
@CrossOrigin(origins = "*")
public class PerformanceController {
    @Autowired
    private PerformanceRepository performanceRepository;
    
    @GetMapping
    public ResponseEntity<List<Performance>> getAllPerformances() {
        List<Performance> performances = performanceRepository.findAll();
        return new ResponseEntity<>(performances, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Performance> getPerformanceById(@PathVariable Long id) {
        Optional<Performance> performance = performanceRepository.findById(id);
        return performance.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    @GetMapping("/spectacle/{spectacleId}")
    public ResponseEntity<List<Performance>> getPerformancesBySpectacle(@PathVariable Long spectacleId) {
        List<Performance> performances = performanceRepository.findBySpectacleIdSpec(spectacleId);
        return new ResponseEntity<>(performances, HttpStatus.OK);
    }
    
    
    @GetMapping("/{id}/with-spectacle")
    public ResponseEntity<Performance> getPerformanceWithSpectacle(@PathVariable Long id) {
        Optional<Performance> performanceOpt = performanceRepository.findById(id);
        
        if (performanceOpt.isPresent()) {
            Performance performance = performanceOpt.get();
            
            // Force initialization of the spectacle if it's lazily loaded
            // This triggers the fetch from the database
            if (performance.getSpectacle() != null) {
                performance.getSpectacle().getTitre(); // Force initialization
            }
            
            return new ResponseEntity<>(performance, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    
    @GetMapping("/lieu/{lieuId}")
    public ResponseEntity<List<Performance>> getPerformancesByLieu(@PathVariable Long lieuId) {
        List<Performance> performances = performanceRepository.findByLieuIdLieu(lieuId);
        return new ResponseEntity<>(performances, HttpStatus.OK);
    }
    
    

    @GetMapping("/{id}/prices")
    public ResponseEntity<SpectaclePricesDTO> getSpectaclePricesByPerformance(@PathVariable Long id) {
        Performance performance = performanceRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Performance not found"));

        Spectacle spectacle = performance.getSpectacle();

        SpectaclePricesDTO pricesDTO = new SpectaclePricesDTO(
            spectacle.getPrixGold(),
            spectacle.getPrixSilver(),
            spectacle.getPrixNormal()
        );

        return ResponseEntity.ok(pricesDTO);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<Performance>> getUpcomingPerformances() {
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        List<Performance> performances = performanceRepository.findUpcomingPerformances(currentDate, currentTime);
        return new ResponseEntity<>(performances, HttpStatus.OK);
    }
    
    @GetMapping("/upcoming/spectacle/{spectacleId}")
    public ResponseEntity<List<Performance>> getUpcomingPerformancesBySpectacle(@PathVariable Long spectacleId) {
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        List<Performance> performances = performanceRepository.findUpcomingPerformancesBySpectacle(
            spectacleId, currentDate, currentTime);
        return new ResponseEntity<>(performances, HttpStatus.OK);
    }
    /*
    @GetMapping("/upcoming/lieu/{lieuId}")
    public ResponseEntity<List<Performance>> getUpcomingPerformancesByLieu(@PathVariable Long spectacleId) {
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        List<Performance> performances = performanceRepository.findUpcomingPerformancesByLieu(
            lieuId, currentDate, currentTime);
        return new ResponseEntity<>(performances, HttpStatus.OK);
    }*/
    
    
    @PostMapping
    public ResponseEntity<Performance> createPerformance(@RequestBody Performance performance) {
        Performance savedPerformance = performanceRepository.save(performance);
        return new ResponseEntity<>(savedPerformance, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Performance> updatePerformance(@PathVariable Long id, @RequestBody Performance performance) {
        if (!performanceRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        performance.setIdPerformance(id);
        Performance updatedPerformance = performanceRepository.save(performance);
        return new ResponseEntity<>(updatedPerformance, HttpStatus.OK);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerformance(@PathVariable Long id) {
        if (!performanceRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        performanceRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    
    @GetMapping("/{performanceId}/lieu")
    public ResponseEntity<Lieu> getLieuByPerformanceId(@PathVariable Long performanceId) {
        Optional<Performance> performance = performanceRepository.findById(performanceId);
        
        if (performance.isPresent()) {
            Lieu lieu = performance.get().getLieu();
            return new ResponseEntity<>(lieu, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}