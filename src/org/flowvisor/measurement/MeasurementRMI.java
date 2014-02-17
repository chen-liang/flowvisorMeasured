package org.flowvisor.measurement;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MeasurementRMI extends Remote {
	public MeasurementInfo getCurrentMeasurementInfoAndRefresh() throws RemoteException;
}
