/*
 * @test
 * @summary javax.management.BadBinaryOpValueExpException conformance: full
 *          contract coverage. getExp returns the problematic ValueExp, toString
 *          includes it, and Exception hierarchy.
 * @run main BadBinaryOpValueExpExceptionTest
 */

import javax.management.BadBinaryOpValueExpException;
import javax.management.Query;
import javax.management.ValueExp;

public class BadBinaryOpValueExpExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        ValueExp exp = Query.value(7);
        BadBinaryOpValueExpException x = new BadBinaryOpValueExpException(exp);
        check(x instanceof Exception, "extends Exception");
        check(x.getExp() == exp, "getExp returns exp");
        check(x.toString().contains(exp.toString()), "toString includes exp");

        BadBinaryOpValueExpException n = new BadBinaryOpValueExpException(null);
        check(n.getExp() == null, "null exp allowed");
        testBBOExactGetter();
        testBBOHierarchyAndSerialization();
        testBBOMismatchIsCCE();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch e): exact getter identity, null handling,
    //      serialization, serialVersionUID, mixed-type sibling behavior ----

    static void testBBOExactGetter() {
        ValueExp exp = Query.value(7);
        BadBinaryOpValueExpException x = new BadBinaryOpValueExpException(exp);
        check(x.getExp() == exp, "getExp returns the very same ValueExp instance");
        check("BadBinaryOpValueExpException: 7".equals(x.toString()), "toString includes exp");
        BadBinaryOpValueExpException n = new BadBinaryOpValueExpException(null);
        check(n.getExp() == null, "null exp allowed");
        check("BadBinaryOpValueExpException: null".equals(n.toString()), "null exp toString");
    }

    static void testBBOHierarchyAndSerialization() throws Exception {
        BadBinaryOpValueExpException x = new BadBinaryOpValueExpException(Query.value(1));
        check(x instanceof Exception && x instanceof java.io.Serializable, "Exception + Serializable");
        check(x.getCause() == null, "no cause");
        check(x.getMessage() == null, "no detail message");
        long uid = java.io.ObjectStreamClass.lookup(BadBinaryOpValueExpException.class).getSerialVersionUID();
        check(uid == 5068475589449021227L, "serialVersionUID locked: " + uid);
        BadBinaryOpValueExpException back = (BadBinaryOpValueExpException) roundTripBB(x);
        check(back.getExp() != null && "1".equals(back.getExp().toString()), "round-trip keeps exp value");
    }

    static void testBBOMismatchIsCCE() throws Exception {
        // sibling behavior recorded: mixed-type operands in arithmetic apply
        // surface as unchecked ClassCastException, not as this exception
        try {
            Query.plus(Query.value(1), Query.value("a")).apply(null);
            check(false, "expected ClassCastException for mixed-type plus");
        } catch (java.lang.ClassCastException e) {
            passed++;
        }
    }

    static Object roundTripBB(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        return ois.readObject();
    }
}
