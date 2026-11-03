# Translator 设计案

## 1. API 功能
`com.sun.java.accessibility.util.Translator` 为不实现 `Accessible` 的对象提供到 `Accessible` 的适配：继承 `javax.accessibility.AccessibleContext`，实现 `Accessible` 与 `AccessibleComponent`。

- 静态工厂：`public static Accessible getAccessible(Object o)`——`o == null` 返回 null；`o` 已是 `Accessible` 时原样返回；否则尝试按类名找内部 `*Translator` 子类，找不到则 `new Translator(o)`。
- 构造器：`Translator()`、`Translator(Object o)`。
- 源对象：`getSource()` / `setSource(Object)`。
- 对象契约：`equals(Object)`、`hashCode()`。
- `AccessibleContext` 方法：`getAccessibleContext()`（返回 this）、`get/setAccessibleName`、`get/setAccessibleDescription`、`getAccessibleRole`、`getAccessibleStateSet`、`getAccessibleParent`、`getAccessibleIndexInParent`、`getAccessibleChildrenCount`、`getAccessibleChild`、`getLocale`、`add/removePropertyChangeListener`。
- `AccessibleComponent` 方法：`get/setBackground/Foreground/Cursor/Font`、`getFontMetrics`、`isEnabled/setEnabled`、`isVisible/setVisible`、`isShowing`、`contains`、`getLocationOnScreen/getLocation/setLocation`、`getBounds/setBounds`、`getSize/setSize`、`getAccessibleAt`、`isFocusTraversable`、`requestFocus`、`add/removeFocusListener`。

## 2. Java 用户如何使用
辅助技术用 `Translator.getAccessible(obj)` 把任意对象适配成 `Accessible`，再调用其 `getAccessibleContext().getAccessibleName()` 等读取可访问信息；对已是 `Accessible` 的对象则直接使用原对象。

组合链路：
```
Accessible a = Translator.getAccessible(someObj);
String name = a.getAccessibleContext().getAccessibleName();
AccessibleRole role = a.getAccessibleContext().getAccessibleRole();
AccessibleStateSet states = a.getAccessibleContext().getAccessibleStateSet();
```

## 3. 开源代码实际用法调研
- Windows 辅助功能桥 `~/git/jdk25u-dev/src/jdk.accessibility/windows/classes/com/sun/java/accessibility/internal/AccessBridge.java` 多处使用 `Translator.getAccessible(c)` / `Translator.getAccessible(e.getSource())`（第 653、5288、5423、5440、5523 等行）。
- JDK 源码内部 `EventQueueMonitor.getAccessibleAt` 末尾使用 `return Translator.getAccessible(c)` 适配非 `Accessible` 组件。
- JDK 源码内部 `Translator.java`：`getTranslatorClass` 按简单类名（Button/Checkbox/Label/List/TextComponent）查找 `internal` 包下的 `*Translator` 子类；非 Component/MenuItem 源时各 getter 返回默认值（name/description/role 等）。
- 行为权威：`~/git/jdk25u-dev/src/jdk.accessibility/share/classes/com/sun/java/accessibility/util/Translator.java`。

## 4. 测试用例设计 + 自动化实现
- 白盒：类非接口、为具体类、继承 `AccessibleContext`、实现 `Accessible` 与 `AccessibleComponent`；关键方法存在。
- 黑盒（`testBlackBox()`）：headless 下（源用普通 `Object`/`String`，绝不实例化 AWT `Component`，因为 `new Label()` 会抛 `HeadlessException`）：
  - `getAccessible(null)==null`；`getAccessible(普通 Object)` 返回包装 `Translator` 且 `getSource()` 保留原对象；`getAccessible(已 Accessible)` 返回同一实例；`getAccessible(String)` 包装成功且 `getAccessibleName()==null`。
  - 构造器默认源 null、`setSource` 后 `getSource` 返回；`new Translator(o)` 保留源。
  - `getAccessibleContext()==this`。
  - 默认元数据：name/description/locale/parent 为 null、role 为 `UNKNOWN`、state 含 `ENABLED` 不含 `FOCUSABLE`、index -1、childrenCount 0、child(0) null。
  - `AccessibleComponent` 默认值：`isEnabled` true、`isVisible/isShowing/contains/isFocusTraversable` false、`getLocationOnScreen/getLocation/getBounds/getSize/getBackground/getForeground/getCursor/getFont/getFontMetrics/getAccessibleAt` 均 null。
  - 各 setter 与 `requestFocus`/`add/removeFocusListener` 对普通对象为 no-op 且不抛。
  - 对象契约：null 源 `hashCode()==0`；`equals(非 Translator)` false。
