# javax.script.SimpleScriptContext 测试设计

## 1. API 功能
ScriptContext 的最小实现：引擎/全局 Bindings、读写器、属性遮蔽。

按方法分组：
- 构造: ()
- Bindings: get/setBindings(int)
- 属性: set/get/removeAttribute, getAttribute(name), getAttributesScope
- IO: get/setWriter, get/setErrorWriter, get/setReader
- 作用域: getScopes()

## 2. Java 用户如何使用
AbstractScriptEngine 默认用它作上下文。测试覆盖引擎/全局绑定、引擎遮蔽全局、全局未设绑定 no-op、IO 回读与异常。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.scripting/share/classes/javax/script/SimpleScriptContext.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/javax/script/SimpleScriptContextNameChecksTest.java

## 4. 测试用例设计 + 自动化实现
- 白盒: 具体类、implements ScriptContext、get/setBindings/get/setAttribute
- 黑盒: 作用域顺序；引擎遮蔽全局；全局绑定 no-op；IO 回读；setBindings null/非法 scope；name 校验

自动化实现位于同目录 `SimpleScriptContextTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。

## 5. 深化增量（契约覆盖）
追加 `testDeepContracts()`：黑盒行为矩阵（构造/读写/视图/null 负例精确异常与消息片段），
JDK-8 与 JDK-27 行为一致时共享同一断言文本。
