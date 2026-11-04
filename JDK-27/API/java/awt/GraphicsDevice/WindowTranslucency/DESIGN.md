# java.awt.GraphicsDevice.WindowTranslucency 黑盒行为测试设计

## 1. API 功能
- `enum WindowTranslucency`：`PERPIXEL_TRANSPARENT`、`TRANSLUCENT`、`PERPIXEL_TRANSLUCENT`。
- 枚举 API：`values()`、`valueOf(String)`、`ordinal()`、`name()`。

## 2. Java 用户如何使用
`GraphicsDevice.isWindowTranslucencySupported(WindowTranslucency.TRANSLUCENT)` 判断窗口透明能力。
```java
if (dev.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT)) { ... }
```

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.desktop/share/classes/java/awt/GraphicsDevice.java`：`WindowTranslucency` 内部枚举，`isWindowTranslucencySupported` 用它表达三类透明能力。
- `test/jdk/java/awt/GraphicsDevice/WindowTranslucency`、`test/jdk/java/awt/Window/` 透明窗口用例遍历该枚举探测能力。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- `values().length==3`、ordinal 顺序 0..2。
- `valueOf(name)`/`name()` 往返、三常量互异。
- `valueOf("BOGUS")` 抛 `IllegalArgumentException`。
纯枚举，headless-safe。
