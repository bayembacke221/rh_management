package sn.bmbacke.rh.payload.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionUpdateDTO {
    @Size(min = 2, max = 100, message = "Le titre doit contenir entre 2 et 100 caractères")
    private String title;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    private String description;

    @Size(min = 1, max = 10, message = "Le grade doit contenir entre 1 et 10 caractères")
    private String grade;

    @DecimalMin(value = "0.0", inclusive = true, message = "Le salaire minimum ne peut pas être négatif")
    private BigDecimal minSalary;

    @DecimalMin(value = "0.0", inclusive = true, message = "Le salaire maximum ne peut pas être négatif")
    private BigDecimal maxSalary;

    private Long departmentId;

    @Size(max = 1000, message = "Les responsabilités ne peuvent pas dépasser 1000 caractères")
    private String responsibilities;

    @Size(max = 1000, message = "Les exigences ne peuvent pas dépasser 1000 caractères")
    private String requirements;

    private Boolean active;
}
