package com.techstore.techstore_api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;

    @Email(message = "Format d'email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit faire au moins 6 caractères")
    private String password;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    private String phone;
    private String profilePictureUrl; 

}