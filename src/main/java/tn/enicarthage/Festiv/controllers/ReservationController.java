package tn.enicarthage.Festiv.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import tn.enicarthage.Festiv.entities.Client;
import tn.enicarthage.Festiv.entities.Performance;
import tn.enicarthage.Festiv.entities.Reservation;
import tn.enicarthage.Festiv.repositories.ClientRepository;
import tn.enicarthage.Festiv.repositories.PerformanceRepository;
import tn.enicarthage.Festiv.repositories.ReservationRepository;
import  tn.enicarthage.Festiv.entities.ReservationRequest;
import tn.enicarthage.Festiv.entities.ReservationResponse;

import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin
public class ReservationController {

    @Autowired
    private ReservationRepository reservationRepository;
    
    
    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private PerformanceRepository performanceRepository;
    @Autowired
    private JavaMailSender mailSender;


    @GetMapping
    public ResponseEntity<List<Reservation>> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return new ResponseEntity<>(reservations, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        Optional<Reservation> reservation = reservationRepository.findById(id);
        return reservation.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Reservation>> getReservationsByClient(@PathVariable Long clientId) {
        List<Reservation> reservations = reservationRepository.findByClientIdclt(clientId);
        return new ResponseEntity<>(reservations, HttpStatus.OK);
    }
    
    private void sendConfirmationEmail(String toEmail, String name, String performanceTitre) {
        if (toEmail == null || performanceTitre == null) return;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Confirmation de réservation");
        message.setText("Bonjour " + name + ",\n\n" +
            "Votre réservation pour le spectacle \"" + performanceTitre + "\" a été confirmée.\n\n" +
            "Merci pour votre confiance.\n\nCordialement,\nL'équipe Festiv");
        mailSender.send(message);
    }



    

    @GetMapping("/performance/{performanceId}")
    public ResponseEntity<List<Reservation>> getReservationsByPerformance(@PathVariable Long performanceId) {
        List<Reservation> reservations = reservationRepository.findByPerformanceIdPerformance(performanceId);
        return new ResponseEntity<>(reservations, HttpStatus.OK);
    }

    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<Reservation>> getReservationsByStatut(@PathVariable String statut) {
        List<Reservation> reservations = reservationRepository.findByStatut(statut);
        return new ResponseEntity<>(reservations, HttpStatus.OK);
    }
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createReservation(@RequestBody ReservationRequest request) {
        try {
            // Create new reservation
            Reservation reservation = new Reservation();
            
            // Set client if clientId is provided
            if (request.getClientId() != null) {
                Optional<Client> clientOpt = clientRepository.findById(request.getClientId());
                if (!clientOpt.isPresent()) {
                    return new ResponseEntity<>("Client not found", HttpStatus.BAD_REQUEST);
                }
                reservation.setClient(clientOpt.get());
            }
            
            // Set performance if performanceId is provided
            if (request.getPerformanceId() != null) {
                Optional<Performance> performanceOpt = performanceRepository.findById(request.getPerformanceId());
                if (!performanceOpt.isPresent()) {
                    return new ResponseEntity<>("Performance not found", HttpStatus.BAD_REQUEST);
                }
                reservation.setPerformance(performanceOpt.get());
            }
            if (reservation.getClient() != null && reservation.getClient().getEmail() != null) {
                sendConfirmationEmail(
                    reservation.getClient().getEmail(),
                    reservation.getClient().getNomclt(),
                    reservation.getPerformance().getSpectacle().getTitre() // ou adapte selon ta structure
                );
            }
            
            // Set other fields
            reservation.setDateReservation(LocalDateTime.now());
            reservation.setStatut(request.getStatut() != null ? request.getStatut() : "Confirmée");
            reservation.setMontantTotal(request.getMontantTotal());
            
            // Save the reservation
            Reservation savedReservation = reservationRepository.save(reservation);
            return new ResponseEntity<>(savedReservation, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error creating reservation: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(@PathVariable Long id, @RequestBody Reservation reservation) {
        if (!reservationRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        reservation.setIdReservation(id);
        Reservation updatedReservation = reservationRepository.save(reservation);
        return new ResponseEntity<>(updatedReservation, HttpStatus.OK);
    }


    @PutMapping("/{id}/cancel")
    public ResponseEntity<Reservation> cancelReservation(@PathVariable Long id) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(id);
        if (reservationOpt.isPresent()) {
            Reservation reservation = reservationOpt.get();
            reservation.setStatut("Annulée");
            Reservation updatedReservation = reservationRepository.save(reservation);
            return new ResponseEntity<>(updatedReservation, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        if (!reservationRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        reservationRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @PostMapping(path = "/guest", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createGuestReservation(@RequestBody ReservationRequest request) {
        try {
            // Create new reservation
            Reservation reservation = new Reservation();
            
            // Set this as a guest reservation
            reservation.setIsGuest(true);

            // Check and set guest-specific information from the request
            if (request.getGuestName() != null && request.getGuestEmail() != null) {
                reservation.setGuestName(request.getGuestName());
                reservation.setGuestEmail(request.getGuestEmail());
                
            } else {
                return new ResponseEntity<>("Guest name and email are required", HttpStatus.BAD_REQUEST);
            }

            // Set performance if performanceId is provided
            if (request.getPerformanceId() != null) {
                Optional<Performance> performanceOpt = performanceRepository.findById(request.getPerformanceId());
                if (!performanceOpt.isPresent()) {
                    return new ResponseEntity<>("Performance not found", HttpStatus.BAD_REQUEST);
                }
                reservation.setPerformance(performanceOpt.get());
            } else {
                return new ResponseEntity<>("Performance ID is required", HttpStatus.BAD_REQUEST);
            }
            sendConfirmationEmail(
            	    reservation.getGuestEmail(),
            	    reservation.getGuestName(),
            	    reservation.getPerformance().getSpectacle().getTitre()
            	);


            // Set other fields
            reservation.setDateReservation(LocalDateTime.now());
            reservation.setStatut(request.getStatut() != null ? request.getStatut() : "Confirmée");
            reservation.setMontantTotal(request.getMontantTotal());

            // Set client to null since it's a guest reservation
            reservation.setClient(null);

            // Save the reservation
            Reservation savedReservation = reservationRepository.save(reservation);

            // Create response object
            ReservationResponse response = new ReservationResponse();
            response.setReservationId(savedReservation.getIdReservation());
            response.setPerformanceId(request.getPerformanceId());
            response.setDateReservation(savedReservation.getDateReservation());
            response.setStatut(savedReservation.getStatut());
            response.setMontantTotal(savedReservation.getMontantTotal());

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace(); // Log the full stack trace
            return new ResponseEntity<>("Error creating guest reservation: " + e.getMessage(),
                                        HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

