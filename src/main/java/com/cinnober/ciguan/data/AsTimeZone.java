package com.cinnober.ciguan.data;


/**
 * This class represents a time zone.
 *
 */
public class AsTimeZone {

    /** The key. */
    public String key;
    
    /**
     * Default instance
     */
    public AsTimeZone() {
    }
    
    /**
     * Instantiates a new as time zone.
     *
     * @param pTimeZoneId the time zone id
     */
    public AsTimeZone(String pTimeZoneId) {
        key = pTimeZoneId;
    }
}
