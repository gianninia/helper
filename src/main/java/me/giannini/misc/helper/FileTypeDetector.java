package me.giannini.misc.helper;

import com.google.common.io.ByteSource;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * A detector that tries to guess the file type of a given {@link ByteSource} by reading the first few bytes and comparing to a signature table. The signatures used are from here
 * <a href="https://www.garykessler.net/library/file_sigs.html">https://www.garykessler.net/library/file_sigs.html</a>. Not all signatures and types are supported, only the ones
 * defined in {@link FileType}
 */
public class FileTypeDetector {

    /**
     * Enum representing the supported file types of this detector
     */
    public enum FileType {

        /*
         * Since byte is a signed type in java, it's more readable to use integers to define the literals for each byte and convert them to bytes, otherwise we would have to cast all
         * values that have the first bit set with (byte) or use a negative sign which is quite confusing. For example 0x89 would have to be either (byte) 0x89 or -0x77.
         */
        PNG(
                "png",
                new int[]{0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A} // \u2030PNG....
        ),
        JPG(
                "jpg",
                new int[]{0xFF, 0xD8} // ÿØ
        ),
        GIF(
                "gif",
                new int[]{0x47, 0x49, 0x46, 0x38, 0x37, 0x61}, // GIF87a
                new int[]{0x47, 0x49, 0x46, 0x38, 0x39, 0x61} // GIF89a
        ),
        BMP(
                "bmp",
                new int[]{0x42, 0x4D} // BM
        ),
        PDF(
                "pdf",
                new int[]{0x25, 0x50, 0x44, 0x46} // %PDF
        ),
        ZIP(
                "zip",
                new int[]{0x50, 0x4B, 0x03, 0x04}, // PK..
                new int[]{0x50, 0x4B, 0x07, 0x08}, // PK..
                new int[]{0x50, 0x4B, 0x4C, 0x49, 0x54, 0x45}, // PKLITE
                new int[]{0x50, 0x4B, 0x53, 0x70, 0x58} // PKSFX
        );

        private final String extension;
        private final List<byte[]> masks;

        FileType(final String extension, final int[]... firstBytes) {
            this.extension = extension;
            this.masks = Arrays.stream(firstBytes).map(FileType::convert).collect(toList());
        }

        private static byte[] convert(final int[] from) {
            final byte[] result = new byte[from.length];
            for (int i = 0; i < from.length; i++) {
                result[i] = (byte) from[i];
            }
            return result;
        }

        private int maxMaskLength() {
            return masks.stream().mapToInt(a -> a.length).max().orElse(0);
        }

        public String getExtension() {
            return extension;
        }

        private boolean matches(final byte[] source) {
            for (final byte[] mask : masks) {
                final byte[] sourceMask = Arrays.copyOfRange(source, 0, mask.length);
                if (Arrays.equals(sourceMask, mask)) {
                    return true;
                }
            }
            return false;
        }

        public static FileType fromExtension(final String extension) {
            return Stream.of(FileType.values())
                    .filter(type -> type.getExtension().equalsIgnoreCase(extension))
                    .findFirst()
                    .orElse(null);
        }
    }

    private static final int MAX_MASK_LENGTH = Stream.of(FileType.values()).mapToInt(FileType::maxMaskLength).max().orElse(0);

    /**
     * Tries to detect the type of the data contained in the passed {@link ByteSource} and delivers the corresponding {@link FileType}.
     *
     * @param source the data to be checked
     * @return the corresponding {@link FileType} if the detection was successful, otherwise null
     */
    public static FileType detect(final ByteSource source) {
        final byte[] buffer = new byte[MAX_MASK_LENGTH];
        read(source, buffer);
        for (final FileType type : FileType.values()) {
            if (type.matches(buffer)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Tries to detect the type of the data contained in the passed file and delivers the corresponding {@link FileType} if found.
     *
     * @param file containing the data to be checked
     * @return the corresponding {@link FileType} if the detection was successful, otherwise null
     */
    public static FileType detect(final File file) {
        return detect(Files.asByteSource(file));
    }

    /**
     * Tries to detect the type of the data contained in the passed {@link ByteSource} and delivers the corresponding file extension if found.
     *
     * @param source the data to be checked
     * @return the corresponding file extension if the detection was successful, otherwise null
     */
    public static String detectExtension(final ByteSource source) {
        return Optional.ofNullable(detect(source))
                .map(FileType::getExtension)
                .orElse(null);
    }

    /**
     * Tries to detect the type of the data contained in the passed file and delivers the corresponding actual file extension if found.
     *
     * @param file containing the data to be checked
     * @return the corresponding file extension if the detection was successful, otherwise null
     */
    public static String detectExtension(final File file) {
        return detectExtension(Files.asByteSource(file));
    }

    private static void read(final ByteSource source, final byte[] buffer) {
        try (InputStream in = source.openStream()) {
            in.read(buffer);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to read byte source", e);
        }
    }

}
