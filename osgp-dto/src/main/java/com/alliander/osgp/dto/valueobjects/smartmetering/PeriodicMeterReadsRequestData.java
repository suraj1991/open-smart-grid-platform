/**
 * Copyright 2015 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.dto.valueobjects.smartmetering;

import java.io.Serializable;
import java.util.Date;

public class PeriodicMeterReadsRequestData implements Serializable {

    private static final long serialVersionUID = -2483665562035897062L;

    private final String deviceIdentification;
    private final PeriodType periodType;
    private final Date beginDate;
    private final Date endDate;

    public PeriodicMeterReadsRequestData(String deviceIdentification, PeriodType periodType, Date beginDate, Date endDate) {
        this.deviceIdentification = deviceIdentification;
        this.periodType = periodType;
        this.beginDate = beginDate;
        this.endDate = endDate;
    }

    public PeriodType getPeriodType() {
        return this.periodType;
    }

    public Date getBeginDate() {
        return this.beginDate;
    }

    public Date getEndDate() {
        return this.endDate;
    }

    public String getDeviceIdentification() {
        return this.deviceIdentification;
    }

}
