# javax.swing.LookAndFeel 设计案

## 1. API 功能
`LookAndFeel` 是外观抽象基类。方法分组：
- 标识（抽象）：`getName/getID/getDescription/isNativeLookAndFeel/isSupportedLookAndFeel`。
- 静态工具：`makeKeyBindings/makeInputMap/makeComponentInputMap/installProperty/getDesktopPropertyValue`。

## 2. Java 用户如何使用
LookAndFeel 实现类定义外观，用户读取元信息：
```java
LookAndFeel laf = UIManager.getLookAndFeel();
String id = laf.getID();
InputMap map = LookAndFeel.makeInputMap(new Object[] { "ENTER", "commit" });
```
典型链路：`getName/getID/isSupportedLookAndFeel` + 静态 `makeInputMap/makeKeyBindings`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/plaf/basic/BasicLookAndFeel.java` 继承 `LookAndFeel` 并实现 `makeKeyBindings/makeInputMap`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicLookAndFeel.java`）。
- `UIManager` 使用 `LookAndFeel` 管理当前外观。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 abstract。
- 行为（黑盒）：`testBlackBox()` 匿名实现覆盖 5 个抽象标识方法，并测试静态 `makeInputMap/makeKeyBindings` 生成非空映射与绑定。
