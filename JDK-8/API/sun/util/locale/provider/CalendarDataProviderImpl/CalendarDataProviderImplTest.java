/*
 * @test
 * @summary sun.util.locale.provider.CalendarDataProviderImpl conformance: class and members.
 * @run main CalendarDataProviderImplTest
 */

public class CalendarDataProviderImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.provider.CalendarDataProviderImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.locale.provider.CalendarDataProviderImpl"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.spi.CalendarDataProvider"), "superclass java.util.spi.CalendarDataProvider");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.util.locale.provider.AvailableLanguageTags"), "interface sun.util.locale.provider.AvailableLanguageTags");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getAvailableLanguageTags"), "declares method getAvailableLanguageTags");
        check(mnames.contains("getAvailableLocales"), "declares method getAvailableLocales");
        check(mnames.contains("getFirstDayOfWeek"), "declares method getFirstDayOfWeek");
        check(mnames.contains("getMinimalDaysInFirstWeek"), "declares method getMinimalDaysInFirstWeek");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
