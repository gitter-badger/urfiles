package org.bitionaire.urfiles.resources;

import io.dropwizard.testing.junit.DropwizardAppRule;
import org.bitionaire.urfiles.UrfilesApplication;
import org.bitionaire.urfiles.UrfilesConfiguration;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.*;
import static io.dropwizard.testing.ResourceHelpers.*;

@SuppressWarnings("Duplicates")
public class IconsResourceTest {

    @ClassRule
    public static final DropwizardAppRule<UrfilesConfiguration> RULE = new DropwizardAppRule<>(UrfilesApplication.class, resourceFilePath("test.yml"));

    private final File uploadDirectory = new File("/tmp/elbombillo/urfiles/media/users");

    @SuppressWarnings("ConstantConditions")
    @Before
    public void cleanFiles() throws Exception {
        if (uploadDirectory.exists() && uploadDirectory.isDirectory()) {
            Arrays.stream(uploadDirectory.listFiles()).forEach(File::delete);
        }
    }

    @Test
    public void testUploadPNG() throws Exception {
        final String image = "bitionaire.png";
        final FormDataMultiPart form = getFormDataMultiPart(image);

        final Response response = post(image, form);
        assertEquals(Response.Status.Family.SUCCESSFUL, response.getStatusInfo().getFamily());
        assertTrue(new File(uploadDirectory, image).exists());
    }

    @Test
    public void testUploadJPEG() throws Exception {
        final String image = "bitionaire.jpg";
        final FormDataMultiPart form = getFormDataMultiPart(image);

        final Response response = post(image, form);
        assertEquals(Response.Status.Family.SUCCESSFUL, response.getStatusInfo().getFamily());
        assertTrue(new File(uploadDirectory, image).exists());
    }

    @Test
    public void testTwoTimeUpload() throws Exception {
        final String image = "bitionaire.jpg";
        final FormDataMultiPart form = getFormDataMultiPart(image);

        post(image, form);
        final Response response = post(image, form);
        assertEquals(Response.Status.Family.CLIENT_ERROR, response.getStatusInfo().getFamily());
    }

    @Test
    public void testTwoTimeUploadOverride() throws Exception {
        final String image = "bitionaire.jpg";
        final FormDataMultiPart form = getFormDataMultiPart(image);

        post(image, form);
        final Response response = put(image, form);
        assertEquals(Response.Status.Family.SUCCESSFUL, response.getStatusInfo().getFamily());
    }

    @Test
    public void testDeletion() throws Exception {
        final String image = "bitionaire.png";
        final FormDataMultiPart form = getFormDataMultiPart(image);

        post(image, form);
        final Response response = delete(image);
        assertEquals(Response.Status.Family.SUCCESSFUL, response.getStatusInfo().getFamily());
        assertFalse(new File(uploadDirectory, image).exists());
    }

    @Test
    public void testUploadZIP() throws Exception {
        final FormDataMultiPart form = getFormDataMultiPart("bitionaire.zip");

        final Response response = post("bitionaire.zip", form);
        assertEquals(Response.Status.Family.CLIENT_ERROR, response.getStatusInfo().getFamily());
    }

    @Test
    public void testUpAndDownload() throws Exception {
        final String image = "bitionaire.jpg";
        final FormDataMultiPart form = getFormDataMultiPart(image);

        post(image, form);
        final Optional<File> downloadedFile = get(image);

        assertTrue(downloadedFile.isPresent());

        final byte[] uploadedBytes = Files.readAllBytes(new File(uploadDirectory, image).toPath());
        final byte[] downloadedBytes = Files.readAllBytes(downloadedFile.get().toPath());

        assertArrayEquals(uploadedBytes, downloadedBytes);
    }

    private static Response post(final String file, final FormDataMultiPart form) {
        return getClient().target(String.format("http://localhost:%d/media/icons/users/%s", RULE.getLocalPort(), file)).request().post(Entity.entity(form, form.getMediaType()));
    }

    private static Response put(final String file, final FormDataMultiPart form) {
        return getClient().target(String.format("http://localhost:%d/media/icons/users/%s", RULE.getLocalPort(), file)).request().put(Entity.entity(form, form.getMediaType()));
    }

    private static Response delete(final String file) {
        return getClient().target(String.format("http://localhost:%d/media/icons/users/%s", RULE.getLocalPort(), file)).request().delete();
    }

    private Optional<File> get(final String file) {
        final Response response = getClient().target(String.format("http://localhost:%d/media/icons/users/%s", RULE.getLocalPort(), file)).request().get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            try (final InputStream stream = response.readEntity(InputStream.class)) {

                final File downloadFile = File.createTempFile("download", null, uploadDirectory);
                Files.copy(stream, downloadFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                return Optional.of(downloadFile);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return Optional.empty();
        }
    }

    private static FormDataMultiPart getFormDataMultiPart(final String file) {
        final File uploadFile = new File(resourceFilePath(String.format("upload/%s", file)));
        final FormDataMultiPart form = new FormDataMultiPart();
        form.bodyPart(new FileDataBodyPart("file", uploadFile));
        return form;
    }

    private static Client getClient() {
        final ClientConfig clientConfig = new ClientConfig();
        clientConfig.connectorProvider(new HttpUrlConnectorProvider());
        clientConfig.register(MultiPartFeature.class);

        final Client client = ClientBuilder.newClient(clientConfig).register(MultiPartFeature.class);
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        return client;
    }

}