# EventID 设计案

## 1. API 功能
`com.sun.java.accessibility.util.EventID` 是一个常量类（工具类），把 AWT / Swing 的各类事件监听器映射为 `int` 常量，供 `AWTEventMonitor` / `SwingEventMonitor` 及其内部监听器按类别安装/卸载监听器时使用。

- 构造器：`public EventID()`（无参，空实现）。
- 常量（`public static final int`）：29 个，覆盖 AWT（ACTION/ADJUSTMENT/COMPONENT/CONTAINER/FOCUS/ITEM/KEY/MOUSE/MOTION/TEXT/WINDOW）、Swing（ANCESTOR/CARET/CELLEDITOR/CHANGE/COLUMNMODEL/DOCUMENT/LISTDATA/LISTSELECTION/MENU/POPUPMENU/TABLEMODEL/TREEEXPANSION/TREEMODEL/TREESELECTION/UNDOABLEEDIT/INTERNALFRAME）以及 Beans（PROPERTYCHANGE/VETOABLECHANGE）。
- 无实例方法；纯常量容器。

## 2. Java 用户如何使用
用户直接以 `EventID.FOCUS` 等方式引用常量，作为「事件类别」标签传给安装/卸载逻辑；也可 `new EventID()`（不常用，仅为完整性存在）。

组合链路示例（来自 JDK 内部 `AWTEventMonitor` 的用法）：
```
AWTEventMonitor.awtListener.installListeners(EventID.COMPONENT);
AWTEventMonitor.awtListener.removeListeners(EventID.KEY);
```

## 3. 开源代码实际用法调研
- JDK 源码内部：`~/git/jdk25u-dev/src/jdk.accessibility/share/classes/com/sun/java/accessibility/util/AWTEventMonitor.java` 大量使用 `EventID.COMPONENT` / `EventID.KEY` / `EventID.MOUSE` / `EventID.MOTION` / `EventID.ACTION` / `EventID.TEXT` 等常量作为 `switch` 分支与 `installListeners/removeListeners` 参数。
- JDK 源码内部：`SwingEventMonitor.java` 使用 `EventID.ANCESTOR` / `EventID.CARET` / `EventID.CELLEDITOR` / `EventID.CHANGE` / `EventID.DOCUMENT` / `EventID.LISTSELECTION` 等常量。
- 行为权威：`~/git/jdk25u-dev/src/jdk.accessibility/share/classes/com/sun/java/accessibility/util/EventID.java` 直接给出 29 个常量的精确取值（ACTION=0 … INTERNALFRAME=29，其中 MOTION=8、TEXT=10，中间 9 号空缺）。
- 注：`~/git/jdk25u-dev/test/jdk` 无针对本类的直接功能用例；结构/行为以 JDK 源码为准，不凭记忆。

## 4. 测试用例设计 + 自动化实现
- 白盒：类非接口、为具体类；12 个 brief 指定字段存在（保留旧断言）。
- 黑盒（`testBlackBox()`）：
  - 反射读取全部 29 个常量，断言 `public static final`、类型为 `int`、非负、值等于源码给定值、且 29 个值互不相同。
  - `new EventID()` 不抛异常且非 null。
