package tn.star.Pfe.dto.election.candidate;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String profileImageUrl;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
