package br.com.ml.mktplace.orders.adapter.outbound.id;

import br.com.ml.mktplace.orders.domain.port.IDGenerator;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;

/**
 * ULID implementation of IDGenerator.
 * Generates unique, sortable identifiers using ULID format.
 */
@Component
public class UlidGenerator implements IDGenerator {
    
    private static final String CROCKFORD_BASE32 = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";
    private static final int RANDOM_PART_LENGTH = 16;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    @Override
    public String generate() {
        long timestamp = Instant.now().toEpochMilli();
        byte[] randomBytes = new byte[RANDOM_PART_LENGTH];
        SECURE_RANDOM.nextBytes(randomBytes);
        
        return encodeULID(timestamp, randomBytes);
    }
    
    @Override
    public String[] generateMultiple(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be greater than zero");
        }
        
        String[] ids = new String[count];
        long timestamp = Instant.now().toEpochMilli();
        
        for (int i = 0; i < count; i++) {
            byte[] randomBytes = new byte[RANDOM_PART_LENGTH];
            SECURE_RANDOM.nextBytes(randomBytes);
            ids[i] = encodeULID(timestamp, randomBytes);
            
            // Increment timestamp by 1ms to ensure uniqueness
            timestamp++;
        }
        
        return ids;
    }
    
    @Override
    public boolean isValid(String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        
        // ULID should be 26 characters long
        if (id.length() != 26) {
            return false;
        }
        
        // Check if all characters are valid Crockford Base32
        for (char c : id.toCharArray()) {
            if (CROCKFORD_BASE32.indexOf(c) == -1) {
                return false;
            }
        }
        
        return true;
    }
    
    private String encodeULID(long timestamp, byte[] randomBytes) {
        StringBuilder sb = new StringBuilder(26);
        
        // Encode timestamp (48 bits = 10 characters)
        sb.append(encodeBase32(timestamp, 10));
        
        // Encode random bytes (128 bits = 16 characters)
        long randomHigh = bytesToLong(randomBytes, 0);
        long randomLow = bytesToLong(randomBytes, 8);
        
        sb.append(encodeBase32(randomHigh, 8));
        sb.append(encodeBase32(randomLow, 8));
        
        return sb.toString();
    }
    
    private String encodeBase32(long value, int length) {
        StringBuilder sb = new StringBuilder(length);
        
        for (int i = length - 1; i >= 0; i--) {
            int index = (int) ((value >>> (i * 5)) & 0x1F);
            sb.append(CROCKFORD_BASE32.charAt(index));
        }
        
        return sb.toString();
    }
    
    private long bytesToLong(byte[] bytes, int offset) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result = (result << 8) | (bytes[offset + i] & 0xFF);
        }
        return result;
    }
}