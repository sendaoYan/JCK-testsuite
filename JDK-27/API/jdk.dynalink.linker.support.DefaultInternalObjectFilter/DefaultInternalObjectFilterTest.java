/*
 * @test
 * @summary jdk.dynalink.linker.support.DefaultInternalObjectFilter conformance: filter transform.
 * @run main DefaultInternalObjectFilterTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.util.*;

public class DefaultInternalObjectFilterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        MethodHandle id = MethodHandles.identity(Object.class);
        DefaultInternalObjectFilter f = new DefaultInternalObjectFilter(id, id);
        check(f.transform(id) != null, "transform");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
