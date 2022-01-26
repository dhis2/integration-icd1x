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

import java.util.Collections;
import java.util.LinkedList;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.hisp.dhis.integration.icd1x.models.Entity;
import org.hisp.dhis.integration.icd1x.processors.EnqueueEntitiesProcessor;
import org.hisp.dhis.integration.icd1x.processors.HeadersSetter;
import org.hisp.dhis.integration.icd1x.processors.ToOptionsProcessor;
import org.springframework.stereotype.Component;

@Component
public class ICD11RouteBuilder extends RouteBuilder
{

    public static final String PROPERTY_ENTITY_ID_QUEUE = "entityIdsQueue";

    public static final String PROPERTY_ENTITIES = "entities";

    @Override
    public void configure()
    {

        final HeadersSetter headersSetter = new HeadersSetter( "v2", "en" );

        // todo parallelize if necessary
        from( "direct:icd11" )
            .routeId( "icd11-route" )
            .log( "Parsing ICD11..." )
            .setProperty( PROPERTY_ENTITY_ID_QUEUE )
            .exchange( exchange -> new LinkedList<>( Collections.singleton( "" ) ) )
            .setProperty( PROPERTY_ENTITIES ).exchange( exchange -> new LinkedList<>() )
            .loopDoWhile( exchange -> !exchange.getProperty( PROPERTY_ENTITY_ID_QUEUE, LinkedList.class ).isEmpty() )
            .setProperty( "id" ).exchange( ex -> ex.getProperty( PROPERTY_ENTITY_ID_QUEUE, LinkedList.class ).poll() )
            .process( headersSetter )
            .toD( "http://localhost/icd/release/11/2021-05/mms/${exchangeProperty.id}" )
            .unmarshal()
            .json( Entity.class )
            .process( new EnqueueEntitiesProcessor() )
            .end()
            .process( new ToOptionsProcessor() )
            .marshal()
            .json( JsonLibrary.Jackson, false )
            .log( "Writing OptionSets to the file..." )
            .to( "file:/tmp/options.json?charset=utf-8" )
            .end();

    }
}
