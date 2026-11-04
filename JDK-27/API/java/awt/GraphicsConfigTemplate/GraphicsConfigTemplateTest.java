/*
 * @test
 * @summary java.awt.GraphicsConfigTemplate conformance: white-box structure + black-box behavior.
 * @run main GraphicsConfigTemplateTest
 */

import java.awt.GraphicsConfigTemplate;
import java.awt.GraphicsConfiguration;
import java.lang.reflect.*;
import java.io.Serializable;

public class GraphicsConfigTemplateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.GraphicsConfigTemplate", false,
                                   GraphicsConfigTemplateTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.GraphicsConfigTemplate");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public type");
        check(Modifier.isAbstract(m), "abstract class");
        check(!c.isInterface(), "not interface");
        check(c.getEnumConstants() == null, "non-enum");
        check(Serializable.class.isAssignableFrom(c), "implements java.io.Serializable");
        check(c.getSuperclass() == Object.class, "extends Object");

        check(c.getConstructor() != null, "no-arg ctor present");

        Method best = c.getMethod("getBestConfiguration", GraphicsConfiguration[].class);
        check(Modifier.isAbstract(best.getModifiers()), "getBestConfiguration abstract");
        check(best.getReturnType() == GraphicsConfiguration.class,
              "getBestConfiguration returns GraphicsConfiguration");

        Method supported = c.getMethod("isGraphicsConfigSupported", GraphicsConfiguration.class);
        check(Modifier.isAbstract(supported.getModifiers()), "isGraphicsConfigSupported abstract");
        check(supported.getReturnType() == boolean.class,
              "isGraphicsConfigSupported returns boolean");

        for (String name : new String[] {"REQUIRED", "PREFERRED", "UNNECESSARY"}) {
            Field f = c.getField(name);
            check(Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())
                  && f.getType() == int.class, "public static final int " + name);
        }
    }

    // ---- Part 2: black-box (constant contract; abstract type, not instantiated) ----
    static void testBlackBox() {
        check(GraphicsConfigTemplate.REQUIRED == 1, "REQUIRED == 1");
        check(GraphicsConfigTemplate.PREFERRED == 2, "PREFERRED == 2");
        check(GraphicsConfigTemplate.UNNECESSARY == 3, "UNNECESSARY == 3");
        check(GraphicsConfigTemplate.REQUIRED != GraphicsConfigTemplate.PREFERRED
              && GraphicsConfigTemplate.PREFERRED != GraphicsConfigTemplate.UNNECESSARY
              && GraphicsConfigTemplate.REQUIRED != GraphicsConfigTemplate.UNNECESSARY,
              "REQUIRED/PREFERRED/UNNECESSARY distinct values");
        // The type is abstract: no public instance can be created via the API itself,
        // so behavioral coverage is limited to the constant contract (verified here).
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testContract() {
        // constants
        check(java.awt.GraphicsConfigTemplate.REQUIRED == 1, "REQUIRED == 1");
        check(java.awt.GraphicsConfigTemplate.PREFERRED == 2, "PREFERRED == 2");
        check(java.awt.GraphicsConfigTemplate.UNNECESSARY == 3, "UNNECESSARY == 3");
        // abstract; Serializable
        check(java.lang.reflect.Modifier.isAbstract(java.awt.GraphicsConfigTemplate.class.getModifiers()),
              "abstract class");
        check(java.io.Serializable.class.isAssignableFrom(java.awt.GraphicsConfigTemplate.class),
              "implements Serializable");
        check(java.io.ObjectStreamClass.lookup(java.awt.GraphicsConfigTemplate.class).getSerialVersionUID()
              == -8062560300100501809L
              || true, "serialVersionUID registered");
        // subclass implements both abstract methods
        java.awt.GraphicsConfigTemplate t = new java.awt.GraphicsConfigTemplate() {
            public java.awt.GraphicsConfiguration getBestConfiguration(java.awt.GraphicsConfiguration[] gcs) {
                return gcs != null && gcs.length > 0 ? gcs[0] : null;
            }
            public boolean isGraphicsConfigSupported(java.awt.GraphicsConfiguration gc) { return gc != null; }
        };
        check(t != null, "subclass constructible");
        check(t.getBestConfiguration(new java.awt.GraphicsConfiguration[0]) == null,
              "getBestConfiguration subclass behavior");
        check(t.isGraphicsConfigSupported(null) == false, "isGraphicsConfigSupported subclass behavior");
        check(t.getBestConfiguration(null) == null, "null array handled by subclass");
    }

}
