package com.test.backend.iam.infrastructure.hashing.bcrypt.services;

import com.test.backend.iam.infrastructure.hashing.bcrypt.BCryptHashingService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class HashingServiceImpl implements BCryptHashingService {

    private final BCryptPasswordEncoder bcryptPasswordEncoder;

    public HashingServiceImpl() {
        this.bcryptPasswordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return bcryptPasswordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return bcryptPasswordEncoder.matches(rawPassword, encodedPassword);
    }
}
