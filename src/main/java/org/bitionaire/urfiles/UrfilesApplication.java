package org.bitionaire.urfiles;

import io.dropwizard.Application;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.extern.slf4j.Slf4j;
import org.bitionaire.urfiles.resources.IconsResource;
import org.bitionaire.urfiles.resources.exception.IllegalArgumentExceptionMapper;

import java.util.Arrays;

@Slf4j
public class UrfilesApplication extends Application<UrfilesConfiguration> {

    public static void main(final String... args) {
        try {
            log.debug("started application with arguments {}", Arrays.toString(args));
            new UrfilesApplication().run(args);
        } catch (final Exception e) {
            log.error("running application failed", e);
        }
    }

    @Override
    public void initialize(final Bootstrap<UrfilesConfiguration> bootstrap) {
        bootstrap.addBundle(new MultiPartBundle());
        log.debug("added multi part bundle");
    }

    @Override
    public void run(final UrfilesConfiguration configuration, final Environment environment) throws Exception {
        log.info("starting application with configuration {}", configuration);

        environment.jersey().register(new IllegalArgumentExceptionMapper());
        environment.jersey().register(new IconsResource(configuration.getBaseDirectory()));

        log.info("all resources registered");
    }

}
