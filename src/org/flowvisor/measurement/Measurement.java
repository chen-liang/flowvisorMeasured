package org.flowvisor.measurement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.openflow.protocol.OFMatch;

public class Measurement {
	
	static Measurement instance;
	
	MeasurementInfo curentMeasurementInfo;
		
	private static int intervalMsec = 50;
	private static String logPath = "/home/chen/fvflowmodCount/";
	
	ConcurrentHashMap<String, Long> flowCounter;
	
	ConcurrentHashMap<String, BufferedWriter> flowCountLogger;
	
	public static void setInstance(Measurement instance) {
		Measurement.instance = instance;
	}
	
	public static Measurement getInstance() {
		return Measurement.instance;
	}
	
	public Measurement() {
		curentMeasurementInfo = new MeasurementInfo();
		flowCounter = new ConcurrentHashMap<String, Long>();
		flowCountLogger = new ConcurrentHashMap<String, BufferedWriter>();
		Thread worker = new Thread(new FlowmodCounter());
		worker.start();
	}
	
	class FlowmodCounter implements Runnable {
		@Override
		public void run() {
			while(true) {
				try {
					for(String sname : flowCountLogger.keySet()) {
						BufferedWriter out = flowCountLogger.get(sname);
						long count = 0;
						if(flowCounter.containsKey(sname))
							count = flowCounter.get(sname);
						out.write(System.currentTimeMillis() + ":" + count + "\n");
						out.flush();
						flowCounter.put(sname, (long)0);
					}
					Thread.sleep(intervalMsec);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public MeasurementInfo getCurrentMeasurementInfoAndRefresh() {
		MeasurementInfo ret = curentMeasurementInfo.makeCopy();
		curentMeasurementInfo.clear();
		return ret;
	}
	
	
	public void recordPacketIn(OFMatch match, long swid, long timestamp) {
		curentMeasurementInfo.setPacketIn(match.clone(), swid, timestamp);
	}
	
	public void recordFlowProcess(long swid, boolean inc, OFMatch match) {
		if(inc == true)
			curentMeasurementInfo.incFlowProcess(swid, match);
		else
			curentMeasurementInfo.decFlowProcess(swid, match);
	}
	
	public synchronized void countFlowMod(String sname) {
		if(flowCounter.containsKey(sname)) {
			flowCounter.put(sname, flowCounter.get(sname) + 1);
		} else {
			flowCounter.put(sname, (long)1);
			try {
				File f = new File(logPath + sname);
				if(!f.exists()) {
					f.createNewFile();
				}
				FileWriter fstream = new FileWriter(f.getAbsoluteFile());
				BufferedWriter out = new BufferedWriter(fstream);
				flowCountLogger.put(sname, out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
