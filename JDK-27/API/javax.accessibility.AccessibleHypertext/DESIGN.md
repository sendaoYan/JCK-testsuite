# javax.accessibility.AccessibleHypertext 测试设计

## 1. API 功能
扩展 AccessibleText，暴露超文本中的链接集合。

按方法分组：
- 查询: getLinkCount(), getLink(int), getLinkIndex(int)
- 继承: AccessibleText 全部方法

## 2. Java 用户如何使用
辅助技术遍历链接并定位字符下标所属链接。测试用含单个链接的模型断言链接身份与 getLinkIndex 边界。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/accessibility/AccessibleHypertext.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/java/awt/a11y/

## 4. 测试用例设计 + 自动化实现
- 白盒: 接口、继承 AccessibleText、三个方法
- 黑盒: link 数量/身份/getLinkIndex 内外部；链接起止下标

自动化实现位于同目录 `AccessibleHypertextTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
