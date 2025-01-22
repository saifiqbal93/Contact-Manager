package com.scm.forms;

import com.scm.validators.ValidFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserUpdateForm {

    @NotBlank(message = "Username is required")
    @Size(min = 3, message = "Min 3 Characters is required")
    private String updateName;

    @NotBlank(message = "Phone Number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Invalid Phone Number")
    private String updatePhoneNumber;

    @NotBlank(message = "About is required")
    private String updateAbout;

    @ValidFile(message = "Invalid File")
    private MultipartFile updateProfileImage;

    private String updateProfilePic;
}
