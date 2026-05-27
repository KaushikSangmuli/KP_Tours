package KP_TOURS.maintenance;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Base64;

public class MaintenanceAccessManager {

    private static final Path FLAG_FILE =
            Path.of(
                    System.getProperty("user.home"),
                    ".safarease",
                    "sys.dat"
            );

    private static final String SECRET_KEY = "SafarEaseKey2027"; // 16 chars required for AES

    private MaintenanceAccessManager() {
    }

    public static void initializeFlagFileIfMissing() {
        try {
            if (!Files.exists(FLAG_FILE)) {
                Files.createDirectories(FLAG_FILE.getParent());

                String data = "maintenanceUnlocked=false";
                Files.writeString(FLAG_FILE, encrypt(data));
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize maintenance file", e);
        }
    }

    public static boolean isMaintenanceUnlocked() {
        try {
            if (!Files.exists(FLAG_FILE)) {
                initializeFlagFileIfMissing();
                return false;
            }

            String encryptedData = Files.readString(FLAG_FILE);
            String decryptedData = decrypt(encryptedData);

            return decryptedData.contains("maintenanceUnlocked=true");

        } catch (Exception e) {
            return false;
        }
    }

    public static void markMaintenanceUnlocked() {
        try {
            Files.createDirectories(FLAG_FILE.getParent());

            String data =
                    "maintenanceUnlocked=true"
                            + "\nunlockedDate=" + LocalDate.now();

            Files.writeString(FLAG_FILE, encrypt(data));

        } catch (Exception e) {
            throw new RuntimeException("Unable to update maintenance file", e);
        }
    }

    private static String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");

        SecretKeySpec key =
                new SecretKeySpec(
                        SECRET_KEY.getBytes(),
                        "AES"
                );

        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] encrypted =
                cipher.doFinal(data.getBytes());

        return Base64.getEncoder().encodeToString(encrypted);
    }

    private static String decrypt(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");

        SecretKeySpec key =
                new SecretKeySpec(
                        SECRET_KEY.getBytes(),
                        "AES"
                );

        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] decoded =
                Base64.getDecoder().decode(encryptedData);

        byte[] decrypted =
                cipher.doFinal(decoded);

        return new String(decrypted);
    }
}