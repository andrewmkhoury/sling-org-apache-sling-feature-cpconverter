/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.sling.feature.cpconverter.handlers;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Dictionary;

import org.apache.jackrabbit.vault.fs.io.Archive;
import org.apache.jackrabbit.vault.fs.io.Archive.Entry;
import org.apache.sling.feature.ArtifactId;
import org.apache.sling.feature.Configuration;
import org.apache.sling.feature.Feature;
import org.apache.sling.feature.cpconverter.ContentPackage2FeatureModelConverter;
import org.apache.sling.feature.io.json.FeatureJSONReader;
import org.apache.sling.feature.io.json.FeatureJSONWriter;
import org.junit.Test;

public class ConfigEntryHandlerTest {

    @Test
    public void collectionValuesIncluded() throws Exception {
        String resourceConfiguration = "jcr_root/apps/asd/config/org.apache.jackrabbit.oak.spi.security.authentication.external.impl.DefaultSyncHandler~ims.config";

        Archive archive = mock(Archive.class);
        Entry entry = mock(Entry.class);

        when(entry.getName()).thenReturn(resourceConfiguration.substring(resourceConfiguration.lastIndexOf('/') + 1));
        when(archive.openInputStream(entry)).thenReturn(getClass().getResourceAsStream(resourceConfiguration));

        Feature expected = new Feature(new ArtifactId("org.apache.sling", "org.apache.sling.cp2fm", "0.0.1", null, null));
        ContentPackage2FeatureModelConverter converter = spy(ContentPackage2FeatureModelConverter.class);
        when(converter.getTargetFeature()).thenReturn(expected);

        new ConfigurationEntryHandler().handle(resourceConfiguration, archive, entry, converter);

        verifyConfiguration(expected);

        StringWriter writer = new StringWriter();
        FeatureJSONWriter.write(writer, expected);

        Feature current = FeatureJSONReader.read(new StringReader(writer.toString()), "fake");
        verifyConfiguration(current);
    }

    private void verifyConfiguration(Feature feature) {
        Configuration configuration = feature.getConfigurations().getConfiguration("org.apache.jackrabbit.oak.spi.security.authentication.external.impl.DefaultSyncHandler~ims");
        assertNotNull(configuration);

        Dictionary<String, Object> configurationProperties = configuration.getConfigurationProperties();
        assertNotNull(configurationProperties);
        assertFalse(configurationProperties.isEmpty());
        assertEquals(1, configurationProperties.get("user.membershipNestingDepth"));
        assertArrayEquals(new Object[] { "oauth/oauthid-stage=profile/id", "profile/app-stage=access_token" }, (Object[]) configurationProperties.get("user.propertyMapping"));
        assertArrayEquals(new String[] { "Administrators" }, (String[]) configurationProperties.get("user.autoMembership"));
        assertEquals("ims", configurationProperties.get("handler.name"));
        assertEquals("ims", configurationProperties.get("user.pathPrefix"));
        assertTrue((boolean) configurationProperties.get("user.disableMissing"));
    }

}