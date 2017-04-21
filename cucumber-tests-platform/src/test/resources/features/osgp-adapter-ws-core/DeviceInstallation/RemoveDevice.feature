@PublicLighting @Platform @CoreDeviceInstallation
Feature: CoreDeviceInstallation Remove device
  As a grid operator
  I want to be able to perform DeviceInstallation operations on a device
  In order to ...

  #  This test doesn't work because the backend doesn't remove the device.
  @Skip
  Scenario Outline: Remove A Device
    Given a device
      | DeviceIdentification | <DeviceIdentification> |
    When receiving a remove device request
      | DeviceIdentification | <DeviceIdentification> |
    Then the remove device response is successful
    And the device with id "<DeviceIdentification>" should be removed

    Examples: 
      | DeviceIdentification |
      | TEST1024000000001    |
