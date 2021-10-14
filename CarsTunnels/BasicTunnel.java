package cs131.pa2.CarsTunnels;

import java.util.LinkedList;
import java.util.concurrent.locks.*;
import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;

public class BasicTunnel extends Tunnel{

	private final Lock lock;
	private final Condition condition;
	private boolean ambulancePresent;
	//list of vehicles in tunnel
	public LinkedList<Vehicle> activeVehicles;


	public BasicTunnel(String name) {
		super(name);
		activeVehicles = new LinkedList<Vehicle>();
		lock = new ReentrantLock();
		condition = lock.newCondition();
		ambulancePresent = false;

	}

	@Override
	public synchronized boolean tryToEnterInner(Vehicle vehicle) {
		//sled requires empty tunnel
		//car needs requirements to be met or empty tunnel
		if (canEnter(vehicle)) {
			vehicle.setLock(lock);
			vehicle.setCondition(condition);
			if (vehicle instanceof Ambulance) {
				ambulancePresent = true;
				activeVehicles.add(vehicle);
			}
			else {
				activeVehicles.add(vehicle);
			}
			return true;
		}
		else {
			return false;
		}
	}

	public synchronized boolean canEnter(Vehicle vehicle) {
		if (vehicle instanceof Ambulance && !ambulancePresent) {
			return true;
		}
		else if (!activeVehicles.isEmpty()) {
			Vehicle v1 = activeVehicles.getFirst();
			//check constraints for non-empty tunnel
			if (activeVehicles.size() < 3 && vehicle instanceof Car 
					&& v1 instanceof Car && v1.getDirection() == vehicle.getDirection()) {
				return true;
			}
		} else {
			//empty tunnel
			return true;
		}
		return false;
	}
	@Override
	public synchronized void exitTunnelInner(Vehicle vehicle) {
		if (activeVehicles.contains(vehicle)) {
			activeVehicles.remove(vehicle);
			if (vehicle instanceof Ambulance) {
				ambulancePresent = false;
			}
		}
	}
}