package tn.enicarthage.Festiv.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.enicarthage.Festiv.entities.Lieu;
import tn.enicarthage.Festiv.repositories.LieuRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/lieux")
@CrossOrigin(origins = "*")
public class LieuController {

    @Autowired
    private LieuRepository lieuRepository;

    @GetMapping
    public ResponseEntity<List<Lieu>> getAllLieux() {
        List<Lieu> lieux = lieuRepository.findAll();
        return new ResponseEntity<>(lieux, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lieu> getLieuById(@PathVariable Long id) {
        Optional<Lieu> lieu = lieuRepository.findById(id);
        return lieu.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Lieu>> searchLieux(@RequestParam String nom) {
        List<Lieu> lieux = lieuRepository.findByNomLieuContainingIgnoreCase(nom);
        return new ResponseEntity<>(lieux, HttpStatus.OK);
    }

    @GetMapping("/ville/{ville}")
    public ResponseEntity<List<Lieu>> getLieuxByVille(@PathVariable String ville) {
        List<Lieu> lieux = lieuRepository.findByVilleContainingIgnoreCase(ville);
        return new ResponseEntity<>(lieux, HttpStatus.OK);
    }

    @GetMapping("/capacite/{capacite}")
    public ResponseEntity<List<Lieu>> getLieuxByCapacite(@PathVariable Integer capacite) {
        List<Lieu> lieux = lieuRepository.findByCapaciteGreaterThanEqual(capacite);
        return new ResponseEntity<>(lieux, HttpStatus.OK);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Lieu>> getActiveLieux() {
        List<Lieu> lieux = lieuRepository.findByIsDeletedIsNull();
        return new ResponseEntity<>(lieux, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Lieu> createLieu(@RequestBody Lieu lieu) {
        Lieu savedLieu = lieuRepository.save(lieu);
        return new ResponseEntity<>(savedLieu, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Lieu> updateLieu(@PathVariable Long id, @RequestBody Lieu lieu) {
        if (!lieuRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        lieu.setIdLieu(id);
        Lieu updatedLieu = lieuRepository.save(lieu);
        return new ResponseEntity<>(updatedLieu, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLieu(@PathVariable Long id) {
        Optional<Lieu> lieuOpt = lieuRepository.findById(id);
        if (lieuOpt.isPresent()) {
            Lieu lieu = lieuOpt.get();
            lieu.setIsDeleted('Y');
            lieuRepository.save(lieu);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
