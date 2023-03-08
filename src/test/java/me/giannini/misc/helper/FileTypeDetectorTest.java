package me.giannini.misc.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;

import me.giannini.misc.helper.FileTypeDetector.FileType;

public class FileTypeDetectorTest {

  @ParameterizedTest
  @EnumSource(TestFiles.class)
  public void detectExtension_differentFileTypes_expectedExtension(final TestFiles testFile) throws Exception {
    // act
    final String result = FileTypeDetector.detectExtension(testFile.asByteSource());
    // assert
    assertEquals(testFile.getExtension(), result);
  }

  @ParameterizedTest
  @EnumSource(TestFiles.class)
  public void detect_differentFileTypes_expectedType(final TestFiles testFile) throws Exception {
    // act
    final FileType result = FileTypeDetector.detect(testFile.asByteSource());
    // assert
    assertEquals(FileType.fromExtension(testFile.getExtension()), result);
  }

  public enum TestFiles {

    PNG_IMAGE("small_pic", "png", "files/test_png"),
    JPG_IMAGE("small_pic", "jpg", "files/test_jpg"),
    GIF_IMAGE("small_pic", "gif", "files/test_gif"),
    BMP_IMAGE("small_pic", "bmp", "files/test_bmp"),
    PDF_DOCUMENT("test", "pdf", "files/test_pdf"),
    ZIP_ARCHIVE("test", "zip", "files/test_zip");

    private final String filename;
    private final String extension;
    private final String resourceName;

    private TestFiles(final String filename, final String extension, final String resourceName) {
      this.filename = filename;
      this.extension = extension;
      this.resourceName = resourceName;
    }

    private URL resourceURL() {
      return Thread.currentThread().getContextClassLoader().getResource(resourceName);
    }

    public InputStream openInputStream() {
      return Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
    }

    public ByteSource asByteSource() {
      return Resources.asByteSource(resourceURL());
    }

    public byte[] asByteArray() {
      try {
        return ByteStreams.toByteArray(openInputStream());
      } catch (final IOException e) {
        throw new RuntimeException(e);
      }
    }

    public String getFilename() {
      return filename;
    }

    public String getExtension() {
      return extension;
    }

  }
}
