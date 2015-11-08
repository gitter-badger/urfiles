package org.bitionaire.urfiles.resources;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.bitionaire.urfiles.file.ImageFiles;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.imageio.ImageIO;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Slf4j
@Path("/media/icons")
public class IconsResource {

    private final File baseDirectory;

    public IconsResource(final File baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    @GET
    @Path("/{service}/{name}")
    @Produces({ "image/png", "image/jpeg" })
    public StreamingOutput download(@PathParam("service") final String serviceName, @PathParam("name") final String fileName) {
        return outputStream -> {
            try (final FileInputStream inputStream = new FileInputStream(getFileLocation(serviceName, fileName))) {
                int nextByte = 0;
                while ((nextByte = inputStream.read()) != -1) {
                    outputStream.write(nextByte);
                }
                outputStream.flush();
                outputStream.close();
            }
        };
    }

    @POST
    @Path("/{service}/{name}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response upload(@PathParam("service") final String serviceName, @PathParam("name") final String fileName,
                           @Valid @NotNull @FormDataParam("file") final InputStream inputStream,
                           @Valid @NotNull @FormDataParam("file") final FormDataContentDisposition contentDispositionHeader) {
        log.debug("requested to store file {}/{} with header {}", serviceName, fileName, contentDispositionHeader);

        final BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        verifyFileSize(contentDispositionHeader);
        verifyFileType(fileName, bufferedInputStream);
        verifyImageDimensions(bufferedInputStream, contentDispositionHeader.getSize());

        final File file = getFileLocation(serviceName, fileName);
        Preconditions.checkArgument(!file.exists(), "file already exists");

        final boolean stored = storeFile(bufferedInputStream, file);
        if (stored) {
            return Response.created(UriBuilder.fromResource(IconsResource.class).build(serviceName, fileName)).build();
        } else {
            return Response.serverError().build();
        }
    }

    @PUT
    @Path("/{service}/{name}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response overwrite(@PathParam("service") final String serviceName, @PathParam("name") final String fileName,
                              @Valid @NotNull @FormDataParam("file") final InputStream inputStream,
                              @Valid @NotNull @FormDataParam("file") final FormDataContentDisposition contentDispositionHeader) {
        log.debug("requested to overwrite file {}/{} with header {}", serviceName, fileName, contentDispositionHeader);

        final BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        verifyFileSize(contentDispositionHeader);
        verifyFileType(fileName, bufferedInputStream);
        verifyImageDimensions(bufferedInputStream, contentDispositionHeader.getSize());

        final boolean stored = storeFile(bufferedInputStream, getFileLocation(serviceName, fileName));
        if (stored) {
            return Response.ok().build();
        } else {
            return Response.serverError().build();
        }
    }

    private File getFileLocation(final String serviceName, final String fileName) {
        return new File(new File(baseDirectory, serviceName), fileName);
    }

    private boolean storeFile(final InputStream inputStream, final File fileLocation) {
        final File parentDirectory = fileLocation.getParentFile();
        if (!parentDirectory.exists() && !fileLocation.getParentFile().mkdirs()) {
            log.error("could not create directories for file {}", fileLocation.getAbsolutePath());
            return false;
        }

        try {
            Files.copy(inputStream, fileLocation.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (final IOException e) {
            log.error("could not store file " + fileLocation, e);
            return false;
        }
    }

    @DELETE
    @Path("/{service}/{name}")
    public Response delete(@PathParam("service") final String serviceName, @PathParam("name") final String fileName) {
        final File file = getFileLocation(serviceName, fileName);

        if (!file.exists()) {
            log.warn("called delete for non existing file {}", file.getAbsolutePath());
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            if (file.delete()) {
                log.debug("file {} deleted", file.getAbsolutePath());
                return Response.ok().build();
            } else {
                log.warn("could not delete file {}", file.getAbsolutePath());
                return Response.notModified().build();
            }
        }
    }

    private static void verifyFileSize(final FormDataContentDisposition contentDispositionHeader) {
        Preconditions.checkArgument(contentDispositionHeader.getSize() < 2e+6, "file size must be less than 2MB");
    }

    private static void verifyFileType(final String fileName, final BufferedInputStream inputStream) {
        final Optional<ImageFiles.Type> type = ImageFiles.probeType(inputStream);
        Preconditions.checkArgument(type.isPresent(), "file type must be one of PNG or JPEG");
        Preconditions.checkArgument(type.get().getSuffixPattern().matcher(fileName).matches(), "specified file name does not match detected file type");
    }

    private static void verifyImageDimensions(final BufferedInputStream bufferedInputStream, final long fileSize) {
        try {
            bufferedInputStream.mark((int) fileSize + 1);
            final BufferedImage image = ImageIO.read(bufferedInputStream);
            bufferedInputStream.reset();
            Preconditions.checkArgument(image.getHeight() > 128 | image.getWidth() > 128, "image height & width must be greater than 128px");
        } catch (final IOException e) {
            log.error("could not read image dimensions", e);
            throw new IllegalArgumentException("could not read image");
        }
    }


}
