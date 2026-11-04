# javax.swing.UIManager 设计案

## 1. API 功能
`UIManager` 管理当前外观与全局默认值。方法分组：
- 外观：`getLookAndFeel/getInstalledLookAndFeels/getSystemLookAndFeelClassName/getCrossPlatformLookAndFeelClassName`。
- 默认值：`getDefaults/getLookAndFeelDefaults`。
- 读写：`put/get/getInt` 等类型化 getter。

## 2. Java 用户如何使用
用户查询/设置外观和默认值：
```java
UIManager.setLookAndFeel(systemLaf);
Color c = UIManager.getColor("control");
```
典型链路：`getDefaults/getLookAndFeel` → `getInstalledLookAndFeels` → `put/get`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/UIManager.java` 是 JDK 自身实现（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/UIManager.java`）。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 `Serializable`。
- 行为（黑盒）：`testBlackBox()` 覆盖默认值/外观/安装列表/系统与跨平台类名非空，以及 `put/get/getInt` 读写（headless 下 `getLookAndFeel` 可用）。
