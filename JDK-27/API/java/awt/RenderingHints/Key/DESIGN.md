# java.awt.RenderingHints.Key 黑盒行为测试设计

## 1. API 功能
- 抽象类（`java.awt.RenderingHints$Key`），protected 构造 `Key(int privatekey)`。
- 抽象：`isCompatibleValue(Object)`。
- final：`hashCode()`（恒等于 `System.identityHashCode`）、`equals(Object)`（恒等于 `==`）。
- protected final：`intKey()`。

## 2. Java 用户如何使用
通常不直接实现；通过 JDK 提供的 `KEY_*` 常量（如 `KEY_ANTIALIASING`）判断某个 `VALUE_*` 是否属于该键：
```java
if (RenderingHints.KEY_ANTIALIASING.isCompatibleValue(someValue)) { ... }
```
自定义渲染键时需继承 `Key` 并实现 `isCompatibleValue`，注意同一子类同一 `privatekey` 只能实例化一次。

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 `src/java.desktop/share/classes/java/awt/RenderingHints.java`（内部类 `Key`）：`recordIdentity` 用「子类名 + 子类 identityHashCode + privatekey」登记，同一子类同 `privatekey` 重复构造抛 `IllegalArgumentException`；`hashCode` 返回 `super.hashCode()`（身份哈希）；`equals` 返回 `this==o`。
- JDK 自带测试：`test/jdk/java/awt/**` 大量直接使用 `RenderingHints.KEY_*` 常量（如 `FullScreen/AltTabCrashTest`）。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- 自定义 `TestKey` 子类：`intKey` 返回 private key、`isCompatibleValue` 接受 String。
- `equals` 恒 `==`（自反、两实例不等、null/异类不等）；`hashCode` 恒身份哈希。
- 同一子类同 privatekey 二次构造抛 `IllegalArgumentException`。
- 内置 `KEY_ANTIALIASING`/`KEY_RENDERING` 的 `isCompatibleValue` 正/负/`null` 分支。
断言 27 项，身份语义精确断言（headless-safe）。
