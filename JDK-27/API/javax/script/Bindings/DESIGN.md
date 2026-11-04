# javax.script.Bindings 测试设计

## 1. API 功能
键为 String 的 Map 接口，脚本属性存储契约。

按方法分组：
- 接口: put(String,Object), putAll, containsKey, get, remove
- 继承: java.util.Map 其余方法

## 2. Java 用户如何使用
脚本引擎用 Bindings 存变量。测试用 HashMap 支撑的最小实现验证 Map 契约（put/get/remove/putAll/clear）。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.scripting/share/classes/javax/script/Bindings.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/javax/script/Test1.java

## 4. 测试用例设计 + 自动化实现
- 白盒: 接口、继承 java.util.Map、put/putAll/containsKey/get/remove
- 黑盒: 最小实现 put/get/remove/putAll/clear

自动化实现位于同目录 `BindingsTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
