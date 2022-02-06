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

import static org.hisp.dhis.integration.icd1x.Constants.PROPERTY_AUTH_REQUESTED;
import static org.hisp.dhis.integration.icd1x.Constants.getAsExchangeProperty;

import java.util.Collections;
import java.util.LinkedList;

import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.hisp.dhis.integration.icd1x.Constants;
import org.hisp.dhis.integration.icd1x.config.ICD11CommandConfig;
import org.hisp.dhis.integration.icd1x.models.Entity;
import org.hisp.dhis.integration.icd1x.processors.EnqueueEntitiesProcessor;
import org.hisp.dhis.integration.icd1x.processors.HeadersSetter;
import org.hisp.dhis.integration.icd1x.processors.ToOptionsProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ICD11RouteBuilder extends RouteBuilder
{
    private Processor initRoute()
    {
        return exchange -> {
            ICD11CommandConfig routeConfig = exchange.getProperty( Constants.PROPERTY_COMMAND_CONFIG,
                ICD11CommandConfig.class );

            // extracting properties
            exchange.setProperty( Constants.PROPERTY_HOST, routeConfig.getHost() );
            exchange.setProperty( Constants.PROPERTY_RELEASE, routeConfig.getReleaseId() );
            exchange.setProperty( Constants.PROPERTY_LINEARIZATION, routeConfig.getLinearizationName() );
            exchange.setProperty( Constants.PROPERTY_LANGUAGE, routeConfig.getLanguage() );
            exchange.setProperty( Constants.PROPERTY_OUTPUT_FILE, routeConfig.getFileOut() );
            exchange.setProperty( Constants.VERBOSE, routeConfig.isVerbose() );

            // determine whether auth requested
            exchange.setProperty( Constants.PROPERTY_AUTH_REQUESTED,
                StringUtils.hasLength( routeConfig.getClientId() ) );

            // entity IDs queue
            exchange.setProperty( Constants.PROPERTY_ENTITY_ID_QUEUE,
                new LinkedList<>( Collections.singleton( "" ) ) );
            // list of collected entities
            exchange.setProperty( Constants.PROPERTY_ENTITIES, new LinkedList<>() );
        };
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public void configure()
    {
        // todo parallelize if necessary
        from( "direct:icd11" )
            .routeId( "icd11-route" )
            .log( "Parsing ICD11..." )
            .process( initRoute() )
            // check whether authentication is requested
            // @formatter:off
            .choice()
                .when().simple(getAsExchangeProperty(PROPERTY_AUTH_REQUESTED))
                    .to("direct:icd-auth")
            .end()
            // START DO WHILE
            .loopDoWhile(
                exchange -> !exchange.getProperty( Constants.PROPERTY_ENTITY_ID_QUEUE, LinkedList.class ).isEmpty())
                // read entity id and set as a property
                .setProperty( Constants.PROPERTY_ID )
                .exchange( ex -> ex.getProperty( Constants.PROPERTY_ENTITY_ID_QUEUE, LinkedList.class ).poll() )
                // set headers for the icd11 API call
                .process( new HeadersSetter() )
                .toD(
                    getAsExchangeProperty( Constants.PROPERTY_HOST )
                        + "/icd/release/11/" + getAsExchangeProperty( Constants.PROPERTY_RELEASE )
                        + "/" + getAsExchangeProperty( Constants.PROPERTY_LINEARIZATION )
                        + "/" + getAsExchangeProperty( Constants.PROPERTY_ID ) )
                .choice()
                    // no token expiration. proceed with the Entity
                    .when().simple( "${header.CamelHttpResponseCode} == 200" )
                        .unmarshal().json( Entity.class )
                        .process( new EnqueueEntitiesProcessor() )
                    .when().simple( "${header.CamelHttpResponseCode} == 401" )
                        // the token has been expired
                        .log( "The token has been expired. Refreshing the token..." )
                        .to( "direct:icd-auth" )
                        // putting the failed ID back to the queue
                        .process( exchange -> exchange.getProperty( Constants.PROPERTY_ENTITY_ID_QUEUE, LinkedList.class )
                            .add(0, exchange.getProperty( Constants.PROPERTY_ID ) ) )
                    .otherwise()
                        .log("Unexpected status code ${header.CamelHttpResponseCode}")
                .end()
            // END DO WHILE
            // @formatter:on
            .end()
            .log( "Generating DHIS2 options..." )
            .process( new ToOptionsProcessor() )
            .marshal()
            .json( JsonLibrary.Jackson, false )
            .log( "Writing OptionSets to " + getAsExchangeProperty( Constants.PROPERTY_OUTPUT_FILE ) )
            .toD( "file:" + getAsExchangeProperty( Constants.PROPERTY_OUTPUT_FILE ) + "?charset=utf-8" );
    }
}
