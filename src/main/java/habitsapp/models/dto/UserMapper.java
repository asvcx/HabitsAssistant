package habitsapp.models.dto;

import habitsapp.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
    UserDto userToUserDto(User user);
    User userDtoToUser(UserDto userDTO);
}