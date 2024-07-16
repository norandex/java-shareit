package ru.practicum.shareit.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * TODO Sprint add-controllers.
 */
@Data
@Builder
@AllArgsConstructor
public class User {
    private Long id;
    private String name;
    @Email
    @NotNull
    private String email;


    public User(String name, String birthDate) {
        this.name = name;
        this.email = birthDate;
    }
}
