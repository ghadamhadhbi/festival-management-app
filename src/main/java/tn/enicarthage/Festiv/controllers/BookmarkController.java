package tn.enicarthage.Festiv.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.enicarthage.Festiv.entities.Bookmark;
import tn.enicarthage.Festiv.entities.Spectacle;
import tn.enicarthage.Festiv.repositories.BookmarkRepository;
import tn.enicarthage.Festiv.repositories.SpectacleRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookmarks")
@CrossOrigin(origins = "*")
public class BookmarkController {

    @Autowired
    private BookmarkRepository bookmarkRepository;
    
    @Autowired
    private SpectacleRepository spectacleRepository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Bookmark>> getUserBookmarks(@PathVariable Long userId) {
        List<Bookmark> bookmarks = bookmarkRepository.findByUserId(userId);
        return new ResponseEntity<>(bookmarks, HttpStatus.OK);
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkBookmark(
            @RequestParam Long userId, 
            @RequestParam Long spectacleId) {
        boolean exists = bookmarkRepository.existsByUserIdAndSpectacleIdSpec(userId, spectacleId);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Bookmark> addBookmark(@RequestBody Bookmark bookmark) {
        // Check if already exists
        if (bookmarkRepository.existsByUserIdAndSpectacleIdSpec(bookmark.getUserId(), bookmark.getSpectacle().getIdSpec())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        
        Bookmark savedBookmark = bookmarkRepository.save(bookmark);
        return new ResponseEntity<>(savedBookmark, HttpStatus.CREATED);
    }

    @PostMapping("/toggle")
    public ResponseEntity<?> toggleBookmark(@RequestParam Long userId, @RequestParam Long spectacleId) {
        Optional<Bookmark> existingBookmark = bookmarkRepository.findByUserIdAndSpectacleIdSpec(userId, spectacleId);
        
        if (existingBookmark.isPresent()) {
            // Remove bookmark
            bookmarkRepository.delete(existingBookmark.get());
            return new ResponseEntity<>(false, HttpStatus.OK);
        } else {
            // Add bookmark
            Optional<Spectacle> spectacleOpt = spectacleRepository.findById(spectacleId);
            if (!spectacleOpt.isPresent()) {
                return new ResponseEntity<>("Spectacle not found", HttpStatus.NOT_FOUND);
            }
            
            Bookmark newBookmark = Bookmark.builder()
                .userId(userId)
                .spectacle(spectacleOpt.get())
                .build();
                
            bookmarkRepository.save(newBookmark);
            return new ResponseEntity<>(true, HttpStatus.CREATED);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBookmark(@PathVariable Long id) {
        if (!bookmarkRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        bookmarkRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @DeleteMapping("/user/{userId}/spectacle/{spectacleId}")
    public ResponseEntity<Void> deleteBookmarkByUserAndSpectacle(
            @PathVariable Long userId, 
            @PathVariable Long spectacleId) {
        Optional<Bookmark> bookmark = bookmarkRepository.findByUserIdAndSpectacleIdSpec(userId, spectacleId);
        if (bookmark.isPresent()) {
            bookmarkRepository.delete(bookmark.get());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}