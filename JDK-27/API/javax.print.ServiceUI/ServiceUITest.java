/*
 * @test
 * @summary javax.print.ServiceUI conformance: service UI.
 * @run main ServiceUITest
 */

import java.lang.reflect.*;
import javax.print.ServiceUI;

public class ServiceUITest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasIf(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    public static void main(String[] a) throws Exception {
        ServiceUI ui = new ServiceUI();
        check(ui != null, "ctor");
        Class<?> c = Class.forName("javax.print.ServiceUI");
        check(hasMethod(c, "printDialog"), "declares printDialog");
        check(java.lang.reflect.Modifier.isStatic(c.getDeclaredMethod("printDialog", java.awt.GraphicsConfiguration.class, int.class, int.class, javax.print.PrintService[].class, javax.print.PrintService.class, javax.print.DocFlavor.class, javax.print.attribute.PrintRequestAttributeSet.class).getModifiers()), "printDialog static");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
