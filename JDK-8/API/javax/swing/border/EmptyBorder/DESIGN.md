# javax.swing.border.EmptyBorder 设计案

## 1. API 功能
`EmptyBorder` 是透明、只占位不绘制的空边框，继承 `AbstractBorder` 并实现 `Serializable`。方法分组：

- 构造：`EmptyBorder(int top, int left, int bottom, int right)`；`EmptyBorder(Insets borderInsets)`（null 抛 NPE）。
- 绘制：`paintBorder(...)`（空实现，不绘制）。
- 边距：`getBorderInsets(Component, Insets)`（用保存的 top/left/bottom/right 重初始化并返回同一对象）；`getBorderInsets()`（返回新的 `Insets`）。
- 查询：`isBorderOpaque()`（返回 `false`）。

## 2. Java 用户如何使用
用户用 `EmptyBorder` 给组件加空白留白，或作为 `CompoundBorder` 的 margin：

```java
EmptyBorder margin = new EmptyBorder(10, 10, 10, 10);   // 四周各 10
Insets in = margin.getBorderInsets(null);                // (10,10,10,10)
Insets reuse = new Insets(0, 0, 0, 0);
margin.getBorderInsets(null, reuse);                     // reuse 被覆写为 (10,10,10,10)
```

典型组合链路：`new EmptyBorder(top, left, bottom, right)` → `getBorderInsets()` 校验边距 → 作为 `new CompoundBorder(decor, margin)` 的内边框形成装饰+留白组合。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/BorderFactory.java`：`public static Border createEmptyBorder(int top, int left, int bottom, int right) { return new EmptyBorder(top, left, bottom, right); }`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/BorderFactory.java`）。
- JDK 源码 `javax/swing/plaf/basic/BasicBorders.java`：`static class RolloverMarginBorder extends EmptyBorder { ... }`（来源：同目录 `BasicBorders.java`）。
- JDK 测试 `test/jdk/java/beans/XMLEncoder/javax_swing_border_EmptyBorder.java`：`return new EmptyBorder(1, 2, 3, 4);`（来源：`~/git/jdk25u-dev/test/jdk/java/beans/XMLEncoder/`）。
- JDK 源码 `javax/swing/border/CompoundBorder.java` 文档示例：`Border margin = new EmptyBorder(10,10,10,10); comp.setBorder(new CompoundBorder(border, margin));`。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `AbstractBorder`、实现 `Serializable`、非 abstract。
- 行为（黑盒）：`testBlackBox()` 覆盖——int 构造与 `Insets` 构造的边距 round-trip；`getBorderInsets(c, insets)` 复用并覆写同一对象；`getBorderInsets()` 每次返回新对象；`null` Insets 构造与 `getBorderInsets(c, null)` 抛 NPE；`isBorderOpaque()==false`；`paintBorder` 空实现 smoke。

## 5. 深化追加（第二轮）
- 序列化 round-trip：`new EmptyBorder(4,5,6,7)` 反序列化后 insets 逐边保持、仍非 opaque、为新实例。
- BorderFactory 数据属性：`createEmptyBorder(2,3,4,5)` 类型/insets/非 opaque；`createEmptyBorder()` 零 insets。（注意 `createEmptyBorder(Insets)` 重载不存在，Insets 版本只有 `new EmptyBorder(Insets)` 构造器。）
