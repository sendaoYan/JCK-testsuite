/*
 * @test
 * @summary javax.transaction.xa.XAResource conformance: white-box structure + black-box behavior.
 * @run main XAResourceTest
 */

import java.lang.reflect.Modifier;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class XAResourceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.transaction.xa.XAResource");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isEnum() && !c.isAnnotation(), "not enum/annotation");
        check(c.getInterfaces().length == 0, "0 direct superinterfaces");
        check(c.getDeclaredMethods().length == 10, "declared method count 10");

        check(XAResource.TMENDRSCAN == 8388608, "TMENDRSCAN == 8388608");
        check(XAResource.TMFAIL == 536870912, "TMFAIL == 536870912");
        check(XAResource.TMJOIN == 2097152, "TMJOIN == 2097152");
        check(XAResource.TMNOFLAGS == 0, "TMNOFLAGS == 0");
        check(XAResource.TMONEPHASE == 1073741824, "TMONEPHASE == 1073741824");
        check(XAResource.TMRESUME == 134217728, "TMRESUME == 134217728");
        check(XAResource.TMSTARTRSCAN == 16777216, "TMSTARTRSCAN == 16777216");
        check(XAResource.TMSUCCESS == 67108864, "TMSUCCESS == 67108864");
        check(XAResource.TMSUSPEND == 33554432, "TMSUSPEND == 33554432");
        check(XAResource.XA_RDONLY == 3, "XA_RDONLY == 3");
        check(XAResource.XA_OK == 0, "XA_OK == 0");
    }

    static void testBlackBox() throws Exception {
        final Xid xid = new Xid() {
            public int getFormatId() { return 1; }
            public byte[] getGlobalTransactionId() { return new byte[]{1, 2, 3}; }
            public byte[] getBranchQualifier() { return new byte[]{4, 5}; }
        };
        final boolean[] committed = {false};
        final int[] prepared = {-1};

        XAResource xa = new XAResource() {
            public void commit(Xid x, boolean onePhase) { committed[0] = onePhase; }
            public void end(Xid x, int flags) { }
            public void forget(Xid x) { }
            public int getTransactionTimeout() { return 30; }
            public boolean isSameRM(XAResource other) { return other == this; }
            public int prepare(Xid x) { prepared[0] = 1; return XA_OK; }
            public Xid[] recover(int flag) { return new Xid[]{xid}; }
            public void rollback(Xid x) { }
            public boolean setTransactionTimeout(int seconds) { return seconds > 0; }
            public void start(Xid x, int flags) { }
        };

        check(xa instanceof XAResource, "anonymous instanceof XAResource");
        check(xa.getTransactionTimeout() == 30, "getTransactionTimeout");
        check(xa.setTransactionTimeout(60), "setTransactionTimeout positive");
        check(!xa.setTransactionTimeout(0), "setTransactionTimeout non-positive");
        check(xa.isSameRM(xa), "isSameRM self");
        check(!xa.isSameRM(new XAResource() {
            public void commit(Xid x, boolean o) { } public void end(Xid x, int f) { } public void forget(Xid x) { }
            public int getTransactionTimeout() { return 0; } public boolean isSameRM(XAResource r) { return false; }
            public int prepare(Xid x) { return 0; } public Xid[] recover(int f) { return null; }
            public void rollback(Xid x) { } public boolean setTransactionTimeout(int s) { return false; }
            public void start(Xid x, int f) { }
        }), "isSameRM other false");

        check(xa.prepare(xid) == XAResource.XA_OK, "prepare returns XA_OK");
        check(prepared[0] == 1, "prepare invoked");
        Xid[] recovered = xa.recover(XAResource.TMSTARTRSCAN);
        check(recovered.length == 1 && recovered[0] == xid, "recover returns xid array");

        xa.commit(xid, true);
        check(committed[0], "commit invoked with onePhase");
        xa.start(xid, XAResource.TMNOFLAGS);
        xa.end(xid, XAResource.TMSUCCESS);
        xa.rollback(xid);
        xa.forget(xid);
        check(true, "start/end/rollback/forget callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
