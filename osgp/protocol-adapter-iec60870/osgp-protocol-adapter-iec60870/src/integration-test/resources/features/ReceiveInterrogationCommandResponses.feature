@ProtocolAdapterIec60870
Feature: OSGP Protocol Adapter IEC60870 - Receive Interrogation Command Responses
    In order to correctly handle device messaging
    As a protocol adapter
    I want to be able to process interrogation command responses from IEC60870 devices
    

Scenario: Receive an interrogation command response ASDU
    Given an existing connection with IEC60870 device "DA_DVC_1" of type DISTRIBUTION_AUTOMATION_DEVICE
    When I receive an ASDU of type "C_IC_NA_1" from the IEC60870 device
    Then I should send a log item with a message containing type "C_IC_NA_1"
