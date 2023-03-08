package me.giannini.misc.helper.user;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class HashGenerator {

  public static void main(final String[] args) {
    // final String salt = generateSalt();
    final String salt = "oJwGu6ONMl0=";
    final String password = "Ewb35&+k";
    System.out.println("generated hash of " + password + " with salt " + salt + " is " + calculateHash(password, salt));
  }

  /**
   * Generate a random salt string
   *
   * @return Base64 encoded salt string
   */
  public static String generateSalt() {
    final SecureRandom random = new SecureRandom();
    final byte[] salt = new byte[8];
    random.nextBytes(salt);
    return Base64.getEncoder().encodeToString(salt);
  }

  /**
   * From the password and salt, return the corresponding password hash
   * (Using the SHA-256 and 1000 iterations)
   *
   *
   * @param password The password to encrypt
   * @param salt The salt as Base64 encoded string
   * @return password hash as Base64 encoded string
   */
  public static String calculateHash(final String password, final String salt) {
    final int iterationNb = 1000;

    final MessageDigest digest = getSha256Digester();
    digest.reset();
    digest.update(Base64.getDecoder().decode(salt));

    byte[] result = digest.digest(password.getBytes(StandardCharsets.UTF_8));
    for (int i = 0; i < iterationNb; i++) {
      digest.reset();
      result = digest.digest(result);
    }
    return Base64.getEncoder().encodeToString(result);
  }

  private static MessageDigest getSha256Digester() {
    try {
      return MessageDigest.getInstance("SHA-256");
    } catch (final NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}
