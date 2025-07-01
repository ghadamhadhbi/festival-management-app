package tn.enicarthage.Festiv.entities;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SpectaclePricesDTO {
    private BigDecimal prixGold;
    private BigDecimal prixSilver;
    private BigDecimal prixNormal;
}
