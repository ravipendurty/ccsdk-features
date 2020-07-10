/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 */
package org.onap.ccsdk.features.sdnr.wt.dataprovider.yangtools;

import java.io.IOException;
import javax.annotation.Nullable;

import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.credentials.Credentials;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder.Value;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;

/**
 * YangToolsMapper is a specific Jackson mapper configuration for opendaylight yangtools serialization or
 * deserialization of DataObject to/from JSON TODO ChoiceIn and Credentials deserialization only for
 * LoginPasswordBuilder
 */
public class YangToolsMapper extends ObjectMapper {

    private final Logger LOG = LoggerFactory.getLogger(YangToolsMapper.class);
    private static final long serialVersionUID = 1L;
    private static BundleContext context;

    public YangToolsMapper() {
        super();
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);
        setSerializationInclusion(Include.NON_NULL);
        setAnnotationIntrospector(new YangToolsBuilderAnnotationIntrospector());
        SimpleModule dateAndTimeSerializerModule = new SimpleModule();
        dateAndTimeSerializerModule.addSerializer(DateAndTime.class, new CustomDateAndTimeSerializer());
        registerModule(dateAndTimeSerializerModule);
        Bundle bundle = FrameworkUtil.getBundle(YangToolsMapper.class);
        context = bundle != null ? bundle.getBundleContext() : null;
    }

    @Override
    public String writeValueAsString(Object value) throws JsonProcessingException {
        // TODO Auto-generated method stub
        return super.writeValueAsString(value);
    }

    /**
     * Get Builder object for yang tools interface.
     * 
     * @param <T> yang-tools base datatype
     * @param clazz class with interface.
     * @return builder for interface or null if not existing
     */
    @SuppressWarnings("unchecked")
    public @Nullable <T extends DataObject> Builder<T> getBuilder(Class<T> clazz) {
        String builder = clazz.getName() + "Builder";
        try {
            Class<?> clazzBuilder = findClass(builder);
            return (Builder<T>) clazzBuilder.newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            LOG.debug("Problem ", e);
            return null;
        }
    }

    /**
     * Callback for handling mapping failures.
     * 
     * @return
     */
    public int getMappingFailures() {
        return 0;
    }

    /**
     * Provide mapping of string to attribute names, generated by yang-tools. "netconf-id" converted to "_netconfId"
     * 
     * @param name with attribute name, not null or empty
     * @return converted string or null if name was empty or null
     */
    public @Nullable static String toCamelCaseAttributeName(final String name) {
        if (name == null || name.isEmpty())
            return null;

        final StringBuilder ret = new StringBuilder(name.length());
        if (!name.startsWith("_"))
            ret.append('_');
        int start = 0;
        for (final String word : name.split("-")) {
            if (!word.isEmpty()) {
                if (start++ == 0) {
                    ret.append(Character.toLowerCase(word.charAt(0)));
                } else {
                    ret.append(Character.toUpperCase(word.charAt(0)));
                }
                ret.append(word.substring(1));
            }
        }
        return ret.toString();
    }

    /**
     * Adapted Builder callbacks
     */
    private static class YangToolsBuilderAnnotationIntrospector extends JacksonAnnotationIntrospector {
        private static final long serialVersionUID = 1L;

        @Override
        public Class<?> findPOJOBuilder(AnnotatedClass ac) {
            try {
                String builder = null;
                if (ac.getRawType().equals(Credentials.class)) {
                    builder =
                            "org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.credentials.credentials.LoginPasswordBuilder";
                    //System.out.println(DataContainer.class.isAssignableFrom(ac.getRawType()));
                    //System.out.println(ChoiceIn.class.isAssignableFrom(ac.getRawType()));

                } else if (ac.getRawType().equals(DateAndTime.class)) {
                    builder = DateAndTimeBuilder.class.getName();
                }

                else {
                    if (ac.getRawType().isInterface()) {
                        builder = ac.getName() + "Builder";
                    }
                }
                if (builder != null) {
                    //System.out.println("XX1: "+ac.getRawType());
                    //System.out.println("XX2: "+builder);
                    //Class<?> innerBuilder = Class.forName(builder);
                    Class<?> innerBuilder = findClass(builder);
                    //System.out.println("Builder found: "+ innerBuilder);
                    return innerBuilder;
                }
            } catch (ClassNotFoundException e) {
                // No problem .. try next
            }
            return super.findPOJOBuilder(ac);
        }

        @Override
        public Value findPOJOBuilderConfig(AnnotatedClass ac) {
            if (ac.hasAnnotation(JsonPOJOBuilder.class)) {
                return super.findPOJOBuilderConfig(ac);
            }
            return new JsonPOJOBuilder.Value("build", "set");
        }
    }

    private static Class<?> findClass(String name) throws ClassNotFoundException {
        // Try to find in other bundles
        if (context != null) {
            //OSGi environment
            for (Bundle b : context.getBundles()) {
                try {
                    return b.loadClass(name);
                } catch (ClassNotFoundException e) {
                    // No problem, this bundle doesn't have the class
                }
            }
            throw new ClassNotFoundException("Can not find Class in OSGi context.");
        } else {
            return Class.forName(name);
        }
        // not found in any bundle
    }

    public static class DateAndTimeBuilder {

        private final String _value;

        public DateAndTimeBuilder(String v) {
            this._value = v;
        }

        public DateAndTime build() {
            return new DateAndTime(_value);
        }

    }
    public static class CustomDateAndTimeSerializer extends StdSerializer<DateAndTime> {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public CustomDateAndTimeSerializer() {
            this(null);
        }

        protected CustomDateAndTimeSerializer(Class<DateAndTime> t) {
            super(t);
        }

        @Override
        public void serialize(DateAndTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.getValue());
        }

    }
}
