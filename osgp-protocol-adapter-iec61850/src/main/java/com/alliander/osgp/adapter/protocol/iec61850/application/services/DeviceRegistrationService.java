/**
 * Copyright 2014-2016 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.adapter.protocol.iec61850.application.services;

import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alliander.osgp.adapter.protocol.iec61850.domain.valueobjects.DeviceMessageLog;
import com.alliander.osgp.adapter.protocol.iec61850.exceptions.NodeException;
import com.alliander.osgp.adapter.protocol.iec61850.exceptions.NodeNotFoundException;
import com.alliander.osgp.adapter.protocol.iec61850.exceptions.NodeWriteException;
import com.alliander.osgp.adapter.protocol.iec61850.exceptions.ProtocolAdapterException;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.helper.DeviceConnection;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.helper.Function;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.helper.IED;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.helper.LogicalDevice;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.services.Iec61850DeviceConnectionService;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.services.commands.Iec61850ClearReportCommand;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.services.commands.Iec61850DisableRegistrationCommand;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.services.commands.Iec61850EnableReportingCommand;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.services.commands.Iec61850SetGpsCoordinatesCommand;
import com.alliander.osgp.core.db.api.iec61850.entities.Ssld;
import com.alliander.osgp.core.db.api.iec61850.repositories.SsldDataRepository;

@Service(value = "iec61850DeviceRegistrationService")
public class DeviceRegistrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceRegistrationService.class);

    @Autowired
    private Iec61850DeviceConnectionService iec61850DeviceConnectionService;

    @Autowired
    private SsldDataRepository ssldDataRepository;

    @Autowired
    private int delayAfterDeviceRegistration;

    @Autowired
    private boolean isReportingAfterDeviceRegistrationEnabled;

    @Autowired
    private Float defaultLatitude;

    @Autowired
    private Float defaultLongitude;

    /**
     * After the device has registered with the platform successfully, the
     * device has to be informed that the registration worked. Disable an
     * attribute so the device will stop attempting to register once a minute.
     *
     * @param deviceIdentification
     *            The device identification.
     * @param ipAddress
     *            The IP address of the device.
     * @param ied
     *            The type of IED.
     * @param serverName
     *            The server name.
     *
     * @throws ProtocolAdapterException
     *             In case the connection to the device can not be established
     *             or the connection breaks during communication.
     */
    public void disableRegistration(final String deviceIdentification, final InetAddress ipAddress, final IED ied,
            final String serverName) throws ProtocolAdapterException {

        final DeviceConnection deviceConnection = this.iec61850DeviceConnectionService.connectWithoutConnectionCaching(
                ipAddress.getHostAddress(), deviceIdentification, "", ied, serverName,
                LogicalDevice.LIGHTING.getDescription());

        final Function<Void> function = new Function<Void>() {

            @Override
            public Void apply(final DeviceMessageLog deviceMessageLog) throws Exception {
                DeviceRegistrationService.this.disableRegistration(deviceConnection);
                DeviceRegistrationService.this.setLocationInformation(deviceConnection);
                if (DeviceRegistrationService.this.isReportingAfterDeviceRegistrationEnabled) {
                    LOGGER.info("Reporting enabled for device: {}", deviceConnection.getDeviceIdentification());
                    DeviceRegistrationService.this.enableReporting(deviceConnection);
                } else {
                    LOGGER.info("Reporting disabled for device: {}", deviceIdentification);
                    DeviceRegistrationService.this.iec61850DeviceConnectionService.disconnect(deviceConnection, null);
                }
                return null;
            }
        };

        this.iec61850DeviceConnectionService.sendCommandWithRetry(function, deviceIdentification);
    }

    /**
     * Set the location information for this device. If the osgp_core database
     * contains longitude and latitude information for the given device, those
     * values must be saved to the corresponding data-attributes.
     *
     * @throws NodeException
     * @throws NodeNotFoundException
     *
     */
    protected void setLocationInformation(final DeviceConnection deviceConnection) throws NodeException {
        final Ssld ssld = DeviceRegistrationService.this.ssldDataRepository
                .findByDeviceIdentification(deviceConnection.getDeviceIdentification());
        if (ssld != null) {
            final Float longitude = ssld.getGpsLongitude();
            final Float latitude = ssld.getGpsLatitude();
            LOGGER.info("Ssld found for device: {} longitude: {}, latitude: {}",
                    deviceConnection.getDeviceIdentification(), longitude, latitude);

            if (longitude != null && latitude != null) {
                // Add GPS information when available in meta data.
                this.writeGpsCoordinates(deviceConnection, longitude, latitude);
            } else {
                // Otherwise use default GPS information.
                this.writeGpsCoordinates(deviceConnection, this.defaultLongitude, this.defaultLatitude);
            }
        } else {
            LOGGER.warn("No SSLD found for device identification: {}", deviceConnection.getDeviceIdentification());
        }
    }

    private void writeGpsCoordinates(final DeviceConnection deviceConnection, final Float longitude,
            final Float latitude) throws NodeException {
        if (longitude != null && latitude != null) {
            try {
                new Iec61850SetGpsCoordinatesCommand().setGpsCoordinates(deviceConnection, longitude, latitude);
            } catch (final NodeWriteException e) {
                LOGGER.error(
                        "Unable to set location information for device: " + deviceConnection.getDeviceIdentification(),
                        e);
            }
        }
    }

    /**
     * Set attribute to false in order to signal the device the registration was
     * successful.
     *
     * @throws NodeException
     *             In case writing of the data-attribute fails.
     * @throws NodeNotFoundException
     *
     */
    protected void disableRegistration(final DeviceConnection deviceConnection)
            throws NodeNotFoundException, NodeException {
        new Iec61850DisableRegistrationCommand().disableRegistration(deviceConnection);
    }

    protected void enableReporting(final DeviceConnection deviceConnection) throws NodeException {
        try {
            new Iec61850EnableReportingCommand().enableReportingOnDeviceWithoutUsingSequenceNumber(
                    DeviceRegistrationService.this.iec61850DeviceConnectionService.getIec61850Client(),
                    deviceConnection);
            // Don't disconnect now! The device should be able to send
            // reports.
            this.waitClearReportAndDisconnect(deviceConnection);
        } catch (final NodeWriteException e) {
            LOGGER.error("Unable to enabele reporting for device: " + deviceConnection.getDeviceIdentification(), e);
            DeviceRegistrationService.this.iec61850DeviceConnectionService.disconnect(deviceConnection, null);
        }
    }

    protected void waitClearReportAndDisconnect(final DeviceConnection deviceConnection) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    new Iec61850ClearReportCommand().clearReportOnDevice(deviceConnection);
                } catch (final NodeNotFoundException e) {
                    LOGGER.error("Unable to get fcModelnode for device: " + deviceConnection.getDeviceIdentification(),
                            e);
                } catch (final NodeException e) {
                    LOGGER.error("Unable to clear report for device: " + deviceConnection.getDeviceIdentification(), e);
                }
                DeviceRegistrationService.this.iec61850DeviceConnectionService.disconnect(deviceConnection, null);
            }
        }, DeviceRegistrationService.this.delayAfterDeviceRegistration);
    }
}
