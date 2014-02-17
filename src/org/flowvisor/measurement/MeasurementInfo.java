package org.flowvisor.measurement;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

import org.openflow.protocol.OFMatch;

public class MeasurementInfo implements Serializable {
	/**
	 * 
	 */
	
	private static final long serialVersionUID = 1L;
	
	public class SwitchEntry implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		long swid;
		long timestamp;
		
		public long getSwid() {
			return swid;
		}
		
		public long getTimestamp() {
			return timestamp;
		}
	}

	ConcurrentHashMap<OFMatch, SwitchEntry> matchTimestamp;
	
	ConcurrentHashMap<Long, Long> currentSwitchFlowCount;
	ConcurrentHashMap<Long, Long> lastSwitchFlowCount;
	
	public MeasurementInfo() {
		matchTimestamp = new ConcurrentHashMap<OFMatch, MeasurementInfo.SwitchEntry>();
		currentSwitchFlowCount = new ConcurrentHashMap<Long, Long>();
		lastSwitchFlowCount = new ConcurrentHashMap<Long, Long>();
	}
	
	public void setPacketIn(OFMatch match, long swid, long timestamp) {
		if(match.getNetworkSource() == 0 || match.getNetworkSource() == -1) {
			// =0 is 0.0.0.0, = -1 is 255.255.255.255, neither is a host!
			return;
		}
		match.setInputPort((short)-1);
		
		if(matchTimestamp.containsKey(match)) {
			
			//already seen it, compare timestamp to avoid race condition
			SwitchEntry current = matchTimestamp.get(match);
			if(current.timestamp > timestamp) {
				current.timestamp = timestamp;
				current.swid = swid;
			}			
		} else {
			SwitchEntry entry = new SwitchEntry();
			entry.swid = swid;
			entry.timestamp = timestamp;
			matchTimestamp.put(match, entry);
		}
	}

	public void incFlowProcess(long swid) {
		if(currentSwitchFlowCount.containsKey(swid)) {
			currentSwitchFlowCount.put(swid, currentSwitchFlowCount.get(swid) + 1);
		} else {
			currentSwitchFlowCount.put(swid, (long)1);
		}	
	}
	
	public void clear() {
		//matchTimestamp.clear();
		lastSwitchFlowCount.clear();
		lastSwitchFlowCount.putAll(currentSwitchFlowCount);
		currentSwitchFlowCount.clear();
	}
	
	public MeasurementInfo makeCopy() {
		MeasurementInfo copy = new MeasurementInfo();
		copy.matchTimestamp.putAll(this.matchTimestamp);
		copy.lastSwitchFlowCount.putAll(this.lastSwitchFlowCount);
		copy.currentSwitchFlowCount.putAll(this.currentSwitchFlowCount);
		return copy;
	}
	
	public ConcurrentHashMap<OFMatch, SwitchEntry> getMatchTimeMap() {
		return this.matchTimestamp;
	}
	
	public ConcurrentHashMap<Long, Long> getCurrentFlowCount() {
		return this.currentSwitchFlowCount;
	}
	
	public ConcurrentHashMap<Long, Long> getLastFlowCount() {
		return this.lastSwitchFlowCount;
	}
}
