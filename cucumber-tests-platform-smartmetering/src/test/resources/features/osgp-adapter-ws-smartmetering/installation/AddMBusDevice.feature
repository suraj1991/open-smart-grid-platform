@SmartMetering @Platform
Feature: SmartMetering Installation - Add M-Bus device
  As a grid operator
  I want to be able to add a new M-Bus device

  Scenario: Add a new gas device
    When receiving a smartmetering add device request
      | DeviceIdentification           | TEST1024G00000001                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
      | DeviceType                     | SMART_METER_G                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
      | CommunicationMethod            | GPRS                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
      | MbusIdentificationNumber       |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         02615107 |
      | MbusManufacturerIdentification | LGB                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
      | MbusVersion                    |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              066 |
      | MbusDeviceTypeIdentification   |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               03 |
      | MbusDefaultKey                 | a0be6ca007ccdd440cf3e87a885bfda73db8ec1ce5483cc874adeaba7910a1b9a6455398d36bc2fca9026e9f949e555d6cc590002301dbbc97cf2ed7b3d4bd9d8e14c63a813f814114fa2c24bc57db6808b303de34f6ec29873ac6885f6606a71e7c585ddee0b01ab84a6cc504e7c3bc3533df880a2696cc2531863b74e1bd05bcca22966d8abd02b6379e9c61e01e09ed0a3e55af52fa9dc5fc64fc1f71e0b6b72439a1f326aac5e581b56dc2952c3f8f19389dca200246aa9cf169922c55a5f1b07784f7f9da9e6949f5508dbb72c4f8ee0935eb4fa51ebaf39c4cee57d8370e37ce43c62df834cfeed0ed33029fc12472d051e93cd630fe16e876a5001b42 |
      | DSMR_version                   | 4.2.2                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
      | ManufacturerCode               | Test                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
      | ModelCode                      | Test                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
    Then the add device response should be returned
      | DeviceIdentification | TEST1024G00000001 |
      | Result               | OK                |
    And the dlms device with identification "TEST1024G00000001" exists with device model
      | ManufacturerCode | Test |
      | ModelCode        | Test |
    And the smart meter is registered in the core database
      | DeviceIdentification           | TEST1024G00000001 |
      | MbusIdentificationNumber       |          02615107 |
      | MbusManufacturerIdentification | LGB               |
      | MbusVersion                    |               066 |
      | MbusDeviceTypeIdentification   |                03 |
    And the stored M-Bus Default key is not equal to the received key