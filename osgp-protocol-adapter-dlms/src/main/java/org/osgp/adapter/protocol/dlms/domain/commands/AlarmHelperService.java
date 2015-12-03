package org.osgp.adapter.protocol.dlms.domain.commands;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.alliander.osgp.dto.valueobjects.smartmetering.AlarmType;

@Service("alarmHelperService")
public class AlarmHelperService {
    private static final int NUMBER_OF_BITS_IN_REGISTER = 32;

    private static final ByteRegisterConverter<AlarmType> BYTE_REGISTER_CONVERTER;

    static {
        /**
         * Gives the position of the alarm code as indicated by the AlarmType in
         * the bit string representation of the alarm register.
         * <p>
         * A position of 0 means the least significant bit, up to the maximum of
         * 31 for the most significant bit. Since the 4 most significant bits in
         * the object are not used according to the DSMR documentation, the
         * practical meaningful most significant bit is bit 27.
         */
        final EnumMap<AlarmType, Integer> map = new EnumMap<>(AlarmType.class);

        // Bits for group: Other Alarms
        map.put(AlarmType.CLOCK_INVALID, 0);
        map.put(AlarmType.REPLACE_BATTERY, 1);
        map.put(AlarmType.POWER_UP, 2);
        // bits 3 to 7 are not used

        // Bits for group: Critical Alarms
        map.put(AlarmType.PROGRAM_MEMORY_ERROR, 8);
        map.put(AlarmType.RAM_ERROR, 9);
        map.put(AlarmType.NV_MEMORY_ERROR, 10);
        map.put(AlarmType.MEASUREMENT_SYSTEM_ERROR, 11);
        map.put(AlarmType.WATCHDOG_ERROR, 12);
        map.put(AlarmType.FRAUD_ATTEMPT, 13);
        // bits 14 and 15 are not used

        // Bits for group: M-Bus Alarms
        map.put(AlarmType.COMMUNICATION_ERROR_M_BUS_CHANNEL_1, 16);
        map.put(AlarmType.COMMUNICATION_ERROR_M_BUS_CHANNEL_2, 17);
        map.put(AlarmType.COMMUNICATION_ERROR_M_BUS_CHANNEL_3, 18);
        map.put(AlarmType.COMMUNICATION_ERROR_M_BUS_CHANNEL_4, 19);
        map.put(AlarmType.FRAUD_ATTEMPT_M_BUS_CHANNEL_1, 20);
        map.put(AlarmType.FRAUD_ATTEMPT_M_BUS_CHANNEL_2, 21);
        map.put(AlarmType.FRAUD_ATTEMPT_M_BUS_CHANNEL_3, 22);
        map.put(AlarmType.FRAUD_ATTEMPT_M_BUS_CHANNEL_4, 23);

        // Bits for group: Reserved
        map.put(AlarmType.NEW_M_BUS_DEVICE_DISCOVERED_CHANNEL_1, 24);
        map.put(AlarmType.NEW_M_BUS_DEVICE_DISCOVERED_CHANNEL_2, 25);
        map.put(AlarmType.NEW_M_BUS_DEVICE_DISCOVERED_CHANNEL_3, 26);
        map.put(AlarmType.NEW_M_BUS_DEVICE_DISCOVERED_CHANNEL_4, 27);
        // bits 28 to 31 are not used

        // TODO: Dependency injection of this instance?
        BYTE_REGISTER_CONVERTER = new ByteRegisterConverter<AlarmType>(Collections.unmodifiableMap(map),
                NUMBER_OF_BITS_IN_REGISTER);
    }

    /**
     * Returns the position of the bit value for the given AlarmType, in the
     * 4-byte register space.
     *
     * @param alarmType
     *            AlarmType
     * @return position of the bit holding the alarm type value.
     */
    public Integer toBitPosition(final AlarmType alarmType) {
        return BYTE_REGISTER_CONVERTER.toBitPosition(alarmType);
    }

    /**
     * Create a set of alarm types representing the active bits in the register
     * value.
     *
     * @param registerValue
     *            Value of the register.
     * @return List of active alarm types.
     */
    public Set<AlarmType> toAlarmTypes(final Long registerValue) {
        return BYTE_REGISTER_CONVERTER.toTypes(registerValue);
    }

    /**
     * Calculate the long value for the given set of AlarmTypes
     *
     * @param alarmTypes
     *            Set of AlarmTypes
     * @return Long value.
     */
    public Long toLongValue(final Set<AlarmType> alarmTypes) {
        return BYTE_REGISTER_CONVERTER.toLongValue(alarmTypes);
    }
}
