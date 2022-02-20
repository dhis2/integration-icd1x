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

import static org.hisp.dhis.integration.icd1x.Constants.PROPERTY_ENTITIES;
import static org.hisp.dhis.integration.icd1x.Constants.PROPERTY_ICD_VERSION;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.hisp.dhis.integration.icd1x.Constants;
import org.hisp.dhis.integration.icd1x.models.*;

@SuppressWarnings( "unchecked" )
public class ToOptionsProcessor implements Processor
{

    private static List<Translation> toTranslations( Map<String, Entity> entities )
    {
        if ( entities.isEmpty() )
        {
            return null;
        }
        return entities.entrySet().stream().map( entry -> {
            Translation translation = new Translation();
            translation.setProperty( Constants.TRANSLATION_PROPERTY_NAME );
            translation.setLocale( entry.getKey() );
            translation.setValue( entry.getValue().getTitle().getValue() );
            return translation;
        } ).collect( Collectors.toList() );
    }

    @Override
    public void process( Exchange exchange )
    {
        // Map<OPT_CODE, Map<LANG_CODE, Entity>>
        LinkedHashMap<String, Map<String, Entity>> entities = exchange.getProperty(
            PROPERTY_ENTITIES, LinkedHashMap.class );
        String icdVersion = exchange.getProperty( PROPERTY_ICD_VERSION, String.class );

        MetadataPayload metadataPayload = new MetadataPayload();

        Iterator<Map.Entry<String, Map<String, Entity>>> entitiesItr = entities.entrySet().iterator();

        Map<String, Entity> entries = entitiesItr.next().getValue();
        Entity englishEntry = entries.remove( Constants.LANGUAGE_ENGLISH );

        OptionSet optionSet = new OptionSet();
        optionSet.setValueType( "TEXT" );
        optionSet.setCode( String.format( "icd%s", icdVersion ) );
        optionSet.setName( englishEntry.getTitle().getValue() );

        optionSet.setTranslations( toTranslations( entries ) );

        List<Option> options = new ArrayList<>();

        while ( entitiesItr.hasNext() )
        {
            entries = entitiesItr.next().getValue();
            englishEntry = entries.remove( Constants.LANGUAGE_ENGLISH );

            Option option = new Option();
            option.setCode( englishEntry.getCode() );
            option.setName( englishEntry.getTitle().getValue() );

            List<Translation> translations = toTranslations( entries );
            option.setTranslations( translations );
            options.add( option );
        }

        // remove name to make the payload less bulky
        optionSet.setOptions( options.stream().map( option -> {
            Option op = new Option();
            op.setCode( option.getCode() );
            return op;
        } ).collect( Collectors.toList() ) );

        metadataPayload.setOptionSets( Collections.singletonList( optionSet ) );
        metadataPayload.setOptions( options );

        exchange.getMessage().setBody( metadataPayload );
    }
}
