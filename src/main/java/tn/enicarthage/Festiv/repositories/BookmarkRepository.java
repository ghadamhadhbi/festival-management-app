package tn.enicarthage.Festiv.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.enicarthage.Festiv.entities.Bookmark;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findByUserId(Long userId);
    Optional<Bookmark> findByUserIdAndSpectacleIdSpec(Long userId, Long spectacleId);
    boolean existsByUserIdAndSpectacleIdSpec(Long userId, Long spectacleId);
    void deleteByUserIdAndSpectacleIdSpec(Long userId, Long spectacleId);
}