package me.giannini.misc.helper;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Represents an application version number that can be compared.
 *
 * @see #compareTo(VersionNumber)
 */
public final class VersionNumber implements Comparable<VersionNumber> {

  private static final String SNAPSHOT_TOKEN = "-SNAPSHOT";
  private static final String SEPARATOR = ".";
  private static final String SEPARATOR_REGEX = "\\" + SEPARATOR;

  private final String versionNumberAsString;
  private final int[] versions;
  private final boolean isSnapshot;

  private VersionNumber(final String versionNumberAsString, final int[] versions, final boolean isSnapshot) {
    this.versionNumberAsString = versionNumberAsString;
    this.versions = versions;
    this.isSnapshot = isSnapshot;
  }

  /**
   * Parses the passed version String and creates a new {@link VersionNumber} object.
   *
   * @param versionString the version String to be parsed
   * @return the created {@link VersionNumber} object
   */
  public static VersionNumber from(final String versionString) {
    final boolean isSnapshot = versionString.toUpperCase().endsWith(SNAPSHOT_TOKEN);
    return new VersionNumber(Objects.requireNonNull(versionString), parse(versionString, isSnapshot), isSnapshot);
  }

  private static int[] parse(final String versionString, final boolean isSnapshot) {
    return IntStream.concat(
        Stream.of(versionString.split(SEPARATOR_REGEX))
            .map(VersionNumber::parseIntUntilFirstNonDigit)
            .filter(OptionalInt::isPresent)
            .mapToInt(OptionalInt::getAsInt),
        isSnapshot ? IntStream.of(Integer.MAX_VALUE) : IntStream.empty())
        .toArray();
  }

  private static OptionalInt parseIntUntilFirstNonDigit(final String fragment) {
    final int index = indexOfLastDigitChar(fragment);
    if (index > -1) {
      return OptionalInt.of(Integer.parseInt(fragment.substring(0, index + 1)));
    } else {
      return OptionalInt.empty();
    }
  }

  private static int indexOfLastDigitChar(final String string) {
    boolean digitFound = false;
    int i = 0;
    while (i < string.length()) {
      if (Character.isDigit(string.charAt(i))) {
        i++;
        digitFound = true;
      } else {
        return digitFound ? i - 1 : -1;
      }
    }
    return digitFound ? string.length() - 1 : -1;
  }

  /**
   * Compares instances by comparing each version field sub-sequentially and also considering <i>-&#83;&#78;&#65;&#80;&#83;&#72;&#79;&#84;</i> suffixes. Snapshots are considered as
   * the greatest possible version
   * for the corresponding version field. Not numeric strings (except the <i>-&#83;&#78;&#65;&#80;&#83;&#72;&#79;&#84;</i> suffix) are completely ignored.<br>
   * <br>
   * Here are some examples to illustrate the comparison logic:<br>
   * <ul>
   * <li>3.1 &gt; 3</li>
   * <li>3.14 &lt; 3.15.5.1</li>
   * <li>3.14.1.2 &lt; 3.14.1.3</li>
   * <li>3-&#83;&#78;&#65;&#80;&#83;&#72;&#79;&#84; &gt; 3.13.4.5</li>
   * <li>3.13-&#83;&#78;&#65;&#80;&#83;&#72;&#79;&#84; &gt; 3.12-&#83;&#78;&#65;&#80;&#83;&#72;&#79;&#84;</li>
   * <li>3.13-&#83;&#78;&#65;&#80;&#83;&#72;&#79;&#84; = 3.13.special_branch-&#83;&#78;&#65;&#80;&#83;&#72;&#79;&#84;</li>
   * <li>3.13.4 = 3.13.4.special_branch</li>
   * <li>3.13.4 = 3.13.4_special_branch</li>
   * </ul>
   */
  @Override
  public int compareTo(final VersionNumber other) {
    final int[] otherVersions = Objects.requireNonNull(other).versions;
    for (int i = 0; i < Integer.max(versions.length, otherVersions.length); i++) {
      final int left = i < versions.length ? versions[i] : 0;
      final int right = i < otherVersions.length ? otherVersions[i] : 0;
      final int result = Integer.compare(left, right);
      if (result != 0) {
        return result;
      }
    }
    return 0;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(versions);
    result = prime * result + Objects.hash(isSnapshot);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof VersionNumber)) {
      return false;
    }
    final VersionNumber other = (VersionNumber)obj;
    return isSnapshot == other.isSnapshot &&
        Arrays.equals(versions, other.versions);
  }

  /**
   * @return the version String originally passed on the creation of this
   */
  public String getVersionString() {
    return versionNumberAsString;
  }

  /**
   * @return true if this version is a snapshot version
   */
  public boolean isSnapshot() {
    return isSnapshot;
  }

  /**
   * Strips this version to the amount of given fields. e. g. <br>
   * <br>
   * <code>
   *   new ApplicationVersion("3.14.5.1").strip(2); <br>
   *   // returns <br>
   *   new ApplicationVersion("3.14"); <br>
   * </code>
   *
   * @param numberOfFields the amount of fields to keep
   * @return the stripped {@link VersionNumber}
   */
  public VersionNumber strip(final int numberOfFields) {
    if (numberOfFields <= versions.length) {
      final int[] stripped = new int[numberOfFields];
      System.arraycopy(versions, 0, stripped, 0, numberOfFields);
      return new VersionNumber(IntStream.of(stripped).mapToObj(String::valueOf).collect(joining(SEPARATOR)), stripped, false);
    } else {
      return this;
    }
  }

  @Override
  public String toString() {
    return versionNumberAsString;
  }

  /**
   * Returns a {@link VersionNumber} which is the same as this but without the SNAPSHOT portion in case this is a snapshot version. If this is not a snapshot version, then
   * this is
   * returned.
   *
   * @return the version without the snapshot portion
   */
  public VersionNumber removeSnapshot() {
    if (isSnapshot) {
      return VersionNumber.from(getVersionString().substring(0, getVersionString().lastIndexOf(SNAPSHOT_TOKEN)));
    } else {
      return this;
    }
  }

}
