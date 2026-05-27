package tn.star.Pfe.dto.inscription;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class InscriptionCreateRequest {
    private List<GuestDTO> guests = new ArrayList<>();
    private String paymentPeriod;
}
