package hr.fran.api.user;

import hr.fran.api.dto.UserDto;

import java.util.List;

public interface UserService {

    List<UserDto> getStudents();
}
