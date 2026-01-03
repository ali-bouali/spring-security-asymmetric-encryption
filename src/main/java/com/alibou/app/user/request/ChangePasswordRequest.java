package com.alibou.app.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangePasswordRequest {

    @NotBlank(message = "VALIDATION.CHANGE_PASSWORD.CURRENT_PASSWORD.BLANK")
    @Schema(example = "oldPassword1!_")
    private String currentPassword;

    @NotBlank(message = "VALIDATION.CHANGE_PASSWORD.NEW_PASSWORD.BLANK")
    @Size(
            min = 8,
            max = 72,
            message = "VALIDATION.CHANGE_PASSWORD.NEW_PASSWORD.SIZE"
    )
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*\\W).*$",
            message = "VALIDATION.CHANGE_PASSWORD.NEW_PASSWORD.WEAK"
    )
    @Schema(example = "newPassword1!_")
    private String newPassword;

    @NotBlank(message = "VALIDATION.CHANGE_PASSWORD.CONFIRM_NEW_PASSWORD.BLANK")
    @Size(
            min = 8,
            max = 72,
            message = "VALIDATION.CHANGE_PASSWORD.CONFIRM_NEW_PASSWORD.SIZE"
    )
    @Schema(example = "newPassword1!_")
    private String confirmNewPassword;
}
