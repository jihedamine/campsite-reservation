package campsite.reservation.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Striped lock, allows synchronizing on a subset of locks
 */
public class StripedLocks {
    private final static Logger logger = LoggerFactory.getLogger(StripedLocks.class.getName());

    private Lock[] locks;
    private int maxWaitSeconds;

    /**
     * Builds a StripedLocks object
     * @param nbLocks number of locks
     * @param maxWaitSeconds number of seconds to wait to acquire a lock
     */
    public StripedLocks(int nbLocks, int maxWaitSeconds) {
        this.maxWaitSeconds = maxWaitSeconds;
        locks = new Lock[nbLocks];
        for (int i = 0; i < locks.length; i++) {
            locks[i] = new ReentrantLock();
        }
    }

    /**
     * Runs a runnable if able to acquire the locks of the striped lock that are between two indexes
     * @param lockStartIndex index of the first lock to acquire
     * @param lockEndIndex index of the last lock to acquire
     * @param runnable runnable to run was locks are acquired
     */
    public void runSync(int lockStartIndex, int lockEndIndex, Runnable runnable) {
        try {
            getLocksRecursively(lockStartIndex, lockEndIndex, runnable);
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed to execute operation on reservations due to high volume," +
                    " please try again later");
        }
    }

    private void getLocksRecursively(int currentIdx, int endIdx, Runnable runnable) throws InterruptedException {
        if (locks[currentIdx].tryLock(maxWaitSeconds, TimeUnit.SECONDS)) {
            try {
                if (currentIdx == endIdx) {
                    logger.debug("Acquired all locks, will run runnable");
                    runnable.run();
                } else {
                    logger.debug("Acquired lock " + currentIdx);
                    getLocksRecursively(currentIdx + 1, endIdx, runnable);
                }
            } finally {
                logger.debug("Releasing lock " + currentIdx);
                locks[currentIdx].unlock();
            }
        }
    }

}
