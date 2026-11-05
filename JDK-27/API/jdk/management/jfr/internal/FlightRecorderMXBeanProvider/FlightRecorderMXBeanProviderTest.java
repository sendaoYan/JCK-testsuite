/*
 * @test
 * @summary jdk.management.jfr.internal.FlightRecorderMXBeanProvider conformance: internal class reflection.
 * @modules jdk.management.jfr/jdk.management.jfr.internal:+open java.management/sun.management.spi
 * @run main FlightRecorderMXBeanProviderTest
 */
public class FlightRecorderMXBeanProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.management.jfr.internal.FlightRecorderMXBeanProvider", false, FlightRecorderMXBeanProviderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.management.jfr.internal.FlightRecorderMXBeanProvider"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.management.spi.PlatformMBeanProvider"), "superclass");
        {
            java.lang.Class<?>[] arr = c.getInterfaces();
            String[] n = new String[arr.length];
            for (int i = 0; i < arr.length; i++) n[i] = arr[i].getName();
            java.util.Arrays.sort(n);
            check(String.join(";", n).equals(""), "interfaces");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                int mod = m.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(m.getName());
            }
            check(String.join(";", s).equals("getPlatformComponentList;setFlightRecorderMXBeanFactory"), "methods");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(f.getName());
            }
            check(String.join(";", s).equals(""), "fields");
        }
        // ---- deepening: provider participates in platform MBean discovery ----
        // instantiable and reports a non-empty platform component list
        Object instance = c.getDeclaredConstructor().newInstance();
        java.lang.reflect.Method list = c.getMethod("getPlatformComponentList");
        Object comps = list.invoke(instance);
        check(comps instanceof java.util.List && !((java.util.List<?>) comps).isEmpty(),
              "getPlatformComponentList returns non-empty list");
        // the FlightRecorder MXBean is reachable through the provider's component
        boolean exposesFlightRecorder = false;
        for (Object comp : (java.util.List<?>) comps) {
            // component classes are package-private: public members need setAccessible (covered by --add-opens)
            java.lang.reflect.Method namesM = comp.getClass().getMethod("mbeanInterfaceNames");
            namesM.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Set<String> names = (java.util.Set<String>) namesM.invoke(comp);
            if (names.contains("jdk.management.jfr.FlightRecorderMXBean")) exposesFlightRecorder = true;
            java.lang.reflect.Method mapM = comp.getClass().getMethod("nameToMBeanMap");
            mapM.setAccessible(true);
            Object mbeans = mapM.invoke(comp);
            check(mbeans instanceof java.util.Map && !((java.util.Map<?, ?>) mbeans).isEmpty(), "component nameToMBeanMap non-empty");
        }
        check(exposesFlightRecorder, "component exposes FlightRecorderMXBean type");
        // ServiceLoader discovers this provider through the platform SPI.
        // sun.management.spi is not exported by java.management, so the SPI class
        // must be located reflectively (same channel the platform itself uses).
        boolean discovered = false;
        Class<?> spi = Class.forName("sun.management.spi.PlatformMBeanProvider");
        Object loader = java.util.ServiceLoader.class.getMethod("load", Class.class).invoke(null, spi);
        java.util.Iterator<?> it = (java.util.Iterator<?>) loader.getClass().getMethod("iterator").invoke(loader);
        while (it.hasNext()) {
            if (it.next().getClass().getName().equals("jdk.management.jfr.internal.FlightRecorderMXBeanProvider")) discovered = true;
        }
        check(discovered, "ServiceLoader(PlatformMBeanProvider) discovers the provider");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
