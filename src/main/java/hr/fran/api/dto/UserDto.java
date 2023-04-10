package hr.fran.api.dto;

import hr.fran.api.user.Role;

public record UserDto(String firstName, String lastName, String email, Role role) {
}
