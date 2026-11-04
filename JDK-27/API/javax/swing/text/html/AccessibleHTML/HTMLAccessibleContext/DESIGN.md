# javax.swing.text.html.AccessibleHTML.HTMLAccessibleContext — DESIGN

## 1. API 功能
`public abstract class HTMLAccessibleContext extends AccessibleContext implements Accessible, AccessibleComponent`，是 AccessibleHTML 可访问树中所有元素的公共基类（角色/状态/组件属性/光标/字体/边界/焦点等）。

## 2. Java 用户如何使用
用户通过 `((HTMLEditorKit)editor.getEditorKit()).getAccessibleContext()` 获得根（私有子类 `RootHTMLAccessibleContext`）。注意 `editor.getAccessibleContext()` 返回的是 `JEditorPane$AccessibleJEditorPaneHTML`，并非 HTMLAccessibleContext。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/AccessibleHTML.java`：`class HTMLAccessibleContext`（第 301 行附近）实现 Accessible/AccessibleComponent。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLEditorKit.java`：`getAccessibleContext()` 委托 AccessibleHTML。
- `~/git/jdk25u-dev/test/jdk/javax/swing/JEditorPane/4492274/bug4492274.java`：编辑器可访问性回归。

## 4. 测试用例设计（黑盒部分，`HTMLAccessibleContextTest.testBlackBox`）
1. `kit.getAccessibleContext()` 非 null、instanceof、`getAccessibleContext()==this`。
2. `getAccessibleChildrenCount() >= 1`、根 `getAccessibleIndexInParent()==-1`、状态集含 ENABLED、locale 固定。
3. AccessibleComponent 往返：enabled/background/foreground/font/cursor/location/visibility。
4. `contains` 返回布尔、`requestFocus`、`add/removeFocusListener` 可调用。
