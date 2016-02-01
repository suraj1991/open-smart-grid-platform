/**
 * Copyright 2015 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.dto.valueobjects.smartmetering;

public enum EventLogCategory {
    STANDARD_EVENT_LOG(2),
    FRAUD_DETECTION_LOG(2),
    COMMUNICATION_SESSION_LOG(3),
    M_BUS_EVENT_LOG(2);

    private int numberOfEventLogElements;

    private EventLogCategory(final int numberOfEventLogElements) {
        this.numberOfEventLogElements = numberOfEventLogElements;
    }

    public int getNumberOfEventElements() {
        return this.numberOfEventLogElements;
    }
}
