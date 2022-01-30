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
import org.hisp.dhis.integration.icd1x.config.ICD11RouteConfig;
import org.hisp.dhis.integration.icd1x.models.OAuthResponse;
import org.hisp.dhis.integration.icd1x.routes.ICDAuthRouteBuilder;
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
    @ShellMethod( "Generate DHIS2 OptionsSet with ICD11 codes" )
    public void icd11(
        @ShellOption( defaultValue = "", help = "ICD Entity ID to start with" ) String rootId,
        @ShellOption( defaultValue = "2021-05", help = "ICD 11 Release Id. One of 2021-05, 2020-09, 2019-04, 2018" ) String releaseId,
        @ShellOption( defaultValue = "mms" ) String linearizationName,
        @ShellOption( defaultValue = "en", help = "Language for entity descriptions. One of ar, en, es, zh" ) String language,
        @ShellOption( defaultValue = "http://localhost" ) String host,
        @ShellOption( defaultValue = "" ) String clientId,
        @ShellOption( defaultValue = "" ) String clientSecret,
        @ShellOption( defaultValue = "options.json" ) String fileOut )
    {
        ICD11RouteConfig icd11RouteConfig = new ICD11RouteConfig();
        icd11RouteConfig.setRootId( rootId );
        icd11RouteConfig.setHost( host );
        icd11RouteConfig.setClientId( clientId );
        icd11RouteConfig.setClientSecret( clientSecret );
        icd11RouteConfig.setLinearizationName( linearizationName );
        icd11RouteConfig.setReleaseId( releaseId );
        icd11RouteConfig.setLanguage( language );
        icd11RouteConfig.setFileOut( fileOut );

        boolean useAuth = StringUtils.hasLength( icd11RouteConfig.getClientId() );
        if ( useAuth && !StringUtils.hasLength( icd11RouteConfig.getClientSecret() ) )
        {
            throw new RuntimeException( "Client Secret is required" );
        }

        // todo validate host and other validation

        if ( useAuth )
        {
            OAuthResponse oAuthResponse = producerTemplate.requestBody( "direct:icd-auth", icd11RouteConfig,
                OAuthResponse.class );
            producerTemplate.sendBodyAndProperty( "direct:icd11", icd11RouteConfig,
                ICDAuthRouteBuilder.PROPERTY_AUTH,
                oAuthResponse );
        }
        else
        {
            // using ICD11 Docker Container
            producerTemplate.sendBody( "direct:icd11", icd11RouteConfig );
        }
    }
}