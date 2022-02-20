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
package org.hisp.dhis.integration.icd1x;

public class Constants
{
    public static final String PROPERTY_ENTITY_ID_QUEUE = "entityIdsQueue";

    public static final String PROPERTY_ENTITIES = "entities";

    public static final String PROPERTY_LANGUAGES = "languages";

    public static final String PROPERTY_CURRENT_LANGUAGE = "current_language";

    public static final String PROPERTY_OUTPUT_FILE = "file";

    public static final String PROPERTY_HOST = "host";

    public static final String PROPERTY_LINEARIZATION = "linearization";

    public static final String PROPERTY_RELEASE = "release";

    public static final String PROPERTY_ID = "id";

    public static final String VERBOSE = "verbose";

    public static final String PROPERTY_AUTH_RESPONSE = "auth_response";

    public static final String PROPERTY_AUTH_REQUESTED = "auth_requested";

    public static final String PROPERTY_ICD_VERSION = "icd_version";

    public static final String PROPERTY_ROOT_ID = "root_id";

    public static final String ICD_11 = "11";

    public static final String ICD_10 = "10";

    public static final String HEADER_API_VERSION = "API-Version";

    public static final String LANGUAGE_ENGLISH = "en";

    public static final String TRANSLATION_PROPERTY_NAME = "NAME";

    // flags the root Entity to the EnqueueEntityProcessor
    public static final String FLAG_ROOT = "root";

    /**
     * Configurations collected from the command line
     */
    public static final String PROPERTY_COMMAND_CONFIG = "command_config";

    public static String getAsExchangeProperty( String property )
    {
        return String.format( "${exchangeProperty.%s}", property );
    }
}
