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

import static org.hisp.dhis.integration.icd1x.routes.ICD11RouteBuilder.PROPERTY_ENTITIES;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.hisp.dhis.integration.icd1x.models.Entity;
import org.hisp.dhis.integration.icd1x.models.Option;
import org.hisp.dhis.integration.icd1x.models.OptionSet;

@SuppressWarnings( "unchecked" )
public class ToOptionsProcessor implements Processor
{

    @Override
    public void process( Exchange exchange )
    {
        List<Entity> entities = exchange.getProperty( PROPERTY_ENTITIES, List.class );

        OptionSet optionSet = new OptionSet();
        optionSet.setName( "icd11" );
        optionSet.setValueType( "TEXT" );
        optionSet.setName( entities.remove( 0 ).getTitle().getValue() );

        List<Option> options = entities.stream().map( entity -> {
            Option option = new Option();
            option.setCode( entity.getCode() );
            option.setName( entity.getTitle().getValue() );
            return option;
        } ).collect( Collectors.toList() );

        optionSet.setOptions( options );

        exchange.getMessage().setBody( options );
    }
}
