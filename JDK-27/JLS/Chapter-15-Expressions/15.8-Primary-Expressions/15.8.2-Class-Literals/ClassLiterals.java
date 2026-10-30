/*
 * @test
 * @summary JLS 15.8.2: a class literal evaluates to the Class object; the type of
 *          p.class for a primitive type p is Class of the boxed type, and
 *          void.class has type Class<Void>.
 * @run main ClassLiterals
 */

public class ClassLiterals {
    public static void main(String[] args) {
        Class<String> cs = String.class;
        Class<Integer> ci = int.class;      // primitive -> boxed Class<Integer>
        Class<Void> cv = void.class;
        Class<int[]> cia = int[].class;

        if (cs != String.class) throw new AssertionError("String.class");
        if (ci != Integer.TYPE) throw new AssertionError("int.class != Integer.TYPE");
        if (ci == Integer.class) throw new AssertionError("int.class == Integer.class");
        if (cv != Void.TYPE) throw new AssertionError("void.class != Void.TYPE");
        if (!"[I".equals(cia.getName())) throw new AssertionError("int[].class name");

        System.out.println("PASS");
    }
}
