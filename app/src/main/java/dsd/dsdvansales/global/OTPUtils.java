package dsd.dsdvansales.global;

import org.apache.commons.codec.binary.Base32;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.Locale;

public class OTPUtils {

    public static String generateTOTP(String secret) {

        try {
            Base32 base32 = new Base32();
            byte[] decodedKey = base32.decode(secret);

            long timeIndex = new Date().getTime() / 30000;
            byte[] data = ByteBuffer.allocate(8).putLong(timeIndex).array();

            // Generate HMAC-SHA1 hash
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(decodedKey, "HmacSHA1"));
            byte[] hmacResult = mac.doFinal(data);

            // Get dynamic offset
            int offset = hmacResult[hmacResult.length - 1] & 0xF;
            int otp = ((hmacResult[offset] & 0x7F) << 24) |
                    ((hmacResult[offset + 1] & 0xFF) << 16) |
                    ((hmacResult[offset + 2] & 0xFF) << 8) |
                    (hmacResult[offset + 3] & 0xFF);

            // Convert OTP to 6-digit format
            return String.format(Locale.ENGLISH, "%06d", otp % 1000000);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
