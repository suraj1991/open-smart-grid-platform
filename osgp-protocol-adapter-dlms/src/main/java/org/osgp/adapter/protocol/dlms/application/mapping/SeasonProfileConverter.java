/**
 * Copyright 2015 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.osgp.adapter.protocol.dlms.application.mapping;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.metadata.Type;

import org.openmuc.jdlms.datatypes.CosemDateTime;
import org.openmuc.jdlms.datatypes.DataObject;

import com.alliander.osgp.dto.valueobjects.smartmetering.SeasonProfile;

public class SeasonProfileConverter extends CustomConverter<SeasonProfile, DataObject> {

    @Override
    public DataObject convert(final SeasonProfile source, final Type<? extends DataObject> destinationType) {
        if (source == null) {
            return null;
        }

        final List<DataObject> seasonElements = new ArrayList<>();

        final DataObject seasonProfileNameObject = DataObject.newOctetStringData(source.getSeasonProfileName()
                .getBytes(StandardCharsets.UTF_8));
        seasonElements.add(seasonProfileNameObject);

        final DataObject seasonStartObject = DataObject.newDateTimeData(this.mapperFacade.map(source.getSeasonStart(),
                CosemDateTime.class));
        seasonElements.add(seasonStartObject);

        final DataObject seasonWeekProfileNameObject = DataObject.newOctetStringData(source.getWeekProfile()
                .getWeekProfileName().getBytes(StandardCharsets.UTF_8));
        seasonElements.add(seasonWeekProfileNameObject);

        return DataObject.newStructureData(seasonElements);
    }

}
