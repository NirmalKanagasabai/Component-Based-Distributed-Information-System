package LockManager;

import servercode.LockManager.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.BitSet;

import static org.junit.Assert.*;


public class LockManagerTest {

	int firstTID = 1;
	String firstStr = "car";

	int secondTID = 2;
	String secondStr = "flight";

	@Rule
	public final ExpectedException exception = ExpectedException.none();

    // test that a thread can convert to a write from a read if no other threads have a read
	public void lockConversionRtoW() {

        LockManager lm = new LockManager();

		try {

            boolean locked = lm.Lock(firstTID, firstStr, LockManager.WRITE);
            boolean converted = lm.Lock(firstTID, firstStr, LockManager.READ);

            assertTrue(converted);


        } catch (DeadlockException e) {

        }

    }

    // test that a thread cannot convert from a read to a write if other threads have a read
    @Test
    public void failLockConversionRtoW() {

        LockManager lm = new LockManager();

        try {

            boolean firstLock = lm.Lock(firstTID, firstStr, LockManager.READ);
            boolean secondLock = lm.Lock(secondTID, firstStr, LockManager.READ);

            assertTrue(firstLock);
            assertTrue(secondLock);

            DataObj obj = new DataObj(firstTID, firstStr, LockManager.WRITE);

            boolean cannotConvert = lm.LockConflict(obj, new BitSet());
            assertTrue(cannotConvert);

        } catch (DeadlockException | RedundantLockRequestException e) {

        }
    }

}
