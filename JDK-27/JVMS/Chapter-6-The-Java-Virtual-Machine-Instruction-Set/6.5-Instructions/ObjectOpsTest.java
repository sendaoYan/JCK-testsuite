/*
 * @test
 * @summary JVMS 6.5: object creation (new, dup) and field access (getfield,
 *          putfield), plus type checks instanceof and checkcast.
 * @compile ObjectOps.jasm
 * @run main ObjectOpsTest
 */

public class ObjectOpsTest {
    public static void main(String[] args) {
        Object o = ObjectOps.make(42);
        if (!(o instanceof ObjectOps)) throw new AssertionError("new/dup returned wrong type");
        ObjectOps obj = (ObjectOps) o;
        if (obj.getX() != 42) throw new AssertionError("getfield after <init>");
        obj.setX(99);
        if (obj.getX() != 99) throw new AssertionError("putfield/getfield");

        if (!ObjectOps.isStr("hello")) throw new AssertionError("instanceof String");
        if (ObjectOps.isStr(new Object())) throw new AssertionError("instanceof non-String");

        if (!ObjectOps.cast("hello").equals("hello")) throw new AssertionError("checkcast ok");
        try {
            ObjectOps.cast(new Object());
            throw new AssertionError("checkcast did not throw");
        } catch (ClassCastException e) { }

        System.out.println("PASS");
    }
}
