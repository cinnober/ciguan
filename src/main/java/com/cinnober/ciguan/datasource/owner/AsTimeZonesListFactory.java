package com.cinnober.ciguan.datasource.owner;

import com.cinnober.ciguan.data.AsTimeZone;
import com.cinnober.ciguan.datasource.AsDataSourceFactoryIf;
import com.cinnober.ciguan.datasource.AsDataSourceOwnerIf;
import com.cinnober.ciguan.datasource.AsListIf;
import com.cinnober.ciguan.datasource.impl.AsEmapiTreeMapList;

/**
 * 
 * Factory for the time zone data source
 * FIXME: Remove as soon as Proteus is updated with a Timezone object.
 *
 */
public class AsTimeZonesListFactory implements AsDataSourceFactoryIf<AsTimeZone> {

    @Override
    public AsListIf<AsTimeZone> createGlobalList(String pId) {
        AsListIf<AsTimeZone> tList = new AsEmapiTreeMapList<AsTimeZone>(pId, AsTimeZone.class, "key", "key");
        populateTimeZones(tList);
        return tList;
    }

    /**
     * Populate time zones.
     *
     * @param pList the list
     */
    private void populateTimeZones(AsListIf<AsTimeZone> pList) {
        // for (String tId : TimeZone.getAvailableIDs()) {
        //     tList.add(new AsTimeZone(tId));
        // }
        pList.add(new AsTimeZone("Europe/Stockholm"));
        pList.add(new AsTimeZone("America/New_York"));
        pList.add(new AsTimeZone("Hongkong"));
        pList.add(new AsTimeZone("Brazil/East"));

    }

    @Override
    public AsListIf<AsTimeZone> createMemberList(String pId, AsDataSourceOwnerIf pAsMemberDataSources) {
        return null;
    }
    
    @Override
    public AsListIf<AsTimeZone> createUserList(String pId,
        AsDataSourceOwnerIf pAsUserDataSources) {
        return null;
    }

    @Override
    public Class<AsTimeZone> getItemClass() {
        return AsTimeZone.class;
    }

    @Override
    public boolean isRootList() {
        return false;
    }

}
