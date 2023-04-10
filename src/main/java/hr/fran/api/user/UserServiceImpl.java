package hr.fran.api.user;

import hr.fran.api.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;


    @Override
    public List<UserDto> getStudents() {
        return userRepository.findAll().stream().map(this::mapUserToDTO).collect(Collectors.toList());
    }


    private UserDto mapUserToDTO(final User user){
        return new UserDto(user.getFirstName(), user.getLastName(), user.getEmail(),user.getRole());
    }
}
