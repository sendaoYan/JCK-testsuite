# java.awt.BufferCapabilities.FlipContents 黑盒行为测试设计

## 1. API 功能
- 静态常量（非 Enum，继承 `java.awt.AttributeValue`）：`UNDEFINED`、`BACKGROUND`、`PRIOR`、`COPIED`。
- `toString()`、`hashCode()`（AttributeValue 语义）。

## 2. Java 用户如何使用
`BufferCapabilities.getFlipContents()` 返回翻页后后台缓冲内容：
```java
BufferCapabilities.FlipContents fc = bc.getFlipContents();
if (fc == BufferCapabilities.FlipContents.BACKGROUND) { ... }
```

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.desktop/share/classes/java/awt/BufferCapabilities.java`：FlipContents 继承 `AttributeValue`，NAME 数组 `undefined/background/prior/copied`，int 值 0..3。
- `src/java.desktop/share/classes/java/awt/AttributeValue.java`：`hashCode()` 返回 int 值、`toString()` 返回名字。
- `test/jdk/java/awt/image/VolatileImage/` 等比较 FlipContents 常量。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- 四常量非空、互异。
- `hashCode` 0..3、`toString` 名字精确。
- 单例身份与确定性。
headless-safe。
