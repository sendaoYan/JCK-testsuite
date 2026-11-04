/*
 * @test
 * @summary sun.swing.plaf.synth.SynthFileChooserUIImpl conformance: reflection (no init, native/display-heavy).
 * @run main SynthFileChooserUIImplTest
 */

import java.util.*;
import java.lang.reflect.*;

public class SynthFileChooserUIImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.swing.plaf.synth.SynthFileChooserUIImpl", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.swing.plaf.synth.SynthFileChooserUIImpl".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.swing.plaf.synth.SynthFileChooserUI".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static#void#groupLabels([Lsun.swing.plaf.synth.SynthFileChooserUIImpl$AlignedLabel;);private#int#getMnemonic(java.lang.String,java.util.Locale);private#java.lang.String#fileNameString([Ljava.io.File;);private#java.lang.String#fileNameString(java.io.File);private#void#populateFileNameLabel();protected#javax.swing.Action#getDirectoryComboBoxAction();protected#javax.swing.ActionMap#createActionMap();protected#javax.swing.JComponent#getDirectoryComboBox();protected#sun.swing.plaf.synth.SynthFileChooserUIImpl$DirectoryComboBoxModel#createDirectoryComboBoxModel(javax.swing.JFileChooser);protected#sun.swing.plaf.synth.SynthFileChooserUIImpl$DirectoryComboBoxRenderer#createDirectoryComboBoxRenderer(javax.swing.JFileChooser);protected#sun.swing.plaf.synth.SynthFileChooserUIImpl$FilterComboBoxModel#createFilterComboBoxModel();protected#sun.swing.plaf.synth.SynthFileChooserUIImpl$FilterComboBoxRenderer#createFilterComboBoxRenderer();protected#void#addControlButtons();protected#void#doAccessoryChanged(java.beans.PropertyChangeEvent);protected#void#doControlButtonsChanged(java.beans.PropertyChangeEvent);protected#void#doDirectoryChanged(java.beans.PropertyChangeEvent);protected#void#doFileSelectionModeChanged(java.beans.PropertyChangeEvent);protected#void#doSelectedFileChanged(java.beans.PropertyChangeEvent);protected#void#doSelectedFilesChanged(java.beans.PropertyChangeEvent);protected#void#installDefaults(javax.swing.JFileChooser);protected#void#installListeners(javax.swing.JFileChooser);protected#void#installStrings(javax.swing.JFileChooser);protected#void#removeControlButtons();protected#void#uninstallListeners(javax.swing.JFileChooser);public#java.lang.String#getFileName();public#void#installComponents(javax.swing.JFileChooser);public#void#rescanCurrentDirectory(javax.swing.JFileChooser);public#void#setFileName(java.lang.String);public#void#uninstallUI(javax.swing.JComponent)".equals(methods(c)), "methods");
        check("private final#java.beans.PropertyChangeListener#modeListener;private static final#java.awt.Dimension#hstrut5;private static final#java.awt.Insets#shrinkwrap;private static#java.awt.Dimension#LIST_PREF_SIZE;private#boolean#readOnly;private#int#fileNameLabelMnemonic;private#int#filesOfTypeLabelMnemonic;private#int#folderNameLabelMnemonic;private#int#lookInLabelMnemonic;private#java.lang.String#detailsViewButtonAccessibleName;private#java.lang.String#detailsViewButtonToolTipText;private#java.lang.String#fileNameLabelText;private#java.lang.String#filesOfTypeLabelText;private#java.lang.String#folderNameLabelText;private#java.lang.String#homeFolderAccessibleName;private#java.lang.String#homeFolderToolTipText;private#java.lang.String#listViewButtonAccessibleName;private#java.lang.String#listViewButtonToolTipText;private#java.lang.String#lookInLabelText;private#java.lang.String#newFolderAccessibleName;private#java.lang.String#newFolderToolTipText;private#java.lang.String#saveInLabelText;private#java.lang.String#upFolderAccessibleName;private#java.lang.String#upFolderToolTipText;private#javax.swing.Action#directoryComboBoxAction;private#javax.swing.JComboBox#directoryComboBox;private#javax.swing.JComboBox#filterComboBox;private#javax.swing.JLabel#lookInLabel;private#javax.swing.JPanel#bottomPanel;private#javax.swing.JPanel#buttonPanel;private#javax.swing.JTextField#fileNameTextField;private#javax.swing.JToggleButton#detailsViewButton;private#javax.swing.JToggleButton#listViewButton;private#sun.swing.FilePane#filePane;private#sun.swing.plaf.synth.SynthFileChooserUIImpl$AlignedLabel#fileNameLabel;private#sun.swing.plaf.synth.SynthFileChooserUIImpl$DirectoryComboBoxModel#directoryComboBoxModel;private#sun.swing.plaf.synth.SynthFileChooserUIImpl$FilterComboBoxModel#filterComboBoxModel;static final#int#space".equals(fields(c)), "fields");
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
            s.append(Modifier.toString(m.getModifiers())).append('#');
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
            l.add(Modifier.toString(f.getModifiers()) + '#' + f.getType().getName() + '#' + f.getName());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String enumConstants(Class<?> c) {
        try {
            Object[] ec = c.getEnumConstants();
            if (ec == null) return "";
            List<String> l = new ArrayList<>();
            for (Object o : ec) l.add(((Enum<?>) o).name());
            Collections.sort(l);
            return String.join(";", l);
        } catch (Throwable t) {
            return "";
        }
    }
}
