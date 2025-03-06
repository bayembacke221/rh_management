package sn.bmbacke.rh.payload.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionCreateDTO {
    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 2, max = 100, message = "Le titre doit contenir entre 2 et 100 caractères")
    private String title;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    private String description;

    @NotBlank(message = "Le grade est obligatoire")
    @Size(min = 1, max = 10, message = "Le grade doit contenir entre 1 et 10 caractères")
    private String grade;

    @NotNull(message = "Le salaire minimum est obligatoire")
    @DecimalMin(value = "0.0", inclusive = true, message = "Le salaire minimum ne peut pas être négatif")
    private BigDecimal minSalary;

    @NotNull(message = "Le salaire maximum est obligatoire")
    @DecimalMin(value = "0.0", inclusive = true, message = "Le salaire maximum ne peut pas être négatif")
    private BigDecimal maxSalary;

    private Long departmentId;

    @Size(max = 1000, message = "Les responsabilités ne peuvent pas dépasser 1000 caractères")
    private String responsibilities;

    @Size(max = 1000, message = "Les exigences ne peuvent pas dépasser 1000 caractères")
    private String requirements;

    private Boolean active;
}

