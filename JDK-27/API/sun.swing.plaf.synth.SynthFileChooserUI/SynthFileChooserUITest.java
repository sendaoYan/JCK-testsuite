/*
 * @test
 * @summary sun.swing.plaf.synth.SynthFileChooserUI conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.swing.plaf.synth:+open
 * @run main SynthFileChooserUITest
 */

import java.util.*;
import java.lang.reflect.*;

public class SynthFileChooserUITest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.swing.plaf.synth.SynthFileChooserUI", false, SynthFileChooserUITest.class.getClassLoader());
        check(c != null, "load");
        check("sun.swing.plaf.synth.SynthFileChooserUI".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("javax.swing.plaf.basic.BasicFileChooserUI".equals(c.getSuperclass().getName()), "superclass");
        check("javax.swing.plaf.synth.SynthUI".equals(interfaces(c)), "interfaces");
        check("private static#boolean#isGlobPattern(java.lang.String);private#int#getComponentState(javax.swing.JComponent);private#java.lang.String#getCommonStartString([Ljava.io.File;);private#javax.swing.plaf.synth.Region#getRegion(javax.swing.JComponent);private#void#resetGlobFilter();private#void#updateFileNameCompletion();private#void#updateStyle(javax.swing.JComponent);protected abstract#javax.swing.ActionMap#createActionMap();protected#javax.swing.JButton#getApproveButton(javax.swing.JFileChooser);protected#javax.swing.JButton#getCancelButton(javax.swing.JFileChooser);protected#javax.swing.plaf.synth.SynthContext#getContext(javax.swing.JComponent,int);protected#void#doAccessoryChanged(java.beans.PropertyChangeEvent);protected#void#doAncestorChanged(java.beans.PropertyChangeEvent);protected#void#doControlButtonsChanged(java.beans.PropertyChangeEvent);protected#void#doDirectoryChanged(java.beans.PropertyChangeEvent);protected#void#doFileSelectionModeChanged(java.beans.PropertyChangeEvent);protected#void#doMultiSelectionChanged(java.beans.PropertyChangeEvent);protected#void#doSelectedFileChanged(java.beans.PropertyChangeEvent);protected#void#doSelectedFilesChanged(java.beans.PropertyChangeEvent);protected#void#installDefaults(javax.swing.JFileChooser);protected#void#installIcons(javax.swing.JFileChooser);protected#void#installListeners(javax.swing.JFileChooser);protected#void#paint(javax.swing.plaf.synth.SynthContext,java.awt.Graphics);protected#void#uninstallDefaults(javax.swing.JFileChooser);public abstract#java.lang.String#getFileName();public abstract#void#setFileName(java.lang.String);public static#javax.swing.plaf.ComponentUI#createUI(javax.swing.JComponent);public#java.beans.PropertyChangeListener#createPropertyChangeListener(javax.swing.JFileChooser);public#javax.swing.Action#getFileNameCompletionAction();public#javax.swing.plaf.synth.SynthContext#getContext(javax.swing.JComponent);public#void#clearIconCache();public#void#installComponents(javax.swing.JFileChooser);public#void#installUI(javax.swing.JComponent);public#void#paint(java.awt.Graphics,javax.swing.JComponent);public#void#paintBorder(javax.swing.plaf.synth.SynthContext,java.awt.Graphics,int,int,int,int);public#void#uninstallComponents(javax.swing.JFileChooser);public#void#update(java.awt.Graphics,javax.swing.JComponent)".equals(methods(c)), "methods");
        check("private#java.lang.String#fileNameCompletionString;private#javax.swing.Action#fileNameCompletionAction;private#javax.swing.JButton#approveButton;private#javax.swing.JButton#cancelButton;private#javax.swing.filechooser.FileFilter#actualFileFilter;private#javax.swing.plaf.synth.SynthStyle#style;private#sun.swing.plaf.synth.SynthFileChooserUI$GlobFilter#globFilter".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
        check(c.getDeclaredConstructors().length == 1, "ctors");
        System.out.println("PASS (" + passed + " assertions)");
    }

    static String interfaces(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Class<?> i : c.getInterfaces()) l.add(i.getName());
        Collections.sort(l);
        return String.join(";", l);
    }

    static String methods(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.isSynthetic()) continue;
            StringBuilder s = new StringBuilder();
            s.append(java.lang.reflect.Modifier.toString(m.getModifiers())).append('#');
            s.append(m.getReturnType().getName()).append('#');
            s.append(m.getName()).append('(');
            Class<?>[] ps = m.getParameterTypes();
            for (int i = 0; i < ps.length; i++) { if (i > 0) s.append(','); s.append(ps[i].getName()); }
            s.append(')');
            l.add(s.toString());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String fields(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isSynthetic()) continue;
            l.add(java.lang.reflect.Modifier.toString(f.getModifiers()) + '#' + f.getType().getName() + '#' + f.getName());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String enumConstants(Class<?> c) {
        if (!c.isEnum()) return "";
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) l.add(f.getName());
        }
        Collections.sort(l);
        return String.join(";", l);
    }
}
