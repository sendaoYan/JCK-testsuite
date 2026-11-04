# javax.accessibility.AccessibleBundle 测试设计

## 1. API 功能
强类型枚举基类（AccessibleRole/AccessibleState/AccessibleRelation 的父类），负责把 locale 无关的 key 映射为本地化显示串，并提供回退逻辑。

按方法分组：
- 查询: toDisplayString(), toDisplayString(Locale), toString()
- 受保护: toDisplayString(String name, Locale), protected String key

## 2. Java 用户如何使用
子类在构造时设 key；toDisplayString(Locale) 从 com.sun.accessibility.internal.resources.accessibility 资源包查 key，找不到则回退为 key 本身；toString() 等价 toDisplayString()。测试用子类暴露受保护成员，验证已知 key 本地化与未知 key 回退。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/accessibility/AccessibleBundle.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/java/awt/a11y/

## 4. 测试用例设计 + 自动化实现
- 白盒: 抽象类、extends Object、声明 toDisplayString/toString
- 黑盒: 已知 key(active) 本地化=active；未知 key 回退；toString==toDisplayString；受保护重载回退

自动化实现位于同目录 `AccessibleBundleTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
