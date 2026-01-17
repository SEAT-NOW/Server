package depth.main.seatnow.domain.user.service;

import depth.main.seatnow.domain.user.entity.User;
import depth.main.seatnow.domain.user.repository.UserRepository;
import depth.main.seatnow.global.exception.custom.BadRequestException;
import depth.main.seatnow.global.exception.custom.NotFoundException;
import depth.main.seatnow.global.exception.error.ErrorCode;
import depth.main.seatnow.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Transactional
    public void withdrawUser(CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND));

        userRepository.delete(user);
    }

    public void verifyPassword(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException(ErrorCode.PASSWORD_MISMATCH);
        }
    }

    @Transactional
    public void updatePassword(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND));

        user.updatePassword(passwordEncoder.encode(password));
    }
}
