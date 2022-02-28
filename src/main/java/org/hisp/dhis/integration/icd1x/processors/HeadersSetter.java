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
package org.hisp.dhis.integration.icd1x.processors;

import static org.hisp.dhis.integration.icd1x.Constants.PROPERTY_CURRENT_LANGUAGE;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.http.HttpMethods;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.hisp.dhis.integration.icd1x.Constants;
import org.hisp.dhis.integration.icd1x.models.OAuthResponse;

/**
 * This {@link Processor} sets all the headers that are necessary to do a ICD
 * API call.
 */
public class HeadersSetter implements Processor
{
    @Override
    public void process( Exchange exchange )
    {
        exchange.getMessage().setHeader( Constants.HEADER_API_VERSION, "v2" );
        exchange.getMessage().setHeader( HttpHeaders.ACCEPT_LANGUAGE,
            exchange.getProperty( PROPERTY_CURRENT_LANGUAGE ) );
        exchange.getMessage().setHeader( Exchange.HTTP_METHOD, HttpMethods.GET );
        exchange.getMessage().setHeader( HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType() );

        OAuthResponse auth = exchange.getProperty( Constants.PROPERTY_AUTH_RESPONSE, OAuthResponse.class );
        if ( auth != null )
        {
            exchange.getMessage().setHeader( HttpHeaders.AUTHORIZATION, "Bearer " + auth.getAccessToken() );
        }
    }
}
