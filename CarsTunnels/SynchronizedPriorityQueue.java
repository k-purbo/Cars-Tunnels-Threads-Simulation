package cs131.pa2.CarsTunnels;

import java.util.Comparator;
import java.util.PriorityQueue;

public class SynchronizedPriorityQueue<E> extends PriorityQueue<E>{
		
	public SynchronizedPriorityQueue (Comparator<E> comparator) {
		super(comparator);
	}
	
	@Override
	public synchronized boolean add(E e) {
		return super.add(e);
	}
	
	@Override
	public synchronized boolean remove(Object e) {
		return super.remove(e);
	}
	
//	public synchronized void clear() {
//		queue.clear();
//	}
//
//	public synchronized boolean contains(Object e) {
//		return queue.contains(e);
//	}
//	
//	public synchronized E peek() {
//		return queue.peek();
//	}
//	
//	public synchronized int size() {
//		return queue.size();
//	}
}