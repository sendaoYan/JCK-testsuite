/*
 * @test
 * @summary javax.naming.spi.DirStateFactory.Result conformance: white-box structure + black-box behavior.
 * @run main ResultTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.spi.DirStateFactory;

public class ResultTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.spi.DirStateFactory$Result");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "is class");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(hasMethod(c, "getObject"), "getObject");
        check(hasMethod(c, "getAttributes"), "getAttributes");
        check(c.getConstructor(Object.class, Attributes.class) != null,
                "public constructor (Object,Attributes)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        BasicAttributes attrs = new BasicAttributes("cn", "value");
        DirStateFactory.Result r = new DirStateFactory.Result("obj", attrs);
        check("obj".equals(r.getObject()), "getObject returns constructor arg");
        check(r.getAttributes() == attrs, "getAttributes identity");

        // Object may be any type (e.g. a number), preserved by identity.
        Integer marker = 42;
        DirStateFactory.Result r2 = new DirStateFactory.Result(marker, attrs);
        check(r2.getObject() == marker, "object identity preserved");
        check(r2.getAttributes() == attrs, "attributes identity preserved");

        // Null object/attributes are permitted and returned as-is.
        DirStateFactory.Result r3 = new DirStateFactory.Result(null, null);
        check(r3.getObject() == null, "null object");
        check(r3.getAttributes() == null, "null attributes");

        // Independent instances do not share state.
        BasicAttributes other = new BasicAttributes(true);
        DirStateFactory.Result r4 = new DirStateFactory.Result("a", other);
        check(r4.getAttributes() == other && r.getAttributes() == attrs,
                "independent instances keep own attributes");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
