package studyolle.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data @NoArgsConstructor @AllArgsConstructor
public class PasswordForm {

    @NotBlank
    @Length(min = 8, max = 50)
    private String newPassword;

    @NotBlank
    @Length(min = 8, max = 50)
    private String newPasswordConfirm;

    public boolean equalsPasswordAndConfirm() {
        return this.newPassword.equals(this.newPasswordConfirm);
    }
}
