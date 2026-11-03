# javax.swing.text.StyleContext.NamedStyle — DESIGN

## 1. API 功能
`StyleContext` 的非静态内部类，实现 `Style`（`extends MutableAttributeSet`）。核心方法：
- 构造：`NamedStyle()`、`NamedStyle(Style parent)`、`NamedStyle(String name, Style parent)`。
- 名称：`getName()`、`setName(String)`（null 名不生效）。
- 监听：`addChangeListener/removeChangeListener/getChangeListeners/getListeners(Class)`。
- 属性：完整 MutableAttributeSet 契约（add/remove/getAttribute/…），底层委托给不可变共享集。

## 2. Java 用户如何使用
```java
StyleContext sc = new StyleContext();
StyleContext.NamedStyle ns = sc.new NamedStyle();
ns.setName("s1");
ns.addAttribute("k", "v");
ns.addChangeListener(e -> ...);
```

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/StyleContext.java`：`NamedStyle(String name, Style parent)` 先 `setName(name)` 再 `setResolveParent(parent)`；`setName(null)` 不操作；`addAttribute` 委托 `StyleContext.this.addAttribute` 并 `fireStateChanged()`；`setResolveParent(null)` 调用 `removeAttribute`。

## 4. 测试用例设计 + 自动化实现（`NamedStyleTest.testBlackBox`）
1. 无参构造：getName null、空、无 parent、toString 前缀。
2. setName/getName 往返；setName(null) 不变。
3. 带名+父构造：子 getName、resolve parent、继承属性、isDefined 本地语义。
4. ChangeListener 触发/移除、getChangeListeners 计数。
5. 属性 add/get/remove + copyAttributes 返回等价独立实例。
