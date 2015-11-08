package org.bitionaire.urfiles.file;

import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Optional;

import static org.junit.Assert.*;

import static io.dropwizard.testing.ResourceHelpers.*;

public class ImageFilesTest {

    @Test
    public void testProbePNG() throws Exception {
        final File file = new File(resourceFilePath("upload/bitionaire.png"));
        final Optional<ImageFiles.Type> type = ImageFiles.probeType(new BufferedInputStream(new FileInputStream(file)));
        assertTrue(type.isPresent());
        assertEquals(ImageFiles.Type.PNG, type.get());
    }

    @Test
    public void testProbeJPEG() throws Exception {
        final File file = new File(resourceFilePath("upload/bitionaire.jpg"));
        final Optional<ImageFiles.Type> type = ImageFiles.probeType(new BufferedInputStream(new FileInputStream(file)));
        assertTrue(type.isPresent());
        assertEquals(ImageFiles.Type.JPEG, type.get());
    }

    @Test
    public void testProbeZIP() throws Exception {
        final File file = new File(resourceFilePath("upload/bitionaire.zip"));
        final Optional<ImageFiles.Type> type = ImageFiles.probeType(new BufferedInputStream(new FileInputStream(file)));
        assertFalse(type.isPresent());
    }

    @Test
    public void testProbeMultipleTimes() throws Exception {
        final File file = new File(resourceFilePath("upload/bitionaire.jpg"));
        final BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));

        assertEquals(ImageFiles.Type.JPEG, ImageFiles.probeType(inputStream).orElse(null));
        assertEquals(ImageFiles.Type.JPEG, ImageFiles.probeType(inputStream).orElse(null));
    }
}