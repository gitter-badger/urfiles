package org.bitionaire.urfiles.file;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
public class ImageFiles {

    private static final int LONGEST_TYPE_HEADER = Arrays.stream(Type.values()).mapToInt(type -> type.header.length).max().getAsInt();

    public static Optional<Type> probeType(final BufferedInputStream bufferedInputStream) {
        try {
            bufferedInputStream.mark(LONGEST_TYPE_HEADER);
            for (final Type type : Type.values()) {
                final byte[] header = new byte[type.header.length];
                int readBytes = bufferedInputStream.read(header, 0, type.header.length);
                bufferedInputStream.reset();

                if (readBytes != type.header.length) continue;
                if (Arrays.equals(header, type.header)) return Optional.of(type);
            }
        } catch (final IOException e) {
            log.error("could not probe image type", e);
        }
        return Optional.empty();
    }

    public enum Type {
        JPEG ( ".*\\.jpg|jpeg", new byte[] { (byte) 0xFF, (byte) 0xD8 }) ,
        PNG ( ".*\\.png", new byte[] { (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47, (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A });

        @Getter private final Pattern suffixPattern;
        private final byte[] header;

        private Type(final String suffixPattern, final byte[] header) {
            this.suffixPattern = Pattern.compile(suffixPattern, Pattern.CASE_INSENSITIVE);
            this.header = header;
        }
    }
}
