package org.flowvisor.measurement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.openflow.protocol.OFMatch;

public class MeasurementInfo implements Serializable {
	/**
	 * 
	 */
	
	private static final long serialVersionUID = 1L;
	
	private ConcurrentHashMap<Long, ArrayList<OFMatch>> matchOnSwitchRecord;
	
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
	ConcurrentHashMap<Long, Long> oldSwitchFlowCount;
	
	public MeasurementInfo() {
		matchTimestamp = new ConcurrentHashMap<OFMatch, MeasurementInfo.SwitchEntry>();
		currentSwitchFlowCount = new ConcurrentHashMap<Long, Long>();
		lastSwitchFlowCount = new ConcurrentHashMap<Long, Long>();
		oldSwitchFlowCount = new ConcurrentHashMap<Long, Long>();
		matchOnSwitchRecord = new ConcurrentHashMap<Long, ArrayList<OFMatch>>();
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

	public void incFlowProcess(long swid, OFMatch match) {
		if(currentSwitchFlowCount.containsKey(swid)) {
			currentSwitchFlowCount.put(swid, currentSwitchFlowCount.get(swid) + 1);
		} else {
			currentSwitchFlowCount.put(swid, (long)1);
		}	
		OFMatch tmatch = new OFMatch();
		tmatch.setDataLayerDestination(match.getDataLayerDestination());
		tmatch.setDataLayerSource(match.getDataLayerSource());
		tmatch.setInputPort(match.getInputPort());
		tmatch.setWildcards(match.getWildcards());
		if(!matchOnSwitchRecord.containsKey(swid)) {
			ArrayList<OFMatch> matches = new ArrayList<>();
			matches.add(tmatch);
			matchOnSwitchRecord.put(swid, matches);
		} else {
			if(!matchOnSwitchRecord.get(swid).contains(tmatch)) {
				matchOnSwitchRecord.get(swid).add(tmatch);
				//System.out.println("inc:" + swid + " to " + matchOnSwitchRecord.get(swid).size() + ":" + tmatch + " ww:" + tmatch.getWildcards() +"\n");
			}
		}
	}
	
	public void decFlowProcess(long swid, OFMatch match) {
		if(!matchOnSwitchRecord.containsKey(swid)) {
			//System.out.println("Something is wrong!!dec flow to negative!" + swid + ":" + match);
			return;
		} else {
			if(matchOnSwitchRecord.get(swid).contains(match)) {
				matchOnSwitchRecord.get(swid).remove(match);
				//System.out.println("dec:" + swid + " to " + matchOnSwitchRecord.get(swid).size() + ":" + match + "\n");
				oldSwitchFlowCount.put(swid, (long)matchOnSwitchRecord.get(swid).size());
			} else {
				System.out.println("Removing non-existing entry!" + swid + ":" + matchOnSwitchRecord.get(swid) + ":" + match + " ww:" + match.getWildcards() + "\n");
				for(OFMatch tmatch : matchOnSwitchRecord.get(swid)) {
					System.out.println(equals(tmatch, match));
				}
			}
		}
	}
	
	public String equals(OFMatch m1, OFMatch m2) {
		String s = ">>>";
		if (m1 == m2) {
			return s;
		}
		if (m1 == null || m2 == null) {
			s += "1 ";
		}
		if (!(m2 instanceof OFMatch) || !(m1 instanceof OFMatch)) {
			s += "2 ";
		}
		if (!Arrays.equals(m1.getDataLayerDestination(), m2.getDataLayerDestination())) {
			s += "3 ";
		}
		if (!Arrays.equals(m1.getDataLayerSource(), m2.getDataLayerSource())) {
			s += "4 ";
		}
		if (m1.getDataLayerType() != m2.getDataLayerType()) {
			s += "5:" + m1.getDataLayerType() + ":" + m2.getDataLayerType() + "  ";
		}
		if (m1.getDataLayerVirtualLan() != m2.getDataLayerVirtualLan()) {
			s += "6:" + m1.getDataLayerVirtualLan() + ":" + m2.getDataLayerVirtualLan();
		}
		if (m1.getDataLayerVirtualLanPriorityCodePoint() != m2.getDataLayerVirtualLanPriorityCodePoint()) {
			s += "7 ";
		}
		if (m1.getInputPort() != m2.getInputPort()) {
			s += "8 ";
		}
		if (m1.getNetworkDestination() != m2.getNetworkDestination()) {
			s += "9:" + m1.getNetworkDestination() + ":" + m2.getNetworkDestination() + " ";
		}
		if (m1.getNetworkProtocol() != m2.getNetworkProtocol()) {
			s += "10:" + m1.getNetworkProtocol() + ":" + m2.getNetworkProtocol() + " ";
		}
		if (m1.getNetworkSource() != m2.getNetworkSource()) {
			s += "11:" + m1.getNetworkSource() + ":" + m2.getNetworkSource() + " ";
		}
		if (m1.getNetworkTypeOfService() != m2.getNetworkTypeOfService()) {
			s += "12 ";
		}
		if (m1.getTransportDestination() != m2.getTransportDestination()) {
			s += "13 ";
		}
		if (m1.getTransportSource() != m2.getTransportSource()) {
			s += "14:" + m1.getTransportSource() + ":" + m2.getTransportSource() + " ";
		}
		if ((m1.getWildcards() & OFMatch.OFPFW_ALL) != (m2.getWildcards() & OFMatch.OFPFW_ALL)) { // only
			// consider
			// allocated
			// part
			// of
			// wildcards
			s += "15 ";
		}
		return s;
	}

	public void clear() {
		//matchTimestamp.clear();
		lastSwitchFlowCount.clear();
		lastSwitchFlowCount.putAll(currentSwitchFlowCount);
		for(long swid : matchOnSwitchRecord.keySet()) {
			oldSwitchFlowCount.put(swid, (long)matchOnSwitchRecord.get(swid).size());
		}
		currentSwitchFlowCount.clear();
	}
	
	public MeasurementInfo makeCopy() {
		MeasurementInfo copy = new MeasurementInfo();
		copy.matchTimestamp.putAll(this.matchTimestamp);
		copy.lastSwitchFlowCount.putAll(this.lastSwitchFlowCount);
		copy.currentSwitchFlowCount.putAll(this.currentSwitchFlowCount);
		copy.oldSwitchFlowCount.putAll(this.oldSwitchFlowCount);
		copy.matchOnSwitchRecord.putAll(this.matchOnSwitchRecord);
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
	
	public ConcurrentHashMap<Long, Long> getOldFlowCount() {
		return this.oldSwitchFlowCount;
	}
}
