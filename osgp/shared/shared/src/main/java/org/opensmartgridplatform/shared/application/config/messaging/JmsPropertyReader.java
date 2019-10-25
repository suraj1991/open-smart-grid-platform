/**
 * Copyright 2019 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.opensmartgridplatform.shared.application.config.messaging;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.core.env.Environment;

public class JmsPropertyReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(JmsPropertyReader.class);

    private static final String JMS_DEFAULT = "jms.default";

    private Environment environment;
    private String propertyPrefix;
    private JmsDefaultConfig defaultMessagingConfig;

    public JmsPropertyReader(final Environment environment, final String propertyPrefix,
            final JmsDefaultConfig defaultMessagingConfig) {
        this.environment = environment;
        this.propertyPrefix = propertyPrefix;
        this.defaultMessagingConfig = defaultMessagingConfig;
    }

    public <T> T get(final String propertyName, final Class<T> targetType) {
        LOGGER.info("Trying to find property {}.{}", this.propertyPrefix, propertyName);
        final String fullPropertyName = this.propertyPrefix + "." + propertyName;
        T property = this.environment.getProperty(fullPropertyName, targetType);

        if (property == null) {
            LOGGER.info("Property {} not found, trying default property.", fullPropertyName);
            property = this.getDefault(propertyName, targetType);
            LOGGER.info("Found value {} for property {}.{}", property, JMS_DEFAULT, propertyName);
        } else {
            LOGGER.info("Found value {} for property {}.{}", property, this.propertyPrefix, propertyName);
        }

        return property;
    }

    @SuppressWarnings("unchecked")
    private <T> T getDefault(final String propertyName, final Class<T> targetType) {
        final String fullPropertyName = JMS_DEFAULT + "." + propertyName;
        try {
            final Class<?>[] noParams = {};
            final Object[] noObjects = {};
            final String methodName = convertPropertyNameToMethodName(fullPropertyName, targetType);
            final Method method = JmsDefaultConfig.class.getDeclaredMethod(methodName, noParams);
            return (T) method.invoke(this.defaultMessagingConfig, noObjects);
        } catch (ReflectiveOperationException | SecurityException e) {
            throw new InvalidPropertyException(this.getClass(), fullPropertyName, e.getMessage(), e);
        }
    }

    // Converts a property name, like abc.def, to a getter names, like getAbcDef
    private static <T> String convertPropertyNameToMethodName(final String propertyName, final Class<T> targetType) {
        String prefix = "get";
        if (targetType.isAssignableFrom(boolean.class)) {
            prefix = "is";
        }
        return prefix + (StringUtils.remove(WordUtils.capitalizeFully(propertyName, '.'), '.'));
    }
}
