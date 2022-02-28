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

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hisp.dhis.integration.icd1x.Constants;
import org.hisp.dhis.integration.icd1x.models.Entity;
import org.springframework.util.StringUtils;

/**
 * This class reads the latest {@link Entity} fetched and adds that to the
 * collection of entities and fills the entityQueue with the child Entities, so
 * it can be picked up by a subsequent iteration in the doWhileLoop of the
 * {@link org.hisp.dhis.integration.icd1x.routes.ICDRouteBuilder}
 */
@SuppressWarnings( "unchecked" )
public class EnqueueEntitiesProcessor implements Processor
{
    private static final Logger LOG = LogManager.getLogger( EnqueueEntitiesProcessor.class );

    @Override
    public void process( Exchange exchange )
    {
        String currentLanguage = exchange.getProperty( Constants.PROPERTY_CURRENT_LANGUAGE, String.class );
        Queue<String> entityQueue = exchange.getProperty( Constants.PROPERTY_ENTITY_ID_QUEUE, Queue.class );
        Map<String, Map<String, Entity>> entities = exchange.getProperty( Constants.PROPERTY_ENTITIES, Map.class );

        String icdVersion = exchange.getProperty( Constants.PROPERTY_ICD_VERSION, String.class );
        int parentOnlyLength = icdVersion.equals( Constants.ICD_11 ) ? 9 : 8;

        Entity entity = exchange.getMessage().getBody( Entity.class );

        if ( exchange.getProperty( Constants.FLAG_ROOT, Boolean.class ) )
        {
            // first one is the root
            entity.setCode( "ROOT" );
        }

        // adding to the entity set, ony if code is present
        if ( StringUtils.hasLength( entity.getCode() ) )
        {
            Map<String, Entity> languagesMap = entities.computeIfAbsent( entity.getCode(), nw -> new HashMap<>() );
            languagesMap.put( currentLanguage, entity );
        }

        if ( entity.getChild() == null )
        {
            return;
        }

        if ( exchange.getProperty( Constants.VERBOSE, Boolean.class ) )
        {
            LOG.info( "Processed entity [{}] {}", entity.getCode(), entity.getTitle().getValue() );
        }

        entityQueue.addAll( entity.getChild().stream().map( url -> {
            String[] split = url.split( "/" );
            if ( split.length == parentOnlyLength )
            {
                return split[split.length - 1];
            }
            else if ( split.length == parentOnlyLength + 1 )
            {
                return split[split.length - 2] + "/" + split[split.length - 1];
            }
            else
            {
                throw new RuntimeException( "Unexpected URL format returned as a child : " + url );
            }
        } ).filter( id -> !id.equals( "unspecified" ) ).collect( Collectors.toList() ) );

        // make child List Garbage Collectible
        entity.setChild( null );
    }
}
