# javax.script.ScriptContext 测试设计

## 1. API 功能
脚本执行上下文契约：作用域 Bindings、属性存取、读写器。

按方法分组：
- 常量: ENGINE_SCOPE=100, GLOBAL_SCOPE=200
- Bindings: set/getBindings(int)
- 属性: set/get/removeAttribute, getAttribute(name), getAttributesScope
- IO: get/setWriter, get/setErrorWriter, get/setReader
- 作用域: getScopes()

## 2. Java 用户如何使用
引擎用 ScriptContext 管理全局/引擎作用域属性。测试用 SimpleScriptContext 实例验证常量、作用域顺序、属性遮蔽与 IO 回读。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.scripting/share/classes/javax/script/ScriptContext.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/javax/script/SimpleScriptContextNameChecksTest.java

## 4. 测试用例设计 + 自动化实现
- 白盒: 接口、set/getBindings/set/getAttribute/removeAttribute
- 黑盒: 常量；作用域顺序；引擎遮蔽全局；全局绑定未设时 no-op；IO 回读；异常

自动化实现位于同目录 `ScriptContextTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。

## 5. 深化增量（契约覆盖）
追加 `testDeepContracts()`：黑盒行为矩阵（构造/读写/视图/null 负例精确异常与消息片段），
JDK-8 与 JDK-27 行为一致时共享同一断言文本。
