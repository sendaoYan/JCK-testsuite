/*
 * @test
 * @summary javax.swing.SpinnerListModel conformance: white-box structure + black-box behavior.
 * @run main SpinnerListModelTest
 */

import java.lang.reflect.Modifier;
import java.util.Arrays;

import javax.swing.SpinnerListModel;

public class SpinnerListModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.SpinnerListModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.AbstractSpinnerModel"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        SpinnerListModel m = new SpinnerListModel(new Object[] { "a", "b", "c" });
        check("a".equals(m.getValue()), "initial value");
        check("b".equals(m.getNextValue()), "getNextValue");
        check(m.getPreviousValue() == null, "previous at first is null");
        m.setValue("c");
        check("c".equals(m.getValue()), "setValue");
        check(m.getNextValue() == null, "next at last is null");
        check("b".equals(m.getPreviousValue()), "previous at last");
        check(m.getList().size() == 3, "getList");

        m.setList(Arrays.asList("x", "y"));
        check("x".equals(m.getValue()), "setList resets value");
        check(m.getList().size() == 2, "setList size");

        SpinnerListModel fromList = new SpinnerListModel(Arrays.asList("p", "q"));
        check("p".equals(fromList.getValue()), "list ctor value");
        SpinnerListModel empty = new SpinnerListModel();
        check("empty".equals(empty.getValue()), "default list has single placeholder");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
