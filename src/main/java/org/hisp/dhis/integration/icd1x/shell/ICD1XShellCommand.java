/*
 * Copyright (c) 2004-2022, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.integration.icd1x.shell;

import org.apache.camel.ProducerTemplate;
import org.hisp.dhis.integration.icd1x.Constants;
import org.hisp.dhis.integration.icd1x.config.ICD11CommandConfig;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StringUtils;

@ShellComponent
public class ICD1XShellCommand
{
    private final ProducerTemplate producerTemplate;

    public ICD1XShellCommand( ProducerTemplate producerTemplate )
    {
        this.producerTemplate = producerTemplate;
    }

    @SuppressWarnings( "unused" )
    @ShellMethod( "Generate DHIS2 OptionsSet with ICD10 codes and saves the output to a file" )
    public void ic10()
    {

    }

    @SuppressWarnings( "unused" )
    @ShellMethod( "Generate DHIS2 OptionsSet with ICD11 codes and saves the output to a file" )
    public void icd11(
        @ShellOption( defaultValue = "", help = "ICD Entity ID to start with" ) String rootId,
        @ShellOption( defaultValue = "2021-05", help = "ICD 11 Release Id. One of 2021-05, 2020-09, 2019-04, 2018" ) String releaseId,
        @ShellOption( defaultValue = "mms", help = "Short name for the linearization. e.g. mms for ICD Mortality and Morbidity Statistics" ) String linearizationName,
        @ShellOption( defaultValue = "en", help = "Language for entity descriptions. One of ar, en, es, zh" ) String language,
        @ShellOption( defaultValue = "http://localhost", help = "Host of the ICD11 repository. The default value works with docker approach" ) String host,
        @ShellOption( defaultValue = "The client id to be used with the publicly hosted icd1 repository" ) String clientId,
        @ShellOption( defaultValue = "The client secret to be used with the publicly hosted icd1 repository" ) String clientSecret,
        @ShellOption( defaultValue = "options.json", help = "Path to the output file" ) String fileOut,
        @ShellOption( help = "Indicates whether progress should be displayed verbosely" ) boolean verbose )
    {
        ICD11CommandConfig icd11CommandConfig = new ICD11CommandConfig();
        icd11CommandConfig.setRootId( rootId );
        icd11CommandConfig.setHost( host );
        icd11CommandConfig.setClientId( clientId );
        icd11CommandConfig.setClientSecret( clientSecret );
        icd11CommandConfig.setLinearizationName( linearizationName );
        icd11CommandConfig.setReleaseId( releaseId );
        icd11CommandConfig.setLanguage( language );
        icd11CommandConfig.setFileOut( fileOut );
        icd11CommandConfig.setVerbose( verbose );

        boolean useAuth = StringUtils.hasLength( icd11CommandConfig.getClientId() );
        if ( useAuth && !StringUtils.hasLength( icd11CommandConfig.getClientSecret() ) )
        {
            throw new RuntimeException( "Client Secret is required" );
        }

        // todo validate host and other validation

        producerTemplate.sendBodyAndProperty( "direct:icd11", null, Constants.PROPERTY_COMMAND_CONFIG,
            icd11CommandConfig );
    }
}
