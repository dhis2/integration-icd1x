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
package org.hisp.dhis.integration.icd1x.routes;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.hisp.dhis.integration.icd1x.config.ICD11RouteConfig;
import org.hisp.dhis.integration.icd1x.models.OAuthResponse;
import org.springframework.stereotype.Component;

@Component
public class ICDAuthRouteBuilder extends RouteBuilder
{
    public static final String PROPERTY_AUTH = "auth";

    @Override
    public void configure()
        throws Exception
    {
        from( "direct:icd-auth" )
            .setHeader( Exchange.CONTENT_TYPE )
            .simple( "application/x-www-form-urlencoded" )
            .setHeader( "Accept" )
            .simple( "application/json" )
            .setBody( exchange -> {
                ICD11RouteConfig config = exchange.getMessage().getBody( ICD11RouteConfig.class );
                return "grant_type=client_credentials&client_id=" + config.getClientId()
                    + "&client_secret=" + config.getClientSecret() + "&scope=icdapi_access";
            } )
            .setHeader( Exchange.HTTP_METHOD )
            .simple( "POST" )
            .log( "Authenticating..." )
            .to( "https://icdaccessmanagement.who.int/connect/token" )
            .unmarshal().json( OAuthResponse.class )
            .end();
    }
}
