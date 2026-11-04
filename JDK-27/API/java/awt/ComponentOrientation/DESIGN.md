# java.awt.ComponentOrientation 黑盒行为测试设计

## 1. API 功能
- 常量：`LEFT_TO_RIGHT`、`RIGHT_TO_LEFT`、`UNKNOWN`。
- 查询：`isHorizontal()`、`isLeftToRight()`。
- 工厂：`getOrientation(Locale)`（按语言标签映射）、`getOrientation(ResourceBundle)`（已弃用）。

## 2. Java 用户如何使用
按区域设置组件排版方向：
```java
ComponentOrientation o = ComponentOrientation.getOrientation(locale);
if (o == ComponentOrientation.RIGHT_TO_LEFT) { mirrorLayout(); }
```

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.desktop/share/classes/java/awt/ComponentOrientation.java`：`isHorizontal` 看 HORIZ_BIT、`isLeftToRight` 看 LTR_BIT；`getOrientation` 对 `ar/fa/he/iw/ji/ur/yi` 返回 RTL，其余 LTR。
- `src/java.desktop/share/classes/java/awt/Component.java` 的 `applyComponentOrientation`/`setComponentOrientation` 使用它。
- `test/jdk/java/awt/ComponentOrientation/`、`test/jdk/java/awt/Container/` 断言 RTL 排版行为。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- 三常量 `isHorizontal/isLeftToRight` 组合。
- `getOrientation(Locale)`：`en`→LTR、`ar/he`→RTL、`zh`→LTR。
headless-safe。
