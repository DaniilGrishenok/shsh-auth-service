package com.shsh.auth_service_social_network.service;

import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

@Component
public class IdGenerator {
    public String generateUserId() {
        UUID uuid = UUID.randomUUID();
        byte[] uuidBytes = ByteBuffer.wrap(new byte[16])
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits())
                .array();
        return "U-" + Base64.getUrlEncoder().withoutPadding().encodeToString(uuidBytes);
    }

}
