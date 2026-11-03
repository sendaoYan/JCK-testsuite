# javax.script.AbstractScriptEngine 测试设计

## 1. API 功能
ScriptEngine 的标准骨架实现：默认 SimpleScriptContext、put/get、setContext/getContext、eval 各变体委托。

按方法分组：
- 构造: (), (Bindings)
- 上下文: set/getContext, get/setBindings, getScriptContext(Bindings)
- 取值: put, get
- eval 变体: eval(String/Reader) 委托 eval(.., ScriptContext)

## 2. Java 用户如何使用
脚本引擎继承它只需实现 eval(String/Reader, ScriptContext)、createBindings、getFactory。测试用最小子类验证 put/get、eval 委托、scope 校验异常与构造。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.scripting/share/classes/javax/script/AbstractScriptEngine.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/javax/script/Test1.java

## 4. 测试用例设计 + 自动化实现
- 白盒: 抽象类、implements ScriptEngine、声明 set/getContext 等
- 黑盒: put/get；eval 变体委托；set/getContext；scope 校验 IAE/NPE；Bindings 构造

自动化实现位于同目录 `AbstractScriptEngineTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
