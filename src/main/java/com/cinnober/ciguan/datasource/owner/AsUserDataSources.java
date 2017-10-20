/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Cinnober Financial Technology AB (cinnober.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.cinnober.ciguan.datasource.owner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.cinnober.ciguan.AsDataSourceNotFoundException;
import com.cinnober.ciguan.AsLoggerIf;
import com.cinnober.ciguan.data.AsDataSourceDef;
import com.cinnober.ciguan.datasource.AsDataSourceIf;
import com.cinnober.ciguan.datasource.AsFilterIf;
import com.cinnober.ciguan.datasource.AsListIf;
import com.cinnober.ciguan.datasource.impl.AsEmapiTreeMapList;
import com.cinnober.ciguan.impl.As;

/**
 * Class holding user level data sources
 * The intention here is to hide the original data sources by registering filtered data sources using the same name.
 */
public class AsUserDataSources extends AsDataSources {

	/**
	 * Maximum attempts to retry a data source factory creation.
	 */
	private static final int MAX_RETRIES = 3;

	/** The member id. */
	private final String mMemberId;

	/** The user id. */
	private final String mUserId;

	/**
	 * Instantiates a new as user data sources.
	 *
	 * @param pParameters the parameters
	 */
	public AsUserDataSources(String... pParameters) {

		mMemberId = pParameters[0];
		mUserId = pParameters[1];

		List<AsDataSourceDef<?>> tDataSources = new ArrayList<AsDataSourceDef<?>>();
		tDataSources.addAll(AsDataSourceDef.getAll());
		for (int i = 0; i < MAX_RETRIES && !tDataSources.isEmpty(); i++) {
			createUserDataSources(tDataSources);
		}
		if (!tDataSources.isEmpty()) {
			logAndDie(tDataSources);
		}
		if (isSuperUser()) {
			allowAnyAccess();
		}
	}

	/**
	 * This method is a means to indicate wether or not a user is to be considered super user.
	 * Super users bypass member level filters by registering the global data source on user level.
	 * However, super users do NOT bypass user filters, this needs to be coded into the filters.
	 *
	 * @return {@code true} if is super user
	 */
	protected boolean isSuperUser() {
		return false;
	}

	/**
	 * Create user level data sources.
	 *
	 * @param pDataSourceDefinitions the data source definitions
	 */
	protected void createUserDataSources(List<AsDataSourceDef<?>> pDataSourceDefinitions) {
		Iterator<AsDataSourceDef<?>> tIterator = pDataSourceDefinitions.iterator();
		while (tIterator.hasNext()) {
			AsDataSourceDef<?> tDataSourceDef = tIterator.next();
			if (tDataSourceDef.isQueryDataSource()) {
				createQueryDataSource(tDataSourceDef.getId());
			}
			else if (tDataSourceDef.getUserFilter() != null && tDataSourceDef.getUserFilter().length() > 0) {
				AsFilterIf<?> tFilter = tDataSourceDef.createUserFilter(getMemberId(), getUserId());
				if (tFilter != null) {
					String tDataSourceId = tDataSourceDef.getId();
					createUserDataSource(tDataSourceId, tDataSourceId, tFilter);
				}
			}
			else if (tDataSourceDef.getFactoryImpl() != null) {
				try {
					AsDataSourceIf<?> tDataSource = tDataSourceDef.getFactoryImpl()
							.createUserList(tDataSourceDef.getId(), this);
					if (tDataSource != null) {
						tDataSource.setPermanent();
						putDataSource(tDataSource);
					}
				}
				catch (AsDataSourceNotFoundException e) {
					// Lookup failed, leave the definition in the list
					AsLoggerIf.Singleton.get().logTrace(getClass().getSimpleName() + "(" + getName() +
							"): Creation of data source " + tDataSourceDef.getId() +
							" was delayed due to a data source lookup failure: " + e.getMessage());
					continue;
				}
			}
			tIterator.remove();
		}
	}

	/**
	 * Allow any access.
	 */
	protected final void allowAnyAccess() {

		// Register all global data sources here for users with special privileges,
		// since this type of access rights are not granted on the member level.
		// (Skip the data sources that are already registered)
		// NOTE: It is important that this step is the last in the initialization of this class.

		Map<String, AsDataSourceIf<?>> tRegisteredGlobalDataSources =
				((AsDataSources) As.getGlobalDataSources()).getDataSources();
		for (String tKey : tRegisteredGlobalDataSources.keySet()) {
			if (!getDataSources().containsKey(tKey)) {
				putDataSourceAs(tRegisteredGlobalDataSources.get(tKey), tKey);
			}
		}
	}

	@Override
	public String getName() {
		return mUserId;
	}

	/**
	 * Gets the member id.
	 *
	 * @return the member id
	 */
	public String getMemberId() {
		return mMemberId;
	}

	/**
	 * Gets the user id.
	 *
	 * @return the user id
	 */
	public String getUserId() {
		return mUserId;
	}

	/**
	 * Apply user filter.
	 *
	 * @param <T> the generic type
	 * @param pDataSourceId the data source id
	 * @param pFilter the filter
	 */
	protected <T> void applyUserFilter(String pDataSourceId, AsFilterIf<T> pFilter) {
		createUserDataSource(pDataSourceId, pDataSourceId, pFilter);
	}

	/**
	 * Creates the user data source.
	 *
	 * @param <T> the generic type
	 * @param pNewDataSource the new data source
	 * @param pOriginalSourceId the original source id
	 * @param pFilter the filter
	 */
	protected <T> void createUserDataSource(String pNewDataSource, String pOriginalSourceId, AsFilterIf<T> pFilter) {
		AsDataSourceIf<T> tOriginalDataSource = As.getMemberDataSources(mMemberId).
				getDataSource(pOriginalSourceId, null, null);
		AsDataSourceIf<T> tDataSource = tOriginalDataSource.createDataSource(pFilter);
		tDataSource.setPermanent();
		putDataSourceAs(tDataSource, pNewDataSource);
	}

	// Query data source (always create a new data source to avoid accidental modification of a shared data
	// source from server side code)

	/**
	 * Creates the query data source.
	 *
	 * @param <T> the generic type
	 * @param pDataSourceId the data source id
	 */
	@SuppressWarnings("unchecked")
	private <T> void createQueryDataSource(String pDataSourceId) {
		AsListIf<T> tOriginalDataSource = (AsListIf<T>) As.getMemberDataSources(mMemberId).
				getDataSource(pDataSourceId, null, null);
		AsListIf<T> tQueryList = new AsEmapiTreeMapList<T>(
				pDataSourceId, tOriginalDataSource.getItemClass(),
				tOriginalDataSource.getIdAttribute(), tOriginalDataSource.getTextAttribute());
		tQueryList.setPermanent();
		putDataSourceAs(tQueryList, pDataSourceId);
	}

	@Override
	public void destroy() {
		super.destroy();
		clean(true);
		As.removeUserDataSources(mMemberId, mUserId);
		//        mMibValue.update();
	}

	@Override
	public void setUsed(Object pUser, boolean pUsed) {
		super.setUsed(pUser, pUsed);
		if (mUseCounter.get() == 0) {
			destroy();
		}
	}

}
