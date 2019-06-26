package me.giannini.misc.helper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This helper is supposed to look for patterns in big text files and convert the matches to something else.
 */
public class FileDateFormatConverter {

  private final Pattern pattern;
  private final Function<String, String> matchConverter;

  /**
   * Constructor
   *
   * @param pattern - the regex used to search for matches
   * @param matchConverter - the converter to apply on each match
   */
  public FileDateFormatConverter(final Pattern pattern, final Function<String, String> matchConverter) {
    this.pattern = pattern;
    this.matchConverter = matchConverter;
  }

  /**
   * Reads the provided {@code input} file and writes its content line by line to the {@code output} while converting the found matches.
   *
   * @param input - the {@link File} to be converted
   * @param output - the {@link File} to write the conversion output
   * @param charsetName - the name of the {@link Charset} to use while reading and writing
   */
  public void convert(final File input, final File output, final String charsetName) throws IOException, UnsupportedEncodingException, FileNotFoundException {
    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), charsetName))) {
      Files.lines(input.toPath(), Charset.forName(charsetName)).forEach(line -> convertAndWriteLine(writer, line));
    }
  }

  private void convertAndWriteLine(final BufferedWriter writer, final String line) {
    final StringBuffer out = new StringBuffer();
    final Matcher matcher = pattern.matcher(line);
    while (matcher.find()) {
      final String match = matcher.group();
      matcher.appendReplacement(out, matchConverter.apply(match));
    }
    matcher.appendTail(out);
    try {
      writer.append(out.toString());
      writer.newLine();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(final String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException {
    final File input = new File("input.txt");
    final File output = new File("output.txt");
    final FileDateFormatConverter converter = new FileDateFormatConverter(MyDateConverter.REG_EX, new MyDateConverter());
    converter.convert(input, output, "Cp1252");
  }

  /**
   * Converts dates found like this {@code \t15-JUL-98} into this {@code \t15-07-1998}. Years &lt; 70 are considered to be from 1900.
   */
  private static class MyDateConverter implements Function<String, String> {

    private static final Pattern REG_EX = Pattern.compile("\\t\\d{2}-[A-Z]{3}-\\d{2}");

    private enum Months {
      JAN,
      FEB,
      MAR,
      APR,
      MAY,
      JUN,
      JUL,
      AUG,
      SEP,
      OCT,
      NOV,
      DEC;
    }

    @Override
    public String apply(final String match) {
      final String[] fields = match.substring(1).split("-");
      final int month = Months.valueOf(fields[1]).ordinal() + 1;
      final int year2Digit = Integer.parseInt(fields[2]);
      final int year = year2Digit >= 70 ? 1900 + year2Digit : 2000 + year2Digit;
      return "\t" + fields[0] + "-" + String.format("%02d", month) + "-" + year;
    }
  }
}
