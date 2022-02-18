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

import java.io.*;
import java.util.Objects;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.hisp.dhis.integration.icd1x.config.ICDCommandConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.shell.jline.InteractiveShellApplicationRunner;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.google.common.io.Files;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

@SpringBootTest( properties = {
    InteractiveShellApplicationRunner.SPRING_SHELL_INTERACTIVE_ENABLED + "=false"
} )
@CamelSpringBootTest
@Testcontainers
// @UseAdviceWith
public class ICD11MMSTest
{

    private static final Network network = Network.newNetwork();

    @Autowired
    private ProducerTemplate producerTemplate;

    @Container
    private static final GenericContainer<?> icd11Container = new GenericContainer<>( "whoicd/icd-api" )
        .withEnv( "acceptLicense", "true" )
        .withExposedPorts( 80 )
        .withNetwork( network )
        .waitingFor( Wait.forHttp( "/" ).forStatusCode( 404 ) );

    @Test
    public void test11Route()
        throws IOException
    {
        String absolutePath = Files.createTempDir().getAbsolutePath();
        ICDCommandConfig icdCommandConfig = new ICDCommandConfig(
            Constants.ICD_11 );
        icdCommandConfig.setReleaseId( "2021-05" );
        icdCommandConfig.setLinearizationName( "mms" );
        icdCommandConfig.setHost(
            String.format( "http://%s:%s", icd11Container.getHost(),
                icd11Container.getFirstMappedPort() ) );
        icdCommandConfig.setRootId( "" );
        icdCommandConfig.setLanguage( "en" );
        icdCommandConfig.setFileOut( absolutePath );
        icdCommandConfig.setVerbose( true );

        producerTemplate.sendBodyAndProperty( "direct:icd", null,
            Constants.PROPERTY_COMMAND_CONFIG,
            icdCommandConfig );

        Reader reader1 = new BufferedReader(
            new InputStreamReader(
                Objects.requireNonNull( getClass().getClassLoader().getResourceAsStream( "optionset.json" ) ) ) );
        Reader reader2 = new BufferedReader( new FileReader( new File( absolutePath, "optionset.json" ) ) );

        Assertions.assertTrue( IOUtils.contentEqualsIgnoreEOL( reader1, reader2 ) );
    }
}
