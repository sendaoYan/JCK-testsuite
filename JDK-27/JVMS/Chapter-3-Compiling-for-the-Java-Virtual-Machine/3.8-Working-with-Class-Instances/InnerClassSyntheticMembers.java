/*
 * @test
 * @summary JVMS 3.8/4.7.6/4.7.28-4.7.29: the compiler synthesizes a this$0
 *          field for a non-static inner class and records the enclosing class,
 *          but because the inner and outer classes are nestmates (JDK 11+)
 *          private member access needs no access$xxx bridge methods.
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

        // Nestmates: inner and outer share a nest, so private access is direct.
        check(inner.getNestHost() == InnerClassSyntheticMembers.class,
                "NestHost attribute names the outer class as the nest host");
        check(InnerClassSyntheticMembers.class.isNestmateOf(inner),
                "inner and outer classes are nestmates");

        int access = 0;
        for (Method m : InnerClassSyntheticMembers.class.getDeclaredMethods())
            if (m.isSynthetic() && m.getName().startsWith("access$")) access++;
        check(access == 0, "nestmates avoid synthetic access$xxx methods");

        InnerClassSyntheticMembers outer = new InnerClassSyntheticMembers();
        Inner in = outer.new Inner();
        check(in.readOuter() == 7, "inner reads the private outer field");
        check(in.callOuter() == 7, "inner calls the private outer method");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
