package org.flowvisor.measurement;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class MeasurementRMIImpl extends UnicastRemoteObject implements MeasurementRMI {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Measurement measurementRef;
	
	public MeasurementRMIImpl(Measurement measurementRef) throws RemoteException {
		super();
		this.measurementRef = measurementRef;
	}
	
	@Override
	public MeasurementInfo getCurrentMeasurementInfoAndRefresh() throws RemoteException {
		return measurementRef.getCurrentMeasurementInfoAndRefresh();
	}

}
