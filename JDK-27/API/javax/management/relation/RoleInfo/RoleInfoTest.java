/*
 * @test
 * @summary javax.management.relation.RoleInfo conformance: white-box structure + black-box behavior.
 * @run main RoleInfoTest
 */
import javax.management.relation.InvalidRoleInfoException;
import javax.management.relation.RoleInfo;

public class RoleInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.relation.RoleInfo");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "RoleInfo must be public");
        check(!c.isInterface(), "RoleInfo must be a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "RoleInfo must be concrete");
        check(java.io.Serializable.class.isAssignableFrom(c), "RoleInfo must implement Serializable");
        check(c.getField("ROLE_CARDINALITY_INFINITY").getInt(null) == -1, "ROLE_CARDINALITY_INFINITY == -1");
        c.getConstructor(String.class, String.class, boolean.class, boolean.class, int.class, int.class, String.class);
        c.getConstructor(String.class, String.class, boolean.class, boolean.class);
        c.getConstructor(String.class, String.class);
        c.getConstructor(RoleInfo.class);
        c.getMethod("getName"); c.getMethod("isReadable"); c.getMethod("isWritable");
        c.getMethod("getDescription"); c.getMethod("getMinDegree"); c.getMethod("getMaxDegree");
        c.getMethod("getRefMBeanClassName"); c.getMethod("checkMinDegree", int.class);
        c.getMethod("checkMaxDegree", int.class); c.getMethod("toString");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // full constructor
        RoleInfo full = new RoleInfo("r", "java.lang.String", true, false, 1, 3, "desc");
        check("r".equals(full.getName()), "full ctor name");
        check(full.isReadable(), "full ctor readable");
        check(!full.isWritable(), "full ctor writable");
        check("desc".equals(full.getDescription()), "full ctor description");
        check(full.getMinDegree() == 1, "full ctor min");
        check(full.getMaxDegree() == 3, "full ctor max");
        check("java.lang.String".equals(full.getRefMBeanClassName()), "full ctor ref class");

        // 4-arg constructor defaults min=max=1, description null
        RoleInfo four = new RoleInfo("r", "java.lang.String", true, false);
        check(four.getMinDegree() == 1 && four.getMaxDegree() == 1, "4-arg ctor defaults min/max to 1");
        check(four.getDescription() == null, "4-arg ctor defaults description to null");

        // 2-arg constructor defaults read=write=true, min=max=1, desc null
        RoleInfo two = new RoleInfo("r", "java.lang.String");
        check(two.isReadable() && two.isWritable(), "2-arg ctor defaults read/write to true");
        check(two.getMinDegree() == 1 && two.getMaxDegree() == 1, "2-arg ctor min/max 1");
        check(two.getDescription() == null, "2-arg ctor description null");

        // copy constructor
        RoleInfo copy = new RoleInfo(full);
        check(copy != full, "copy ctor is a distinct instance");
        check("r".equals(copy.getName()) && copy.getMinDegree() == 1 && copy.getMaxDegree() == 3
              && "java.lang.String".equals(copy.getRefMBeanClassName()), "copy ctor copies all fields");

        // cardinality checks
        check(!full.checkMinDegree(0) && full.checkMinDegree(1) && full.checkMinDegree(3), "checkMinDegree bounds");
        check(full.checkMaxDegree(3) && !full.checkMaxDegree(4), "checkMaxDegree bounds");
        RoleInfo inf = new RoleInfo("i", "java.lang.String", true, true, RoleInfo.ROLE_CARDINALITY_INFINITY, RoleInfo.ROLE_CARDINALITY_INFINITY, "d");
        check(inf.checkMinDegree(5), "infinite min accepts any non-negative");
        check(inf.checkMaxDegree(5), "infinite max accepts any non-negative");

        // toString
        check("role info name: r; isReadable: true; isWritable: false; description: desc; minimum degree: 1; maximum degree: 3; MBean class: java.lang.String"
              .equals(full.toString()), "toString format, got: " + full.toString());

        // exceptions
        try { new RoleInfo(null, "java.lang.String"); check(false, "null name must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "null name message"); }
        try { new RoleInfo("r", (String) null); check(false, "null class must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "null class message"); }
        try { new RoleInfo((RoleInfo) null); check(false, "null copy must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "null copy message"); }
        try { new RoleInfo("r", "java.lang.String", true, true, 3, 2, "d"); check(false, "min>max must throw"); }
        catch (InvalidRoleInfoException e) { check("Minimum degree 3 is greater than maximum degree 2".equals(e.getMessage()), "min>max message: " + e.getMessage()); }
    }

    // ---- deepened (batch j): degree validation matrix, edges, identity equals, serialization ----

    static void testDegreeValidationMatrix() throws Exception {
        // first validation branch: min==-1 with finite max, or min>max (exact message per pair)
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
        // second branch: a degree below ROLE_CARDINALITY_INFINITY(-1) other than via min>max
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
        // copy constructor preserves unbounded degrees
        RoleInfo inf = new RoleInfo("i", "cls", true, true, -1, -1, null);
        RoleInfo infCopy = new RoleInfo(inf);
        check(infCopy.getMinDegree() == -1 && infCopy.getMaxDegree() == -1, "copy keeps infinity degrees");
    }

    static void testAdditionalEdges() throws Exception {
        // readable=false AND writable=false is accepted (RoleInfo performs no accessibility validation)
        RoleInfo noAccess = new RoleInfo("na", "cls", false, false);
        check(!noAccess.isReadable() && !noAccess.isWritable(), "both-false accessibility accepted");
        // empty strings are legal (only null is rejected)
        RoleInfo empty = new RoleInfo("", "");
        check("".equals(empty.getName()) && "".equals(empty.getRefMBeanClassName()), "empty name/class accepted");
        // the ref class is never loaded: a nonexistent class name is fine
        check("no.such.Class.Anywhere".equals(new RoleInfo("g", "no.such.Class.Anywhere").getRefMBeanClassName()),
              "nonexistent ref class accepted without loading");
        // null check precedes the degree check
        try { new RoleInfo(null, "cls", true, true, 3, 2, null); check(false, "null name with bad degrees must throw IAE"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "null check precedes degree check"); }
        // checkMinDegree / checkMaxDegree negative-value edges (both verified on JDK-27)
        RoleInfo inf = new RoleInfo("inf", "cls", true, true, -1, -1, null);
        check(!inf.checkMinDegree(-2) && !inf.checkMaxDegree(-2), "value < ROLE_CARDINALITY_INFINITY always false");
        check(inf.checkMinDegree(-1) && inf.checkMaxDegree(-1), "infinity degrees accept the infinity value itself");
        check(inf.checkMinDegree(0) && inf.checkMaxDegree(1000000), "infinity degrees accept any count");
        RoleInfo m13 = new RoleInfo("m13", "cls", true, true, 1, 3, null);
        check(!m13.checkMinDegree(-1), "finite min rejects -1");
        check(!m13.checkMaxDegree(-1), "finite max rejects -1 even though -1 <= max");
        RoleInfo m0 = new RoleInfo("m0", "cls", true, true, 0, 0, null);
        check(m0.checkMinDegree(1) && !m0.checkMaxDegree(1),
              "min 0 accepts any non-negative count; max 0 rejects 1");
        RoleInfo mix = new RoleInfo("mix", "cls", true, true, 2, -1, null);
        check(!mix.checkMinDegree(1) && mix.checkMinDegree(2), "finite min honoured with infinite max");
        check(mix.checkMaxDegree(1000000), "infinite max accepts any count");
        // RoleInfo does not override equals/hashCode: identity semantics
        RoleInfo a = new RoleInfo("same", "cls", true, true, 1, 3, "d");
        RoleInfo b = new RoleInfo("same", "cls", true, true, 1, 3, "d");
        check(!a.equals(b), "no equals override: same-content RoleInfos are not equal");
        check(a.hashCode() == System.identityHashCode(a), "hashCode is the Object identity hash");
        // toString renders a null description as the literal "null"
        check(("role info name: e; isReadable: false; isWritable: false; description: null; "
              + "minimum degree: 1; maximum degree: 1; MBean class: c")
              .equals(new RoleInfo("e", "c", false, false).toString()),
              "toString with null description renders 'null'");
    }

    static void testSerialization() throws Exception {
        RoleInfo orig = new RoleInfo("ser", "com.example.Ser", false, true, 2, 7, "serial desc");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(orig);
        oos.close();
        RoleInfo back = (RoleInfo) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check("ser".equals(back.getName()), "serial name");
        check(!back.isReadable() && back.isWritable(), "serial flags");
        check("serial desc".equals(back.getDescription()), "serial description");
        check(back.getMinDegree() == 2 && back.getMaxDegree() == 7, "serial degrees");
        check("com.example.Ser".equals(back.getRefMBeanClassName()), "serial ref class name");
        check(java.io.ObjectStreamClass.lookup(RoleInfo.class).getSerialVersionUID() == 2504952983494636987L,
              "RoleInfo serialVersionUID is 2504952983494636987L");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDegreeValidationMatrix();
        testAdditionalEdges();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
