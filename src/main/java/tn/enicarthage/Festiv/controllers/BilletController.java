package tn.enicarthage.Festiv.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.enicarthage.Festiv.entities.Billet;
import tn.enicarthage.Festiv.repositories.BilletRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/billets")
@CrossOrigin(origins = "*")
public class BilletController {

    @Autowired
    private BilletRepository billetRepository;

    @GetMapping
    public ResponseEntity<List<Billet>> getAllBillets() {
        List<Billet> billets = billetRepository.findAll();
        return new ResponseEntity<>(billets, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Billet> getBilletById(@PathVariable Long id) {
        Optional<Billet> billet = billetRepository.findById(id);
        return billet.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Billet> getBilletByCode(@PathVariable String code) {
        Optional<Billet> billet = billetRepository.findByCodeBillet(code);
        return billet.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/spectacle/{spectacleId}")
    public ResponseEntity<List<Billet>> getBilletsBySpectacle(@PathVariable Long spectacleId) {
        List<Billet> billets = billetRepository.findBySpectacleIdSpec(spectacleId);
        return new ResponseEntity<>(billets, HttpStatus.OK);
    }

    @GetMapping("/performance/{performanceId}")
    public ResponseEntity<List<Billet>> getBilletsByPerformance(@PathVariable Long performanceId) {
        List<Billet> billets = billetRepository.findByPerformanceIdPerformance(performanceId);
        return new ResponseEntity<>(billets, HttpStatus.OK);
    }

    @GetMapping("/categorie/{categorie}")
    public ResponseEntity<List<Billet>> getBilletsByCategorie(@PathVariable String categorie) {
        List<Billet> billets = billetRepository.findByCategorie(categorie);
        return new ResponseEntity<>(billets, HttpStatus.OK);
    }

    @GetMapping("/vendu/{status}")
    public ResponseEntity<List<Billet>> getBilletsByStatus(@PathVariable String status) {
        List<Billet> billets = billetRepository.findByVendu(status);
        return new ResponseEntity<>(billets, HttpStatus.OK);
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<List<Billet>> getBilletsByReservation(@PathVariable Long reservationId) {
        List<Billet> billets = billetRepository.findByReservationIdReservation(reservationId);
        return new ResponseEntity<>(billets, HttpStatus.OK);
    }

    @GetMapping("/availability/{performanceId}/{categorie}")
    public ResponseEntity<Long> getAvailabilityCount(@PathVariable Long performanceId, @PathVariable String categorie) {
        Long count = billetRepository.countAvailableByPerformanceAndCategorie(performanceId, categorie);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Billet> createBillet(@RequestBody Billet billet) {
        Billet savedBillet = billetRepository.save(billet);
        return new ResponseEntity<>(savedBillet, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Billet> updateBillet(@PathVariable Long id, @RequestBody Billet billet) {
        if (!billetRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        billet.setIdBillet(id);
        Billet updatedBillet = billetRepository.save(billet);
        return new ResponseEntity<>(updatedBillet, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBillet(@PathVariable Long id) {
        if (!billetRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        billetRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
