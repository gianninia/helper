package me.giannini.misc.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import nl.jqno.equalsverifier.EqualsVerifier;

public class VersionNumberTest {

  private enum Operator {

    LESS_THAN("<"),
    EQUALS("="),
    GREATER_THAN(">");

    private final String operator;

    private Operator(final String operator) {
      this.operator = operator;
    }

    public static Operator from(final int comparisonResult) {
      return comparisonResult == 0 ? EQUALS : (comparisonResult < 0 ? LESS_THAN : GREATER_THAN);
    }

    @Override
    public String toString() {
      return operator;
    }
  }

  public static Stream<Arguments> data() {
    return Stream.of(
        Arguments.of("2", "3", Operator.LESS_THAN),
        Arguments.of("3", "2", Operator.GREATER_THAN),
        Arguments.of("3", "3", Operator.EQUALS),
        Arguments.of("3.1", "3.2", Operator.LESS_THAN),
        Arguments.of("3.2", "3.1", Operator.GREATER_THAN),
        Arguments.of("3.1", "3.1", Operator.EQUALS),
        Arguments.of("3.1.1", "3.1.2", Operator.LESS_THAN),
        Arguments.of("3.1.2", "3.1.1", Operator.GREATER_THAN),
        Arguments.of("3.1.1", "3.1.1", Operator.EQUALS),
        Arguments.of("3.1.1.1", "3.1.1.2", Operator.LESS_THAN),
        Arguments.of("3.1.1.2", "3.1.1.1", Operator.GREATER_THAN),
        Arguments.of("3.1.1.1", "3.1.1.1", Operator.EQUALS),
        Arguments.of("2", "3.1", Operator.LESS_THAN),
        Arguments.of("2", "3.1.1.1", Operator.LESS_THAN),
        Arguments.of("3", "3.1", Operator.LESS_THAN),
        Arguments.of("3", "3.1.1", Operator.LESS_THAN),
        Arguments.of("3", "3.1.1.1", Operator.LESS_THAN),
        Arguments.of("3.1", "3", Operator.GREATER_THAN),
        Arguments.of("3.1.1", "3", Operator.GREATER_THAN),
        Arguments.of("3.1.1.1", "3", Operator.GREATER_THAN),
        Arguments.of("3.1", "3.1", Operator.EQUALS),
        Arguments.of("3.2", "3.1", Operator.GREATER_THAN),
        Arguments.of("3-SNAPSHOT", "3.13", Operator.GREATER_THAN),
        Arguments.of("3-SNAPSHOT", "3-SNAPSHOT", Operator.EQUALS),
        Arguments.of("3.13-SNAPSHOT", "3.13.2", Operator.GREATER_THAN),
        Arguments.of("3.13-SNAPSHOT", "3.14.3", Operator.LESS_THAN),
        Arguments.of("3.13-SNAPSHOT", "3.14-SNAPSHOT", Operator.LESS_THAN),
        Arguments.of("3.13.special_branch-SNAPSHOT", "3.13.4", Operator.GREATER_THAN),
        Arguments.of("3.13.4", "3.13.special_branch-SNAPSHOT", Operator.LESS_THAN),
        Arguments.of("3.13special_branch-SNAPSHOT", "3.13.4", Operator.GREATER_THAN),
        Arguments.of("3.13.4", "3.13special_branch-SNAPSHOT", Operator.LESS_THAN),
        Arguments.of("3.13-SNAPSHOT", "3.13.special_branch-SNAPSHOT", Operator.EQUALS),
        Arguments.of("3.13.4", "3.13.4.special_branch", Operator.EQUALS),
        Arguments.of("3.13.4", "19.02.1", Operator.LESS_THAN),
        Arguments.of("19.02.0", "19.02.1", Operator.LESS_THAN),
        Arguments.of("19.02.1", "19.02.1", Operator.EQUALS),
        Arguments.of("19.02.1.1", "19.02.1.1", Operator.EQUALS),
        Arguments.of("19.06.0", "19.02.3.4", Operator.GREATER_THAN),
        Arguments.of("19.02.1", "19.02.1special_branch-SNAPSHOT", Operator.LESS_THAN),
        Arguments.of("19.02.1", "19.02.1.special_branch-SNAPSHOT", Operator.LESS_THAN));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testCompareTo(final String version, final String versionToCheck, final Operator expected) throws Exception {
    // arrange
    final VersionNumber testee = VersionNumber.from(version);
    // act
    final int result = testee.compareTo(VersionNumber.from(versionToCheck));
    // assert
    final Operator resultOperator = Operator.from(result);
    assertTrue(expected == resultOperator, version + " must be  " + expected + " " + versionToCheck + ", but was " + resultOperator);
  }

  @Test
  public void init_VersionStringNull_Exception() throws Exception {
    // act & assert
    assertThrows(NullPointerException.class, () -> VersionNumber.from((String)null));
  }

  @Test
  public void strip_numberOfFieldsSmaller_StrippedVersion() throws Exception {
    // arrange
    final VersionNumber testee = VersionNumber.from("3.14.3.5");
    // act
    final VersionNumber result = testee.strip(1);
    // assert
    assertEquals("3", result.toString());
  }

  @Test
  public void strip_numberOfFieldsSmallerSnapshot_StrippedVersion() throws Exception {
    // arrange
    final VersionNumber testee = VersionNumber.from("3.14-SNAPSHOT");
    // act
    final VersionNumber result = testee.strip(2);
    // assert
    assertEquals("3.14", result.toString());
    assertFalse(result.isSnapshot());
  }

  @Test
  public void strip_numberOfFieldsGreater_DoNotStrip() throws Exception {
    // arrange
    final VersionNumber testee = VersionNumber.from("3.14.3");
    // act
    final VersionNumber result = testee.strip(5);
    // assert
    assertEquals(testee, result);
  }

  @Test
  public void isSnapshot_noSnapshotSuffix_False() throws Exception {
    // arrange
    final VersionNumber testee = VersionNumber.from("3.14.0.1");
    // act
    final boolean result = testee.isSnapshot();
    // assert
    assertFalse(result);
  }

  @Test
  public void isSnapshot_hasSnapshotSuffix_True() throws Exception {
    // arrange
    final VersionNumber testee = VersionNumber.from("3.14-SNAPSHOT");
    // act
    final boolean result = testee.isSnapshot();
    // assert
    assertTrue(result);
  }

  @Test
  public void removeSnapshot_noSnapshotSuffix_Self() throws Exception {
    // arrange
    final VersionNumber testee = VersionNumber.from("3.14.1.2");
    // act
    final VersionNumber result = testee.removeSnapshot();
    // assert
    assertEquals(testee, result);
    assertEquals(testee.getVersionString(), result.getVersionString());
  }

  @Test
  public void removeSnapshot_SnapshotSuffix_SelfWithoutSnapshot() throws Exception {
    // arrange
    final VersionNumber testee = VersionNumber.from("21.07-SNAPSHOT");
    // act
    final VersionNumber result = testee.removeSnapshot();
    // assert
    assertEquals(VersionNumber.from("21.07"), result);
    assertEquals("21.07", result.getVersionString());
  }

  @Test
  public void equalsContract() throws Exception {
    EqualsVerifier.forClass(VersionNumber.class).withIgnoredFields("versionNumberAsString").verify();
  }
}
