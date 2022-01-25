package org.hisp.dhis.integration.icd1x.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties( ignoreUnknown = true )
public class Title
{
    @JsonProperty( "@value" )
    private String value;
}
