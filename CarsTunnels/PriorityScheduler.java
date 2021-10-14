package cs131.pa2.CarsTunnels;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;
import cs131.pa2.Abstract.Log.Log;

public class PriorityScheduler extends Tunnel {
	
	private Collection<Tunnel> tunnels;
	private final Lock schedulerLock;
	private final Condition tunnelCondition;
	private final Condition priorityCondition;
	private final HashMap <Vehicle, Tunnel> vehicleMap;
	private SynchronizedPriorityQueue<Vehicle> waitingQueue;
	private final Lock lock;

	public PriorityScheduler(String name, Collection<Tunnel> tunnels) {
		super(name);
		this.tunnels = tunnels;
		lock = new ReentrantLock();
		schedulerLock = new ReentrantLock();
		tunnelCondition = schedulerLock.newCondition();
		priorityCondition = schedulerLock.newCondition();
		vehicleMap = new HashMap<Vehicle, Tunnel>();
		waitingQueue = new SynchronizedPriorityQueue<Vehicle>(Comparator.comparing(Vehicle::getPriority).reversed());
	}

	@Override
	public boolean tryToEnterInner(Vehicle vehicle) {
		waitingQueue.add(vehicle);
		schedulerLock.lock();
		try {
			boolean vehicleEntered = false;
			priorityCondition.await(1, TimeUnit.MILLISECONDS);
			while (!vehicleEntered) {
				while (vehicle.getPriority() < waitingQueue.peek().getPriority()) {
					priorityCondition.await();
				}
				for (Tunnel tunnel : tunnels) {
					if (tunnel.tryToEnter(vehicle)) {
						vehicleMap.put(vehicle, tunnel);
						waitingQueue.remove(vehicle);
						priorityCondition.signalAll();
						return true;
					}
				}
				if (!vehicleEntered) {
					tunnelCondition.await();
				}
				else {
					while (vehicle.getPriority() < waitingQueue.peek().getPriority()) {
						priorityCondition.await();
						vehicleEntered = false;
					}
				}
			}
			return true;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			schedulerLock.unlock();
		}
		return false;
	}

	@Override
	public void exitTunnelInner(Vehicle vehicle) {
		schedulerLock.lock();
		try {
			Tunnel tunnel = vehicleMap.get(vehicle);
			tunnel.exitTunnel(vehicle);
			vehicleMap.remove(vehicle);
			tunnelCondition.signalAll();
		} finally {
			schedulerLock.unlock();
		}
	}
}