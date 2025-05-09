package dsd.dsdvansales.services;

public interface GattDeviceAPI {
    /**
     * Change the value of the GATT characteristic that we're publishing
     * @param value The new value to set for the characteristic
     */
    void setMyCharacteristicValue(String value);
}