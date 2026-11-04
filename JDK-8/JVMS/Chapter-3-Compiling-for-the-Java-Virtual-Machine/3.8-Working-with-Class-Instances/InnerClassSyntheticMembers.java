/*
 * @test
 * @summary JVMS 3.8/4.7.6: the compiler synthesizes members for inner classes -
 *          a this$0 field referencing the enclosing instance, access$xxx
 *          methods for private member access, and the InnerClasses/enclosing
 *          class metadata.
 * @run main InnerClassSyntheticMembers
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class InnerClassSyntheticMembers {
    static int passed = 0;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    private int secret = 7;
    private int getSecret() { return secret; }

    class Inner {
        int readOuter() { return secret; }       // private field of the outer
        int callOuter() { return getSecret(); }  // private method of the outer
    }

    public static void main(String[] args) throws Exception {
        Class<?> inner = InnerClassSyntheticMembers.Inner.class;

        check(inner.getEnclosingClass() == InnerClassSyntheticMembers.class,
                "InnerClasses/enclosing-class metadata present");

        int this0 = 0;
        for (Field f : inner.getDeclaredFields())
            if (f.isSynthetic() && f.getName().startsWith("this$")) this0++;
        check(this0 == 1, "synthetic this$0 field present");

        int access = 0;
        for (Method m : InnerClassSyntheticMembers.class.getDeclaredMethods())
            if (m.isSynthetic() && m.getName().startsWith("access$")) access++;
        check(access >= 2, "synthetic access$xxx methods generated for private access");

        InnerClassSyntheticMembers outer = new InnerClassSyntheticMembers();
        Inner in = outer.new Inner();
        check(in.readOuter() == 7, "inner reads the private outer field");
        check(in.callOuter() == 7, "inner calls the private outer method");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
