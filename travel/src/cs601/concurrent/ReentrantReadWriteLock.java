package cs601.concurrent;

import java.util.HashMap;
import java.util.Map;

/**
 * A reentrant read/write lock that allows: 
 * 1) Multiple readers (when there is no writer).
 * 2) One writer (when nobody else is writing or reading). 
 * 3) A writer is allowed to acquire a read lock while holding the write lock. 
 * The assignment is based on the assignment of Prof. Rollins (original author).
 * The link is the info about making this class: 
 * http://tutorials.jenkov.com/java-concurrency/read-write-locks.html#simple
 */
public class ReentrantReadWriteLock {

	// TODO: Add instance variables : you need to keep track of the read lock holders and the write lock holders.
	// We should be able to find the number of read locks and the number of write locks 
	// a thread with the given threadId is holding 
	
	private final Map<Long, Integer> readingThreadMap;
	private final Map<Long, Integer> writingThreadMap; 

	
	
	
	
	
	
	
	
	
	/**
	 * Constructor for ReentrantReadWriteLock
	 */
	public ReentrantReadWriteLock() {
		// FILL IN CODE
		readingThreadMap = new HashMap<Long, Integer>();
		writingThreadMap = new HashMap<Long, Integer>();
	}

	
	
	
	
	
	
	
	
	
	/**
	 * Returns true if the current thread holds a read lock.
	 * 
	 * @return
	 */
	public synchronized boolean isReadLockHeldByCurrentThread() {
		// FILL IN CODE
		long threadId = Thread.currentThread().getId();
		
		
		// no thread holding the reading lock.
		if(readingThreadMap.size() == 0){
			return false;
		}
		
		
		// current thread does not have reading lock.
		if(readingThreadMap.get(threadId) == null){
			return false;
		}
		return true;
	}

	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Returns true if the current thread holds a write lock.
	 * 
	 * @return
	 */
	public synchronized boolean isWriteLockHeldByCurrentThread() {
		// FILL IN CODE
		long threadId = Thread.currentThread().getId();
		
		// no thread holding writing lock
		if(writingThreadMap.size() == 0){
			return false;
		}
		
		// there is thread holding writing lock and current thread holding writing lock. 
		if(writingThreadMap.get(threadId) != null && writingThreadMap.get(threadId) > 0){
			return true;
		}
		
		return false;
	}

	
	
	
	
	
	
	
	
	
	
	/**
	 * Non-blocking method that tries to acquire the read lock. Returns true
	 * if successful.
	 * 
	 * @return
	 */
	public synchronized boolean tryAcquiringReadLock() {
		// FILL IN CODE
		long threadId = Thread.currentThread().getId();
		int writerCount = writingThreadMap.size();
		
		// no writer OR only one writer and it is current thread.
		if(writerCount == 0 || (writerCount == 1 && isWriteLockHeldByCurrentThread())){
			if(readingThreadMap.get(threadId) == null){
				readingThreadMap.put(threadId, 1);
			}
			else{
				readingThreadMap.put(threadId, readingThreadMap.get(threadId) + 1);
			}
			return true;
		}
		
		return false;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Non-blocking method that tries to acquire the write lock. Returns true
	 * if successful.
	 * 
	 * @return
	 */
	public synchronized boolean tryAcquiringWriteLock() {
		// FILL IN CODE
		long threadId = Thread.currentThread().getId();
		int readerCount = readingThreadMap.size();
		int writerCount = writingThreadMap.size();
		
		// no writer and no reader.
		if(writerCount == 0){
			if(readerCount == 0){
				writingThreadMap.put(threadId, 1);
				return true;
			}
		}
		
		// just one writer and is current thread.
		else if(writerCount == 1){
			if(isWriteLockHeldByCurrentThread()){
				writingThreadMap.put(threadId, writingThreadMap.get(threadId) + 1);
				return true;
			}
		}
//		System.out.println("Get write lock fail");
		return false; 
	}

	
	
	
	
	
	
	
	
	/**
	 * Blocking method - calls tryAcquiringReadLock and returns only when the read lock has been
	 * acquired, otherwise waits.
	 * 
	 * @throws InterruptedException
	 */
	public synchronized void lockRead() {
		// FILL IN CODE
		
		// while the lock is acquired, just wait.
		while(!tryAcquiringReadLock()){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	
	
	
	
	
	
	
	
	
	
	

	/**
	 * Releases the read lock held by the current thread. 
	 */
	public synchronized void unlockRead() {
		// FILL IN CODE
		long threadId = Thread.currentThread().getId();
		
		
		// if there is no thread holding reading lock, just return.
		if(readingThreadMap.get(threadId) == null){
			return;
		}
		int threadReader = readingThreadMap.get(threadId);
		
		// if reading lock is held by current thread.
		if(isWriteLockHeldByCurrentThread()){
			if(threadReader == 1){
				readingThreadMap.remove(threadId);
			}
			else{
				readingThreadMap.put(threadId, threadReader - 1);
			}
			
		}
		notify();
	}

	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Blocking method that calls tryAcquiringWriteLock and returns only when the write lock has been
	 * acquired, otherwise waits.
	 * 
	 * @throws InterruptedException
	 */
	public synchronized void lockWrite() {
		// FILL IN CODE
		
		// while write lock is acquired, just wait.
		while(!tryAcquiringWriteLock()){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Releases the write lock held by the current thread. 
	 */

	public synchronized void unlockWrite() {
		// FILL IN CODE
		long threadId = Thread.currentThread().getId();
		
		// if there is no thread holding writing lock, just return.
		if(writingThreadMap.get(threadId) == null){
			return;
		}
		int threadWriter = writingThreadMap.get(threadId);
		
		// current thread holds writing lock.
		if(isWriteLockHeldByCurrentThread()){
			if(threadWriter == 1){
				writingThreadMap.remove(threadId);
			}
			else{
				writingThreadMap.put(threadId, threadWriter - 1);
			}
		}
		notify();	
	}

	
}
