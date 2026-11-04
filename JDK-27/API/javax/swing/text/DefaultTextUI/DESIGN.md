# javax.swing.text.DefaultTextUI — DESIGN

## 1. API 功能
已废弃的文本 UI 基类（`@Deprecated`，`abstract class extends javax.swing.plaf.basic.BasicTextUI`）。仅一个受保护构造器 `DefaultTextUI()`，无新增公开方法；行为全部继承自 `BasicTextUI`（`installUI`/`uninstallUI`/`getEditorKit` 等）。

## 2. Java 用户如何使用
已不推荐使用，`BasicTextUI` 取代之。子类需实现 `BasicTextUI.getPropertyPrefix()` 才能实例化：`class MyUI extends DefaultTextUI { protected String getPropertyPrefix(){ return "X"; } }`；随后 `installUI(comp)`/`uninstallUI(comp)` 安装卸载 UI，`getEditorKit(comp)` 返回 `DefaultEditorKit` 实例。

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/DefaultTextUI.java`：`@Deprecated public abstract class DefaultTextUI extends BasicTextUI { protected DefaultTextUI() {} }` —— 仅有空构造器，全部逻辑继承 BasicTextUI。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicTextUI.java`：`private static final EditorKit defaultKit = new DefaultEditorKit(); ... public EditorKit getEditorKit(JTextComponent tc) { return defaultKit; }` —— getEditorKit 返回共享 DefaultEditorKit。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicTextUI.java`：`public void installUI(JComponent c) { if (c instanceof JTextComponent) { ... } else { throw new Error("TextUI needs JTextComponent"); } }` —— installUI 要求 JTextComponent。

## 4. 测试用例设计（黑盒部分，`DefaultTextUITest.testBlackBox`）
1. 具体子类实例 instanceof BasicTextUI 与 TextUI。
2. getEditorKit(area) 返回非 null 的 DefaultEditorKit；getEditorKit(null) 也非 null。
3. installUI(area) / uninstallUI(area) 全程不抛异常。
