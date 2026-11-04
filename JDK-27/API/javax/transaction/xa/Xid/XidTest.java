/*
 * @test
 * @summary javax.transaction.xa.Xid conformance: white-box structure + black-box behavior.
 * @run main XidTest
 */

import java.lang.reflect.Modifier;
import java.util.Arrays;
import javax.transaction.xa.Xid;

public class XidTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.transaction.xa.Xid");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isEnum() && !c.isAnnotation(), "not enum/annotation");
        check(c.getInterfaces().length == 0, "0 direct superinterfaces");
        check(c.getDeclaredMethods().length == 3, "declared method count 3");
        check(Xid.MAXGTRIDSIZE == 64, "MAXGTRIDSIZE == 64");
        check(Xid.MAXBQUALSIZE == 64, "MAXBQUALSIZE == 64");
    }

    static void testBlackBox() {
        final byte[] gtrid = new byte[]{1, 2, 3, 4, 5};
        final byte[] bqual = new byte[]{9, 8, 7};

        Xid xid = new Xid() {
            public int getFormatId() { return 0x1234; }
            public byte[] getGlobalTransactionId() { return gtrid; }
            public byte[] getBranchQualifier() { return bqual; }
        };

        check(xid instanceof Xid, "anonymous instanceof Xid");
        check(xid.getFormatId() == 0x1234, "getFormatId");
        check(Arrays.equals(xid.getGlobalTransactionId(), gtrid), "getGlobalTransactionId");
        check(Arrays.equals(xid.getBranchQualifier(), bqual), "getBranchQualifier");

        // a realistic Xid within the size limits
        final byte[] g = new byte[Xid.MAXGTRIDSIZE];
        final byte[] b = new byte[Xid.MAXBQUALSIZE];
        Xid bounded = new Xid() {
            public int getFormatId() { return 1; }
            public byte[] getGlobalTransactionId() { return g; }
            public byte[] getBranchQualifier() { return b; }
        };
        check(bounded.getGlobalTransactionId().length == Xid.MAXGTRIDSIZE, "gtrid at MAXGTRIDSIZE");
        check(bounded.getBranchQualifier().length == Xid.MAXBQUALSIZE, "bqual at MAXBQUALSIZE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
