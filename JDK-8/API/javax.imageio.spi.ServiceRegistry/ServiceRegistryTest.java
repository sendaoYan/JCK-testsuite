/*
 * @test
 * @summary javax.imageio.spi.ServiceRegistry conformance: behavioral.
 * @run main ServiceRegistryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ServiceRegistryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        java.util.List<java.lang.Class<?>> cats = new java.util.ArrayList<>();
        cats.add(javax.imageio.spi.ImageReaderSpi.class);
        javax.imageio.spi.ServiceRegistry reg = new javax.imageio.spi.ServiceRegistry(cats.iterator());
        check(reg.getCategories().hasNext(), "categories");
        java.util.Iterator<javax.imageio.spi.ImageReaderSpi> it = reg.getServiceProviders(javax.imageio.spi.ImageReaderSpi.class, true);
        check(it != null && !it.hasNext(), "empty providers");
        check(!reg.contains(new Object()), "contains false");
        reg.deregisterAll();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
