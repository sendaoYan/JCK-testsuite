# javax.script.Invocable 测试设计

## 1. API 功能
允许从脚本引擎调用脚本函数/对象方法/获取接口代理。

按方法分组：
- 调用: invokeMethod(Object,String,Object...), invokeFunction(String,Object...)
- 接口: getInterface(Class), getInterface(Object,Class)

## 2. Java 用户如何使用
Java 侧通过 Invocable 调用脚本函数。测试用匿名实现返回固定结果与固定接口对象。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.scripting/share/classes/javax/script/Invocable.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/javax/script/GetInterfaceTest.java

## 4. 测试用例设计 + 自动化实现
- 白盒: 接口、三个方法
- 黑盒: invokeMethod/invokeFunction/getInterface 两重载

自动化实现位于同目录 `InvocableTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
