package com.alibou.app.user.request;

import com.alibou.app.validation.CustomDatePattern;
import com.alibou.app.validation.CustomNameSize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
    @CustomNameSize(message = "VALIDATION.PROFILE_UPDATE.LAST_NAME.SIZE")
    @Schema(example = "Ali")
    private String firstName;

    @NotBlank(message = "VALIDATION.PROFILE_UPDATE.LAST_NAME.BLANK")
    @CustomNameSize(message = "VALIDATION.PROFILE_UPDATE.LAST_NAME.SIZE")
    @Schema(example = "Ali")
    private String lastName;

    @NotBlank(message = "VALIDATION.PROFILE_UPDATE.LAST_NAME.BLANK")
    @CustomDatePattern(message = "VALIDATION.PROFILE_UPDATE.LAST_NAME.DATE_OF_BIRTH")
    private LocalDate dateOfBirth;
}
