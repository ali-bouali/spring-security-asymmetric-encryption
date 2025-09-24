package com.alibou.app.user.request;

import com.alibou.app.validation.CustomPasswordPattern;
import com.alibou.app.validation.CustomPasswordSize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "VALIDATION.CHANGE_PASSWORD.PASSWORD.BLANK")
    @CustomPasswordSize(message = "VALIDATION.CHANGE_PASSWORD.PASSWORD.SIZE")
    @CustomPasswordPattern(message = "VALIDATION.CHANGE_PASSWORD.PASSWORD.WEAK")
    @Schema(example = "<Password>")
    private String currentPassword;

    @NotBlank(message = "VALIDATION.CHANGE_PASSWORD.PASSWORD.BLANK")
    @CustomPasswordSize(message = "VALIDATION.CHANGE_PASSWORD.PASSWORD.SIZE")
    @CustomPasswordPattern(message = "VALIDATION.CHANGE_PASSWORD.PASSWORD.WEAK")
    @Schema(example = "<Password>")
    private String newPassword;

    @NotBlank(message = "VALIDATION.CHANGE_PASSWORD.PASSWORD.BLANK")
    @CustomPasswordSize(message = "VALIDATION.CHANGE_PASSWORD.PASSWORD.SIZE")
    @CustomPasswordPattern(message = "VALIDATION.CHANGE_PASSWORD.PASSWORD.WEAK")
    @Schema(example = "<Password>")
    private String confirmNewPassword;

}
