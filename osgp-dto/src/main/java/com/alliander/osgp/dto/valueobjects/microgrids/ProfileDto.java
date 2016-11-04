/**
 * Copyright 2014-2016 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.dto.valueobjects.microgrids;

import java.io.Serializable;
import java.util.List;

public class ProfileDto implements Serializable {

    private static final long serialVersionUID = 7279719312339028843L;

    private Integer id;
    private String node;
    private List<ProfileEntryDto> profileEntries;

    public ProfileDto(final Integer id, final String node, final List<ProfileEntryDto> profileEntries) {
        this.id = id;
        this.node = node;
        this.profileEntries = profileEntries;
    }

    public Integer getId() {
        return this.id;
    }

    public String getNode() {
        return this.node;
    }

    public List<ProfileEntryDto> getProfileEntries() {
        return this.profileEntries;
    }
}
