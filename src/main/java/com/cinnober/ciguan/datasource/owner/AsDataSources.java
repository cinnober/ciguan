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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.cinnober.ciguan.AsLoggerIf;
import com.cinnober.ciguan.data.AsDataSourceDef;
import com.cinnober.ciguan.datasource.AsDataSourceIf;
import com.cinnober.ciguan.datasource.AsDataSourceOwnerIf;
import com.cinnober.ciguan.datasource.AsFilterIf;
import com.cinnober.ciguan.datasource.AsListIf;
import com.cinnober.ciguan.datasource.AsSortIf;
import com.cinnober.ciguan.datasource.impl.AsDataSourceKeyGenerator;
import com.cinnober.ciguan.impl.As;
import com.cinnober.ciguan.scheduler.AsScheduledTaskHandleIf;
import com.cinnober.ciguan.scheduler.AsScheduledTaskIf;
import com.cinnober.ciguan.scheduler.AsSchedulerIf;

/**
 * Base class for data source storage
 */
public abstract class AsDataSources implements AsDataSourceOwnerIf {

	private static final int INTERVAL = 60 * 1000; // Once per minute
	private static final int MAX_IDLE = 30 * 1000; // 30 seconds of being idle
	private static AtomicLong cLastInstanceId = new AtomicLong();

	protected AsScheduledTaskHandleIf mCleanup;
	//	protected final MibValue mMibValue = new MibValue();
	protected AtomicInteger mUseCounter = new AtomicInteger();
	protected long mInstanceId;
	protected final Map<String, AsDataSourceIf<?>> mDataSources = new HashMap<String, AsDataSourceIf<?>>();

	public AsDataSources() {
		mCleanup = AsSchedulerIf.Singleton.get().schedule(new CleanupTask());
		mInstanceId = cLastInstanceId.addAndGet(1);
		//		mMibValue.update();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> AsDataSourceIf<T> getDataSource(String pDataSourceId, AsFilterIf<T> pFilter, AsSortIf<T> pSort) {
		synchronized (mDataSources) {
			AsDataSourceIf tBaseDataSource = mDataSources.get(
					AsDataSourceKeyGenerator.createKey(pDataSourceId, null, null));
			AsDataSourceIf tFilterDataSource = getDataSource(tBaseDataSource, pFilter);
			AsDataSourceIf tSortDataSource = getDataSource(tFilterDataSource, pSort);
			return tSortDataSource;
		}
	}

	public <T> AsListIf<T> getList(String pDataSourceId) {
		AsDataSourceIf<T> tSource = getDataSource(pDataSourceId, null, null);
		return (AsListIf<T>) tSource;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private AsDataSourceIf getDataSource(AsDataSourceIf pDataSource, AsFilterIf<?> pFilter) {
		if (pDataSource == null || pFilter == null) {
			return pDataSource;
		}
		AsDataSourceIf tDataSource = mDataSources.get(
				AsDataSourceKeyGenerator.createKey(pDataSource.getDataSourceId(), pFilter, null));
		if (tDataSource == null) {
			tDataSource = pDataSource.createDataSource(pFilter);
			putDataSource(tDataSource);
		}
		return tDataSource;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private AsDataSourceIf getDataSource(AsDataSourceIf pDataSource, AsSortIf<?> pSort) {
		if (pDataSource == null || pSort == null) {
			return pDataSource;
		}
		AsDataSourceIf tModel = mDataSources.get(
				AsDataSourceKeyGenerator.createKey(pDataSource.getDataSourceId(), pDataSource.getFilter(), pSort));
		if (tModel == null) {
			tModel = pDataSource.createDataSource(pSort);
			putDataSource(tModel);
		}
		return tModel;
	}

	protected void putDataSource(AsDataSourceIf<?> pDataSource) {
		if (pDataSource.getDataSourceId() == null) {
			throw new RuntimeException(getClass().getSimpleName() + ": Attempt to register a model without an ID");
		}

		String tKey = AsDataSourceKeyGenerator.createKey(
				pDataSource.getDataSourceId(), pDataSource.getFilter(), pDataSource.getSort());
		if (mDataSources.containsKey(tKey)) {
			throw new RuntimeException("Data source with ID " + pDataSource.getDataSourceId() + " already registered");
		}
		if (pDataSource != null) {
			AsLoggerIf.Singleton.get().logTrace(getClass().getSimpleName() + "(" + getName() +
					"): Data source with key " + tKey + " registered");
			mDataSources.put(tKey, pDataSource);
			pDataSource.setOwner(this);
			//			mMibValue.update();
		}
	}

	protected void putDataSourceAs(AsDataSourceIf<?> pDataSource, String pModelId) {
		if (mDataSources.containsKey(pModelId)) {
			throw new RuntimeException("Data source with ID " + pDataSource.getDataSourceId() + " already registered");
		}
		if (pDataSource != null) {
			AsLoggerIf.Singleton.get().logTrace(getClass().getSimpleName() + "(" + getName() +
					"): Data source with key " + pModelId + " registered");
			mDataSources.put(pModelId, pDataSource);
			pDataSource.setOwner(this);
			//			mMibValue.update();
		}
	}

	public Map<String, AsDataSourceIf<?>> getDataSources() {
		return mDataSources;
	}

	protected void logAndDie(List<AsDataSourceDef<?>> pDataSources) {
		StringBuilder tLog = new StringBuilder();
		tLog.append("One or more user data sources could not be created due to referential errors:").append("\n\n");
		for (AsDataSourceDef<?> tDataSourceDef : pDataSources) {
			tLog.append(tDataSourceDef.getId()).append("\n");
		}
		tLog.append("\n").append("Aborting application server startup").append("\n");
		As.systemExit(tLog.toString(), null);
	}

	/**
	 * TODO: This may need optimization
	 * @param pIncludePermanent
	 */
	protected void clean(boolean pIncludePermanent) {
		synchronized (mDataSources) {
			Iterator<Map.Entry<String, AsDataSourceIf<?>>> tIterator = mDataSources.entrySet().iterator();
			while (tIterator.hasNext()) {
				Map.Entry<String, AsDataSourceIf<?>> tEntry = tIterator.next();
				boolean tRemove;
				if (pIncludePermanent) {
					tRemove = true;
				}
				else {
					tRemove =
							tEntry.getValue().getChildlessTime() > MAX_IDLE &&
							!tEntry.getValue().isPermanent();
				}
				if (tRemove) {
					boolean tDestroy = tEntry.getValue().getOwner() == this;
					AsLoggerIf.Singleton.get().logTrace(
							AsDataSources.this.getClass().getSimpleName() + "(" + getName() +
									"): Data source with key " + tEntry.getKey() + (tDestroy ? " destroyed" : " unregistered"));
					if (tDestroy) {
						tEntry.getValue().destroy();
					}
					tIterator.remove();
					//					mMibValue.update();
				}
			}
		}
	}

	/**
	 * Task responsible for cleaning up unused data sources
	 */
	protected class CleanupTask implements AsScheduledTaskIf {

		@Override
		public String getName() {
			return "DataSourceCleanupTask-" + mInstanceId;
		}

		@Override
		public void run() {
			clean(false);
		}

		@Override
		public int getDelayMs() {
			return INTERVAL;
		}

		@Override
		public int getIntervalMs() {
			return INTERVAL;
		}

	}

	//	    protected class MibValue implements AsHasBdxValue {
	//
	//	        private boolean mFlag;
	//
	//	        protected void update() {
	//	            if (!mFlag) {
	//	                mFlag = true;
	//	                As.getBdxHandler().broadcast(MibValue.this);
	//	            }
	//	        }
	//
	//	        @Override
	//	        public Object getBdxValue() {
	//	            mFlag = false;
	//	            MibDataSourceOwner tMib = new MibDataSourceOwner();
	//	            tMib.type = AsDataSources.this.getClass().getSimpleName();
	//	            tMib.key = mInstanceId;
	//	            tMib.name = "" + getName();
	//	            tMib.size = mDataSources.size();
	//	            tMib.used = mUseCounter.get();
	//	            return tMib;
	//	        }
	//	    }

	@Override
	public void setUsed(Object pUser, boolean pUsed) {
		mUseCounter.addAndGet(pUsed ? 1 : -1);
		//	        mMibValue.update();
	}

	@Override
	public boolean isRegisteredDataSourceId(String pId) {
		return mDataSources.containsKey(pId);
	}

	@Override
	public void destroy() {
		mCleanup.cancel();
	}

}
