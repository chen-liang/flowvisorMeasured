package org.flowvisor.measurement;

import org.openflow.protocol.OFMatch;

public class Measurement {
	
	static Measurement instance;
	
	MeasurementInfo curentMeasurementInfo;
	
	public static void setInstance(Measurement instance) {
		Measurement.instance = instance;
	}
	
	public static Measurement getInstance() {
		return Measurement.instance;
	}
	
	public Measurement() {
		curentMeasurementInfo = new MeasurementInfo();
	}
	
	
	public MeasurementInfo getCurrentMeasurementInfoAndRefresh() {
		MeasurementInfo ret = curentMeasurementInfo.makeCopy();
		curentMeasurementInfo.clear();
		return ret;
	}
	
	
	public void recordPacketIn(OFMatch match, long swid, long timestamp) {
		curentMeasurementInfo.setPacketIn(match.clone(), swid, timestamp);
	}
	
	public void recordFlowProcess(long swid) {
		curentMeasurementInfo.incFlowProcess(swid);
	}
}
