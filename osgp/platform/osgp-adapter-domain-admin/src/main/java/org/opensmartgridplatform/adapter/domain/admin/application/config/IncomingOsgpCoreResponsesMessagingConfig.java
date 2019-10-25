/**
 * Copyright 2019 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.opensmartgridplatform.adapter.domain.admin.application.config;

import javax.net.ssl.SSLException;

import org.apache.activemq.pool.PooledConnectionFactory;
import org.opensmartgridplatform.adapter.domain.admin.infra.jms.core.OsgpCoreResponseMessageListener;
import org.opensmartgridplatform.shared.application.config.AbstractConfig;
import org.opensmartgridplatform.shared.application.config.jms.JmsConfigurationNames;
import org.opensmartgridplatform.shared.application.config.messaging.JmsConfigurationFactory;
import org.opensmartgridplatform.shared.application.config.messaging.JmsDefaultConfig;
import org.opensmartgridplatform.shared.infra.jms.BaseMessageProcessorMap;
import org.opensmartgridplatform.shared.infra.jms.MessageProcessorMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

@Configuration
@PropertySource("classpath:osgp-adapter-domain-admin.properties")
@PropertySource(value = "file:${osgp/Global/config}", ignoreResourceNotFound = true)
@PropertySource(value = "file:${osgp/AdapterDomainAdmin/config}", ignoreResourceNotFound = true)
public class IncomingOsgpCoreResponsesMessagingConfig extends AbstractConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(IncomingOsgpCoreResponsesMessagingConfig.class);

    private JmsConfigurationFactory jmsConfigurationFactory;

    public IncomingOsgpCoreResponsesMessagingConfig(final Environment environment,
            final JmsDefaultConfig defaultMessagingConfig) throws SSLException {
        this.jmsConfigurationFactory = new JmsConfigurationFactory(environment, defaultMessagingConfig,
                JmsConfigurationNames.JMS_INCOMING_OSGP_CORE_RESPONSES);
    }

    @Bean(destroyMethod = "stop", name = "domainAdminIncomingOsgpCoreResponsesPooledConnectionFactory")
    public PooledConnectionFactory incomingOsgpCoreResponsesPooledConnectionFactory() {
        LOGGER.info("Initializing pooled connection factory for incoming OSGP core responses.");
        return this.jmsConfigurationFactory.getPooledConnectionFactory();
    }

    @Bean(name = "domainAdminIncomingOsgpCoreResponsesMessageListenerContainer")
    public DefaultMessageListenerContainer incomingOsgpCoreResponsesMessageListenerContainer(
            @Qualifier("domainAdminIncomingOsgpCoreResponseMessageListener") final OsgpCoreResponseMessageListener incomingOsgpCoreResponseMessageListener) {
        return this.jmsConfigurationFactory.initMessageListenerContainer(incomingOsgpCoreResponseMessageListener);
    }

    @Bean
    @Qualifier("domainAdminOsgpCoreResponseMessageProcessorMap")
    public MessageProcessorMap incomingOsgpCoreResponseMessageProcessorMap() {
        return new BaseMessageProcessorMap("OsgpCoreResponseMessageProcessorMap");
    }
}
