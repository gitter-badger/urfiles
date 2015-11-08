package org.bitionaire.urfiles;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import lombok.Getter;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.File;

@ToString
public class UrfilesConfiguration extends Configuration {

    @Valid @NotNull
    @JsonProperty("baseDirectory")
    @Getter  private File baseDirectory;

}
