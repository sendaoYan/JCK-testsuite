# javax.script.ScriptEngine 测试设计

## 1. API 功能
脚本引擎接口：eval 变体、属性存取、Bindings/上下文/工厂。

按方法分组：
- 常量: ARGV/FILENAME/ENGINE/ENGINE_VERSION/NAME/LANGUAGE/LANGUAGE_VERSION
- eval: eval(String/Reader) 与带 ScriptContext/Bindings 的变体
- 属性: put, get
- 作用域: get/setBindings, createBindings
- 上下文/工厂: get/setContext, getFactory

## 2. Java 用户如何使用
脚本引擎用户通过 eval 执行脚本、put 传变量。测试用 AbstractScriptEngine 子类断言常量与各方法。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.scripting/share/classes/javax/script/ScriptEngine.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/javax/script/Test1.java

## 4. 测试用例设计 + 自动化实现
- 白盒: 接口、eval/put/get/getBindings/setBindings
- 黑盒: 七个常量；eval 变体；put/get；createBindings；get/setContext；set/getBindings

自动化实现位于同目录 `ScriptEngineTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。

## 5. JDK-8 侧差异（Nashorn 实引擎）
JDK-8 深化用 `new ScriptEngineManager().getEngineByName("js")` 真执行：`1+2`→Integer、`41/2`→Double(20.5)、
`'a'+'b'`→String、`1<2`→Boolean、空语句/空脚本→null；`var carry=40` 后 `carry+2`→**Double 42.0**（var 算术装箱 Double）；
engine-scope 状态跨 eval 持久。JDK-27 侧以测试内自造 ScriptEngine 实现同一契约（无内置引擎）。
