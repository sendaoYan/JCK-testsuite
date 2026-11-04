/*
 * @test
 * @summary sun.tools.jconsole.MBeansTab conformance: internal class reflection.
 * @modules jdk.jconsole/sun.tools.jconsole:+open
 * @run main MBeansTabTest
 */
public class MBeansTabTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.tools.jconsole.MBeansTab", false, MBeansTabTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("sun.tools.jconsole.MBeansTab"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.tools.jconsole.Tab"), "superclass");
        {
            java.lang.Class<?>[] arr = c.getInterfaces();
            String[] n = new String[arr.length];
            for (int i = 0; i < arr.length; i++) n[i] = arr[i].getName();
            java.util.Arrays.sort(n);
            check(String.join(";", n).equals("java.beans.PropertyChangeListener;javax.management.NotificationListener;javax.swing.event.TreeSelectionListener;javax.swing.event.TreeWillExpandListener"), "interfaces");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                int mod = m.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(m.getName());
            }
            check(String.join(";", s).equals("dispose;getDataViewer;getMBeanServerConnection;getSheet;getSnapshotMBeanServerConnection;getTabName;getTree;getUpdateInterval;handleNotification;propertyChange;treeWillCollapse;treeWillExpand;update;valueChanged"), "methods");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(f.getName());
            }
            check(String.join(";", s).equals(""), "fields");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
