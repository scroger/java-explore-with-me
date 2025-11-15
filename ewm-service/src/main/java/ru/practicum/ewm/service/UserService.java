package ru.practicum.ewm.service;

import java.util.List;

import ru.practicum.ewm.dto.UserDto;

public interface UserService {
    List<UserDto> getUsers(List<Long> ids, int from, int size);

    UserDto registerUser(UserDto dto);

    void deleteUser(Long userId);
}
