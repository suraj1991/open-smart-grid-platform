/**
 * Copyright 2017 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.osgpfoundation.osgp.domain.da.valueobjects;

import org.osgpfoundation.osgp.domain.da.valueobjects.iec61850.LogicalDevice;

import java.io.Serializable;
import java.util.List;

public class GetPQValuesResponse implements Serializable{
    private List<LogicalDevice> logicalDevices;

    public GetPQValuesResponse( final List<LogicalDevice> logicalDevices ) {
        this.logicalDevices = logicalDevices;
    }

    public List<LogicalDevice> getLogicalDevices() {
        return logicalDevices;
    }
}
