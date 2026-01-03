package com.alibou.app.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileUpdateRequest {

    @NotBlank(message = "VALIDATION.PROFILE_UPDATE.FIRST_NAME.BLANK")
    @Size(
            min = 1,
            max = 50,
            message = "VALIDATION.PROFILE_UPDATE.FIRST_NAME.SIZE"
    )
    @Pattern(
            regexp = "^[\\p{L} '-]+$",
            message = "VALIDATION.PROFILE_UPDATE.FIRST_NAME.PATTERN"
    )
    @Schema(example = "Ali")
    private String firstName;

    @NotBlank(message = "VALIDATION.PROFILE_UPDATE.LAST_NAME.BLANK")
    @Size(
            min = 1,
            max = 50,
            message = "VALIDATION.PROFILE_UPDATE.LAST_NAME.SIZE"
    )
    @Pattern(
            regexp = "^[\\p{L} '-]+$",
            message = "VALIDATION.PROFILE_UPDATE.LAST_NAME.PATTERN"
    )
    @Schema(example = "Bouali")
    private String lastName;

    @Past(message = "VALIDATION.PROFILE_UPDATE.DATE_OF_BIRTH.FUTURE")
    @Schema(example = "1990-01-15")
    private LocalDate dateOfBirth;
}
