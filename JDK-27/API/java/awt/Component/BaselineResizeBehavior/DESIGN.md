# java.awt.Component.BaselineResizeBehavior 黑盒行为测试设计

## 1. API 功能
- `enum BaselineResizeBehavior`：`CONSTANT_ASCENT`、`CONSTANT_DESCENT`、`CENTER_OFFSET`、`OTHER`。
- 枚举 API：`values()`、`valueOf(String)`、`ordinal()`、`name()`。

## 2. Java 用户如何使用
`Component.getBaselineResizeBehavior()` 返回组件尺寸变化时基线如何移动：
```java
Component.BaselineResizeBehavior b = comp.getBaselineResizeBehavior();
if (b == Component.BaselineResizeBehavior.CONSTANT_ASCENT) { ... }
```

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.desktop/share/classes/java/awt/Component.java`：`BaselineResizeBehavior` 内部枚举。
- `test/jdk/java/awt/font/`、`test/jdk/javax/swing/` 基线相关用例比较该枚举。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- `values().length==4`、ordinal 顺序 0..3。
- `valueOf(name)`/`name()` 往返、四常量互异。
- `valueOf("BOGUS")` 抛 `IllegalArgumentException`。
headless-safe。
