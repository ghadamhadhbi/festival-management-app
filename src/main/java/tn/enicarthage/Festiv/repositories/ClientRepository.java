package tn.enicarthage.Festiv.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.enicarthage.Festiv.entities.Client;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByEmail(String email);
    Optional<Client> findByEmailAndMotp(String email, String motp);
    boolean existsByEmail(String email);
    Optional<Client> findByNomcltAndPrenomclt(String nomclt, String prenomclt);
}
