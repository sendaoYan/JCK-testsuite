/*
 * @test
 * @summary javax.management.relation.RoleInfo conformance: name/degree/readable.
 * @run main RoleInfoTest
 */

import javax.management.relation.*;
public class RoleInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        RoleInfo ri = new RoleInfo("rname", "java.lang.String");
        check("rname".equals(ri.getName()), "name");
        check(ri.isReadable() && ri.isWritable(), "readable/writable");
        check(ri.getMinDegree() == 1 && ri.getMaxDegree() == 1, "degree");
        check(ri.checkMinDegree(1) && ri.checkMaxDegree(1), "check degree");
        testConstructorMatrixAndDefaults();
        testDegreeValidationMatrix();
        testNullMatrix();
        testCheckDegreeEdges();
        testEqualsHashCodeIdentity();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch j): full contract coverage ----

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // all four constructors: explicit fields + defaulted fields + copy constructor
    static void testConstructorMatrixAndDefaults() throws Exception {
        RoleInfo full = new RoleInfo("full", "com.example.Foo", true, false, 2, 5, "a description");
        check("full".equals(full.getName()), "7-arg name");
        check(full.isReadable(), "7-arg readable");
        check(!full.isWritable(), "7-arg writable");
        check(full.getMinDegree() == 2, "7-arg min degree");
        check(full.getMaxDegree() == 5, "7-arg max degree");
        check("a description".equals(full.getDescription()), "7-arg description");
        check("com.example.Foo".equals(full.getRefMBeanClassName()), "7-arg ref class name");
        // 4-arg: min=max=1, description null
        RoleInfo four = new RoleInfo("four", "com.example.Foo", false, true);
        check(four.getMinDegree() == 1 && four.getMaxDegree() == 1, "4-arg degrees default to 1");
        check(four.getDescription() == null, "4-arg description defaults to null");
        check(!four.isReadable() && four.isWritable(), "4-arg flags round-trip");
        // 2-arg: readable/writable true as well
        RoleInfo two = new RoleInfo("two", "com.example.Foo");
        check(two.isReadable() && two.isWritable(), "2-arg flags default to true");
        check(two.getMinDegree() == 1 && two.getMaxDegree() == 1, "2-arg degrees default to 1");
        check(two.getDescription() == null, "2-arg description defaults to null");
        // copy constructor copies every field, including flags and description
        RoleInfo copy = new RoleInfo(full);
        check(copy != full, "copy ctor yields a distinct instance");
        check("full".equals(copy.getName()), "copy name");
        check(copy.isReadable() && !copy.isWritable(), "copy flags");
        check(copy.getMinDegree() == 2 && copy.getMaxDegree() == 5, "copy degrees");
        check("a description".equals(copy.getDescription()), "copy description");
        check("com.example.Foo".equals(copy.getRefMBeanClassName()), "copy ref class name");
        // copy of an unbounded cardinality info keeps -1/-1
        RoleInfo inf = new RoleInfo("inf", "cls", true, true,
                RoleInfo.ROLE_CARDINALITY_INFINITY, RoleInfo.ROLE_CARDINALITY_INFINITY, null);
        RoleInfo infCopy = new RoleInfo(inf);
        check(infCopy.getMinDegree() == -1 && infCopy.getMaxDegree() == -1, "copy keeps infinity degrees");
        check(infCopy.getDescription() == null, "copy of null description stays null");
        // readable=false AND writable=false is accepted (no accessibility validation in RoleInfo)
        RoleInfo noAccess = new RoleInfo("na", "cls", false, false);
        check(!noAccess.isReadable() && !noAccess.isWritable(), "both-false accessibility accepted");
        // empty strings are legal (only null is rejected)
        RoleInfo empty = new RoleInfo("", "");
        check("".equals(empty.getName()) && "".equals(empty.getRefMBeanClassName()), "empty name/class accepted");
        // the class name is never loaded: a nonexistent class is fine (declared exceptions are dead)
        RoleInfo ghost = new RoleInfo("g", "no.such.Class.Anywhere");
        check("no.such.Class.Anywhere".equals(ghost.getRefMBeanClassName()), "nonexistent ref class accepted without loading");
        check(RoleInfo.ROLE_CARDINALITY_INFINITY == -1, "ROLE_CARDINALITY_INFINITY == -1");
    }

    // degree validation: min>max and out-of-range both raise InvalidRoleInfoException (not IAE)
    static void testDegreeValidationMatrix() throws Exception {
        int[][] badGt = { {3, 2}, {-1, 5}, {1, -2}, {2, 1}, {Integer.MAX_VALUE, Integer.MAX_VALUE - 1} };
        for (int[] d : badGt) {
            try {
                new RoleInfo("n", "cls", true, true, d[0], d[1], null);
                check(false, "min=" + d[0] + ",max=" + d[1] + " must throw");
            } catch (InvalidRoleInfoException e) {
                check(("Minimum degree " + d[0] + " is greater than maximum degree " + d[1]).equals(e.getMessage()),
                      "min>max exact msg for (" + d[0] + "," + d[1] + "), got: " + e.getMessage());
            }
        }
        int[][] badRange = { {-2, 1}, {-2, -2}, {Integer.MIN_VALUE, 1} };
        for (int[] d : badRange) {
            try {
                new RoleInfo("n", "cls", true, true, d[0], d[1], null);
                check(false, "min=" + d[0] + ",max=" + d[1] + " must throw for range");
            } catch (InvalidRoleInfoException e) {
                check("Minimum or maximum degree has an illegal value, must be [0, ROLE_CARDINALITY_INFINITY]."
                      .equals(e.getMessage()), "range exact msg for (" + d[0] + "," + d[1] + "), got: " + e.getMessage());
            }
        }
        // legal boundaries: both infinity, 0/0, 0/infinity, N/infinity, min==max
        int[][] good = { {-1, -1}, {0, 0}, {0, -1}, {5, -1}, {4, 4} };
        for (int[] d : good) {
            RoleInfo ri = new RoleInfo("n", "cls", true, true, d[0], d[1], null);
            check(ri.getMinDegree() == d[0] && ri.getMaxDegree() == d[1],
                  "degrees (" + d[0] + "," + d[1] + ") accepted verbatim");
        }
        // the short constructors use min=max=1 so they can never raise InvalidRoleInfoException
        check(new RoleInfo("n", "cls", true, true).getMinDegree() == 1, "4-arg never throws IRIE (min==max)");
        check(new RoleInfo("n", "cls").getMaxDegree() == 1, "2-arg never throws IRIE (min==max)");
    }

    // null matrix with exact messages (IAE "Invalid parameter." for every null)
    static void testNullMatrix() throws Exception {
        try { new RoleInfo(null, "cls"); check(false, "null name must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "null name msg"); }
        try { new RoleInfo("n", (String) null); check(false, "null ref class must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "null ref class msg"); }
        try { new RoleInfo(null, null, true, true); check(false, "both null 4-arg must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "both null 4-arg msg"); }
        try { new RoleInfo(null, null, true, true, 1, 1, null); check(false, "both null 7-arg must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "both null 7-arg msg"); }
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { new RoleInfo((RoleInfo) null); }
        }, "null copy-ctor argument must throw IAE");
        try { new RoleInfo(null, "cls", true, true, 3, 2, null); check(false, "null name with bad degrees must throw IAE first"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "null check precedes degree check"); }
    }

    // checkMinDegree / checkMaxDegree boundary semantics including ROLE_CARDINALITY_INFINITY
    static void testCheckDegreeEdges() throws Exception {
        RoleInfo inf = new RoleInfo("inf", "cls", true, true, -1, -1, null);
        check(!inf.checkMinDegree(-2) && !inf.checkMaxDegree(-2), "value < ROLE_CARDINALITY_INFINITY always false");
        check(inf.checkMinDegree(-1) && inf.checkMaxDegree(-1), "infinity degrees accept the infinity value itself");
        check(inf.checkMinDegree(0) && inf.checkMaxDegree(0), "infinity min accepts 0");
        check(inf.checkMinDegree(7) && inf.checkMaxDegree(1000000), "infinity degrees accept any count");
        RoleInfo m13 = new RoleInfo("m13", "cls", true, true, 1, 3, null);
        check(!m13.checkMinDegree(-1), "finite min rejects -1");
        check(!m13.checkMinDegree(0), "below min rejected");
        check(m13.checkMinDegree(1) && m13.checkMinDegree(2), "at/above min accepted");
        check(!m13.checkMaxDegree(-1), "finite max rejects -1 even though -1 <= max");
        check(m13.checkMaxDegree(0) && m13.checkMaxDegree(3), "within max accepted");
        check(!m13.checkMaxDegree(4), "above max rejected");
        RoleInfo m0 = new RoleInfo("m0", "cls", true, true, 0, 0, null);
        check(m0.checkMinDegree(0) && m0.checkMaxDegree(0), "0/0 degrees accept exactly 0");
        check(m0.checkMinDegree(1) && !m0.checkMaxDegree(1),
              "min 0 accepts any non-negative count; max 0 rejects 1");
        // mixed finite-min / infinite-max
        RoleInfo mix = new RoleInfo("mix", "cls", true, true, 2, -1, null);
        check(!mix.checkMinDegree(1) && mix.checkMinDegree(2), "finite min honoured with infinite max");
        check(mix.checkMaxDegree(1000000), "infinite max accepts any count");
    }

    // RoleInfo does not override equals/hashCode: identity semantics
    static void testEqualsHashCodeIdentity() throws Exception {
        RoleInfo a = new RoleInfo("same", "cls", true, true, 1, 3, "d");
        RoleInfo b = new RoleInfo("same", "cls", true, true, 1, 3, "d");
        check(!a.equals(b), "no equals override: same-content RoleInfos are not equal");
        check(a.equals(a), "identity equals is reflexive");
        check(!a.equals(new RoleInfo("other", "cls", true, true, 1, 3, "d")), "different name unequal (identity)");
        check(a.hashCode() == System.identityHashCode(a), "hashCode is the Object identity hash");
        // toString exact format
        check(("role info name: same; isReadable: true; isWritable: true; description: d; "
              + "minimum degree: 1; maximum degree: 3; MBean class: cls").equals(a.toString()),
              "toString exact format, got: " + a.toString());
        check(("role info name: e; isReadable: false; isWritable: false; description: null; "
              + "minimum degree: 1; maximum degree: 1; MBean class: c")
              .equals(new RoleInfo("e", "c", false, false).toString()),
              "toString with null description renders 'null'");
    }

    // serialization round-trip of all seven fields + locked serialVersionUID
    static void testSerialization() throws Exception {
        RoleInfo orig = new RoleInfo("ser", "com.example.Ser", false, true, 2, 7, "serial desc");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(orig);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        RoleInfo back = (RoleInfo) ois.readObject();
        ois.close();
        check("ser".equals(back.getName()), "serial name");
        check(!back.isReadable() && back.isWritable(), "serial flags");
        check("serial desc".equals(back.getDescription()), "serial description");
        check(back.getMinDegree() == 2 && back.getMaxDegree() == 7, "serial degrees");
        check("com.example.Ser".equals(back.getRefMBeanClassName()), "serial ref class name");
        check(java.io.ObjectStreamClass.lookup(RoleInfo.class).getSerialVersionUID() == 2504952983494636987L,
              "RoleInfo serialVersionUID is 2504952983494636987L");
    }
}
