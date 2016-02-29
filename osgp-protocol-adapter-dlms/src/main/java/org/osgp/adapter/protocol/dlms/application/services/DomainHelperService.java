/**
 * Copyright 2015 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.osgp.adapter.protocol.dlms.application.services;

import org.osgp.adapter.protocol.dlms.application.jasper.sessionproviders.SessionProvider;
import org.osgp.adapter.protocol.dlms.application.jasper.sessionproviders.SessionProviderService;
import org.osgp.adapter.protocol.dlms.application.jasper.sessionproviders.exceptions.SessionProviderException;
import org.osgp.adapter.protocol.dlms.application.jasper.sessionproviders.exceptions.SessionProviderUnsupportedException;
import org.osgp.adapter.protocol.dlms.domain.entities.DlmsDevice;
import org.osgp.adapter.protocol.dlms.domain.repositories.DlmsDeviceRepository;
import org.osgp.adapter.protocol.dlms.exceptions.ProtocolAdapterException;
import org.osgp.adapter.protocol.dlms.infra.messaging.DlmsDeviceMessageMetadata;
import org.osgp.adapter.protocol.dlms.infra.ws.JasperWirelessSmsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alliander.osgp.shared.exceptionhandling.ComponentType;
import com.alliander.osgp.shared.exceptionhandling.FunctionalException;
import com.alliander.osgp.shared.exceptionhandling.FunctionalExceptionType;

@Service(value = "dlmsDomainHelperService")
public class DomainHelperService {

    private static final int TEN_SECONDS = 10 * 1000;

    private static final Logger LOGGER = LoggerFactory.getLogger(DomainHelperService.class);

    private static final ComponentType COMPONENT_TYPE = ComponentType.PROTOCOL_DLMS;

    @Autowired
    private DlmsDeviceRepository dlmsDeviceRepository;

    @Autowired
    private SessionProviderService sessionProviderService;

    @Autowired
    private JasperWirelessSmsClient jasperWirelessSmsClient;

    @Autowired
    private int dlmsJwccGetSessionRetries;

    @Autowired
    private int dlmsJwccGetSessionSleepBetweenRetries;

    /**
     * Use {@link #findDlmsDevice(DlmsDeviceMessageMetadata)} instead, as this
     * will also set the IP address.
     * <p>
     * If this method turns out to be called from a location where
     * {@link DlmsDeviceMessageMetadata} is not available, check if the IP
     * address needs to be provided in another way.
     *
     * @deprecated
     */
    @Deprecated
    public DlmsDevice findDlmsDevice(final String deviceIdentification) throws FunctionalException {
        final DlmsDevice dlmsDevice = this.dlmsDeviceRepository.findByDeviceIdentification(deviceIdentification);
        if (dlmsDevice == null) {
            throw new FunctionalException(FunctionalExceptionType.UNKNOWN_DEVICE, COMPONENT_TYPE,
                    new ProtocolAdapterException("Unable to communicate with unknown device: " + deviceIdentification));
        }
        return dlmsDevice;
    }

    public DlmsDevice findDlmsDevice(final DlmsDeviceMessageMetadata messageMetadata) throws FunctionalException,
            SessionProviderException, InterruptedException {
        final String deviceIdentification = messageMetadata.getDeviceIdentification();
        final DlmsDevice dlmsDevice = this.dlmsDeviceRepository.findByDeviceIdentification(deviceIdentification);
        if (dlmsDevice == null) {
            throw new FunctionalException(FunctionalExceptionType.UNKNOWN_DEVICE, COMPONENT_TYPE,
                    new ProtocolAdapterException("Unable to communicate with unknown device: " + deviceIdentification));
        }
        // TODO: call the services that gets the ip address via getsessioinfo or
        // wake up the meter and get the ip address

        dlmsDevice.setIpAddress(this.getDeviceIpAddress(dlmsDevice, messageMetadata.getIpAddress()));

        return dlmsDevice;
    }

    private String getDeviceIpAddress(final DlmsDevice dlmsDevice, final String messageMetaDataIpAddress)
            throws InterruptedException, SessionProviderException {
        String deviceIpAddress = null;
        final String iccId = dlmsDevice.getIccId();

        try {
            deviceIpAddress = this.getDeviceIpAddressFromSessionProvider(iccId, dlmsDevice);
        } catch (final SessionProviderUnsupportedException e) {
            // The iccId is not supported by the sessionProvider. Use IP address
            // from the core
            LOGGER.warn(
                    "iccId {} is not supported by the sessionProvider for {}. Using device messageMetaData IpAddress {}",
                    iccId, dlmsDevice.getCommunicationProvider(), messageMetaDataIpAddress);
            return messageMetaDataIpAddress;
        }

        if (deviceIpAddress == null) {
            throw new SessionProviderException("The meter did not wake up in 5 minutes");
        }

        return deviceIpAddress;
    }

    private String getDeviceIpAddressFromSessionProvider(final String iccId, final DlmsDevice dlmsDevice)
            throws SessionProviderException, SessionProviderUnsupportedException, InterruptedException {

        final SessionProvider sessionProvider = this.sessionProviderService.getSessionProvider(dlmsDevice
                .getCommunicationProvider());

        String deviceIpAddress;
        deviceIpAddress = sessionProvider.getIpAddress(iccId);

        // If the result is null then the meter is not in session (not
        // awake).
        // So wake up the meter and start polling for the session
        if (deviceIpAddress == null) {
            this.jasperWirelessSmsClient.sendWakeUpSMS(iccId);
            for (int i = 0; i < this.dlmsJwccGetSessionRetries; i++) {
                Thread.sleep(this.dlmsJwccGetSessionSleepBetweenRetries);
                deviceIpAddress = sessionProvider.getIpAddress(iccId);
                if (deviceIpAddress != null) {
                    return deviceIpAddress;
                }
            }
        }
        return deviceIpAddress;
    }
}
