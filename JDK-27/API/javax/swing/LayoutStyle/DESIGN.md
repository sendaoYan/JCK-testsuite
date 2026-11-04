# javax.swing.LayoutStyle 设计案

## 1. API 功能
`LayoutStyle` 抽象类提供组件间距约定。方法分组：
- 全局实例：`setInstance/getInstance`。
- 间距（抽象）：`getPreferredGap`、`getContainerGap`。

## 2. Java 用户如何使用
LookAndFeel 提供 LayoutStyle，用户读取间距：
```java
LayoutStyle style = LayoutStyle.getInstance();
int gap = style.getPreferredGap(a, b, LayoutStyle.ComponentPlacement.RELATED, SwingConstants.WEST, parent);
```
典型链路：`LayoutStyle.getInstance()` → `getPreferredGap`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/LookAndFeel.java` 的 `getLayoutStyle` 返回 `LayoutStyle`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/LookAndFeel.java`）。
- JDK 源码 `javax/swing/plaf/basic/BasicLookAndFeel.java` 有默认实现。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 abstract。
- 行为（黑盒）：`testBlackBox()` 覆盖 `getInstance` 单例、匿名实现 `getPreferredGap/getContainerGap`。
