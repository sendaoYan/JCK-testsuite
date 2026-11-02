/*
 * @test
 * @summary javax.imageio.spi.IIORegistry conformance: behavioral.
 * @run main IIORegistryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class IIORegistryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.imageio.spi.IIORegistry reg = javax.imageio.spi.IIORegistry.getDefaultInstance();
        check(reg != null, "non-null");
        check(javax.imageio.spi.IIORegistry.getDefaultInstance() == reg, "singleton");
        check(reg instanceof javax.imageio.spi.ServiceRegistry, "instanceof ServiceRegistry");
        check(reg.getCategories() != null, "categories non-null");
        java.util.Iterator<javax.imageio.spi.ImageReaderSpi> readers = reg.getServiceProviders(javax.imageio.spi.ImageReaderSpi.class, true);
        check(readers != null, "readers non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
