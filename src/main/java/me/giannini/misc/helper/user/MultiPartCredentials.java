package me.giannini.misc.helper.user;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;

/**
 * JAAS implementations do not support portable multiple credentials authentication.
 * Only single username and password (via j_username, j_password) are always supported.
 * In case of complex credentials (like PIN+TAN), password field has to be abused to pass encoded complex value.
 *
 * Individual segments are separated by ',' character, the ',' itself is escaped via UrlEncoder.
 * Javascript escaping equivalent would be escape()/unescape()
 */
public class MultiPartCredentials {

  /** Separator character */
  public static final String SEPARATOR = ",";

  /**
   * Encode segments into single password value
   *
   * @param segments
   * @return encoded password representing given segments
   */
  public static String encode(final Object... segments) {
    if (segments == null) {
      return "";
    }
    return encode(Arrays.asList(segments));
  }

  /**
   * Encode segments into single password value
   *
   * @param segments
   * @return encoded password representing given segments
   */
  public static String encode(final Iterable<?> segments) {
    if (segments == null) {
      return "";
    }
    final StringBuilder sb = new StringBuilder();
    int i = 0;
    for (final Object each : segments) {
      if (i > 0) {
        sb.append(SEPARATOR);
      }
      sb.append(escape(each));
      i++;
    }

    return sb.toString();
  }

  public static void main(final String[] args) {
    final String encoded = "%20%21%22%23%24%25%26%27%28%29*+%2C-./%3A%3B%3C%3D%3E%3F@%5B%5C%5D%5E_%60%7B%7C%7D%7E";
    final String chars = " !\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
    System.out.println(chars);
    final String[] decoded = decode(encoded + ",305601");
    System.out.println(decoded[0] + " and token is " + decoded[1]);
  }

  /**
   * Decode segments from encoded password
   *
   * @param input
   * @return segments decoded from given password
   */
  public static String[] decode(final String input) {
    if (input == null) {
      return new String[0];
    }
    final String[] segments = input.split(SEPARATOR);
    for (int i = 0; i < segments.length; i++) {
      segments[i] = unescape(segments[i]);
    }
    return segments;
  }

  protected static String escape(final Object input) {
    if (input == null) {
      return "";
    }
    try {
      return URLEncoder.encode(input.toString(), "UTF-8");
    } catch (final UnsupportedEncodingException e) {
      // Should not happen, UTF-8 should be always available
      return "";
    }
  }

  protected static String unescape(final String input) {
    try {
      return URLDecoder.decode(input, "UTF-8");
    } catch (final UnsupportedEncodingException e) {
      // Should not happen, UTF-8 should be always available
      return input;
    }
  }
}
