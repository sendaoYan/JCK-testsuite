# javax.script.SimpleBindings 测试设计

## 1. API 功能
基于 HashMap 的 Bindings 实现，键非空字符串校验。

按方法分组：
- 构造: (), (Map)
- 操作: put, putAll, clear, remove
- 查询: get, containsKey, containsValue, size, isEmpty, keySet, values, entrySet

## 2. Java 用户如何使用
脚本引擎默认用 SimpleBindings 存变量。测试覆盖 Map 操作与键校验异常（null/空/非 String）。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.scripting/share/classes/javax/script/SimpleBindings.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/javax/script/Test1.java

## 4. 测试用例设计 + 自动化实现
- 白盒: 具体类、implements Bindings、put/get/containsKey/remove
- 黑盒: 增删查回读；put 返回旧值；键校验 NPE/IAE/CCE；clear；构造 null map NPE

自动化实现位于同目录 `SimpleBindingsTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
