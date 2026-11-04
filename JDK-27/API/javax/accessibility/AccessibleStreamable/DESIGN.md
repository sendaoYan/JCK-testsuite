# javax.accessibility.AccessibleStreamable 测试设计

## 1. API 功能
允许以流的方式读取对象内容：MIME 类型与对应 InputStream。

按方法分组：
- 查询: getMimeTypes() -> DataFlavor[], getStream(DataFlavor) -> InputStream

## 2. Java 用户如何使用
支持拖放/复制的组件暴露可读流。测试用匿名实现返回固定 DataFlavor 数组与流，断言身份与空实现。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/accessibility/AccessibleStreamable.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/java/awt/a11y/

## 4. 测试用例设计 + 自动化实现
- 白盒: 接口、两个方法
- 黑盒: mimeTypes 长度/身份；getStream 返回流；空实现

自动化实现位于同目录 `AccessibleStreamableTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
