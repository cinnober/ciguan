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
package com.cinnober.ciguan.datasource.impl;

import java.util.concurrent.atomic.AtomicLong;

import com.cinnober.ciguan.datasource.AsDataSourceEventIf;
import com.cinnober.ciguan.datasource.AsDataSourceIf;
import com.cinnober.ciguan.datasource.AsDataSourceListenerIf;
import com.cinnober.ciguan.datasource.AsDataSourceOwnerIf;
import com.cinnober.ciguan.datasource.AsFilterIf;
import com.cinnober.ciguan.datasource.AsSortIf;

/**
 * Base class for application server models
 *
 * mListeners is updated from different threads, so there is a need to synchronize the usage of this
 * collection. For example, broadcasts are received in one thread and calls notifyListeners while
 * clients set up and remove their subscriptions by calling addListener and removeListener
 * from the HTTP processing threads.
 *
 * @param <T> The type of the contained items
 */
public abstract class AsDataSource<T> implements AsDataSourceIf<T> {

	/** The last instance id. */
	private static AtomicLong cLastInstanceId = new AtomicLong();

	/** The model id. */
	protected final String mModelId;

	/** The sort. */
	protected final AsSortIf<T> mSort;

	/** The class. */
	protected final Class<T> mClass;

	/** The listeners. */
	protected final AsDataSourceListenerCollection<T> mListeners = new AsDataSourceListenerCollection<T>();

	/** The owner. */
	protected AsDataSourceOwnerIf mOwner;

	/** The filter. */
	private AsFilterIf<T> mFilter;

	/** The source. */
	private final AsDataSourceIf<T> mSource;

	/** The instance id. */
	private final long mInstanceId;

	/** The childless timestamp. */
	private long mChildlessTimestamp = System.currentTimeMillis();

	/** The permanent. */
	private boolean mPermanent;

	/**
	 * Instantiates a new as data source.
	 *
	 * @param pModelId the model id
	 * @param pSource the source
	 * @param pFilter the filter
	 * @param pSort the sort
	 * @param pClass the class
	 */
	public AsDataSource(String pModelId, AsDataSourceIf<T> pSource,
			AsFilterIf<T> pFilter, AsSortIf<T> pSort, Class<T> pClass) {
		mModelId = pModelId;
		mSource = pSource;
		mFilter = pFilter;
		mSort = pSort;
		mClass = pClass;
		mInstanceId = cLastInstanceId.addAndGet(1);
	}

	protected long getmInstanceId() {
		return mInstanceId;
	}

	/**
	 * Updates the management information base.
	 */
	//	protected void updateMib() {
	//		MibDataSource tMib = new MibDataSource();
	//		tMib.key = mInstanceId;
	//		tMib.dataSourceId = getDataSourceId();
	//		tMib.listeners = mListeners.size();
	//		tMib.filter = mFilter != null ? mFilter.toString() : "";
	//		tMib.sort = mSort != null ? mSort.toString() : "";
	//		if (mOwner != null) {
	//			tMib.ownertype = mOwner.getClass().getSimpleName();
	//			tMib.ownername = mOwner.getName();
	//		}
	//		As.getBdxHandler().broadcast(tMib);
	//	}

	@Override
	public String getDataSourceId() {
		return mModelId;
	}

	@Override
	public AsDataSourceOwnerIf getOwner() {
		return mOwner;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Updates the management information base.
	 * @see AsDataSource#updateMib()
	 */
	@Override
	public void setOwner(AsDataSourceOwnerIf pOwner) {
		// Only allow owner to be set once
		if (mOwner == null) {
			mOwner = pOwner;
			//			updateMib();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * Updates the management information base.
	 * @see AsDataSource#updateMib()
	 */
	@Override
	public void addListener(AsDataSourceListenerIf<T> pListener) {
		synchronized (mListeners) {
			mListeners.add(pListener);
			mChildlessTimestamp = 0;
		}
		//		updateMib();
	}

	/**
	 * {@inheritDoc}
	 *
	 * Updates the management information base.
	 * @see AsDataSource#updateMib()
	 */
	@Override
	public void removeListener(AsDataSourceListenerIf<T> pListener) {
		synchronized (mListeners) {
			mListeners.remove(pListener);
			if (mListeners.isEmpty()) {
				mChildlessTimestamp = System.currentTimeMillis();
			}
		}
		//		updateMib();
	}

	@Override
	public boolean hasListeners() {
		return mListeners.size() > 0;
	}

	@Override
	public long getChildlessTime() {
		return mChildlessTimestamp == 0 ? 0 : System.currentTimeMillis() - mChildlessTimestamp;
	}

	/**
	 * Notify listeners.
	 *
	 * @param pEvent the event
	 */
	protected void notifyListeners(AsDataSourceEventIf<T> pEvent) {
		synchronized (mListeners) {
			mListeners.notifyListeners(pEvent);
		}
	}

	@Override
	public AsDataSourceIf<T> getSource() {
		return mSource;
	}

	@Override
	public AsFilterIf<T> getFilter() {
		return mFilter;
	}

	@Override
	public AsSortIf<T> getSort() {
		return mSort;
	}

	/**
	 * Sets the base filter.
	 *
	 * @param pFilter the new base filter
	 */
	public void setBaseFilter(AsFilterIf<T> pFilter) {
		if (mFilter != null) {
			throw new IllegalStateException("Filter already defined");
		}
		mFilter = pFilter;
	}

	/**
	 * Checks if the datasource includes the specified item.
	 *
	 * @param pItem the item
	 * @return true, if successful
	 */
	public boolean include(T pItem) {
		return mFilter == null || mFilter.include(pItem);
	}

	@Override
	public Class<T> getItemClass() {
		return mClass;
	}

	/**
	 * Gets the number of listeners.
	 *
	 * @return the number of listeners
	 */
	public int getNumberOfListeners() {
		return mListeners.size();
	}

	@Override
	public void setPermanent() {
		mPermanent = true;
	}

	@Override
	public boolean isPermanent() {
		return mPermanent;
	}

}
