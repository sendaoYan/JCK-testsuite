/*
 * @test
 * @summary jdk.dynalink.DynamicLinkerFactory conformance: configure factory and create linker.
 * @run main DynamicLinkerFactoryTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.util.*;

public class DynamicLinkerFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        DynamicLinkerFactory f = new DynamicLinkerFactory();
        f.setSyncOnRelink(true);
        f.setUnstableRelinkThreshold(4);
        f.setPrioritizedLinkers(new BeansLinker());
        check(f.createLinker() != null, "createLinker");
        check(f.getAutoLoadingErrors() != null, "auto loading errors");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
