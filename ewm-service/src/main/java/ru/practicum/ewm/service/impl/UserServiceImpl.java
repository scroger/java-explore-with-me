package ru.practicum.ewm.service.impl;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.practicum.ewm.dto.UserDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.UserMapper;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        Pageable pg = PageRequest.of(from / size, size);
        List<User> list;

        if (null != ids && !ids.isEmpty()) {
            list = userRepository.findAllById(ids);
        } else {
            list = userRepository.findAll(pg).getContent();
        }

        return list.stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public UserDto registerUser(UserDto dto) {
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new ConflictException("E‑mail already registered");
        }

        User user = User.builder()
                .email(dto.email())
                .name(dto.name())
                .build();
        User saved = userRepository.save(user);

        return UserMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        userRepository.delete(user);
    }

}
