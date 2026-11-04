# java.awt.Cursor 黑盒行为测试设计

## 1. API 功能
封装鼠标光标的位图表示。`implements Serializable`。
- 常量：`DEFAULT_CURSOR(0)` … `MOVE_CURSOR(13)` 预定义类型，`CUSTOM_CURSOR(-1)`。
- 构造：`Cursor(int type)`（预定义类型 0..13），受保护的 `Cursor(String name)` 供实现用。
- 查询：`getType()`、`getName()`、`toString()`。
- 静态工厂：`getPredefinedCursor(int)`（带缓存）、`getDefaultCursor()`、`getSystemCustomCursor(String)`（headless 抛 HeadlessException）。

## 2. Java 用户如何使用
```java
Component c = ...;
c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
c.setCursor(new Cursor(Cursor.WAIT_CURSOR));
int t = c.getCursor().getType();
String n = c.getCursor().getName();
```
组合链路：`getPredefinedCursor/getDefaultCursor` → `getType` 一致性 → `getName/toString` 描述。

## 3. 开源代码实际用法调研（来源标注）
- JDK 自带测试 `test/jdk/java/awt/dnd/ImageDecoratedDnDInOut/MyCursor.java`：`Cursor.getDefaultCursor()` + 组件 `setCursor` 用法。
- JDK 源码 `src/java.desktop/share/classes/java/awt/Cursor.java`：`getPredefinedCursor` 用 `predefinedPrivate[]` 缓存、`new Cursor(type)` 每次新建实例；非法 type（<0 或 >13）抛 `IllegalArgumentException`；`toString()=getClass().getName()+"["+getName()+"]"`。
- `src/java.desktop/share/classes/java/awt/Component.java`：`setCursor/getCursor` 消费 Cursor 对象。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`，headless-safe，不碰 `Toolkit.createCustomCursor`）：
- 15 个常量精确值（0..13 + CUSTOM_CURSOR=-1）。
- `new Cursor(type)` + `getType()` 往返（DEFAULT/HAND/MOVE/CROSSHAIR）。
- `getName()` 非空、`toString()` 前缀/后缀/包含 name。
- `getPredefinedCursor(0..13)` 全类型 `getType()` 一致 + 同一 type 缓存同实例。
- `getDefaultCursor()` == `getPredefinedCursor(DEFAULT)` 且 `getType()==DEFAULT_CURSOR`；`new Cursor(0)` 与缓存默认是不同实例。
- 非法 type（-1/14/100）在构造与 `getPredefinedCursor` 均抛 `IllegalArgumentException`。
断言 77 项，白盒 28 + 黑盒 49。
