# javax.script.CompiledScript 测试设计

## 1. API 功能
预编译脚本抽象类，持有引擎并可多次 eval。

按方法分组：
- 构造: CompiledScript()
- eval: eval(ScriptContext) 抽象; eval(Bindings); eval()
- 查询: getEngine() 抽象

## 2. Java 用户如何使用
eval() 用引擎默认上下文，eval(Bindings) 用给定绑定作 ENGINE_SCOPE。测试用匿名子类验证三种 eval 与上下文/绑定传递。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.scripting/share/classes/javax/script/CompiledScript.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/javax/script/Test1.java

## 4. 测试用例设计 + 自动化实现
- 白盒: 抽象类、eval/getEngine
- 黑盒: eval() 用引擎上下文；eval(Bindings) 传绑定；eval(null) 用引擎上下文

自动化实现位于同目录 `CompiledScriptTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。

## 5. JDK-8 侧差异（Nashorn 实编译）
JDK-8 `cs.eval()` 与 `cs.eval(engine context)` 等价（同一 context 对象恒等）；`cs.eval(Bindings)` 构造临时
context（engine scope=入参，global 拷贝，reader/writer 随行）；getContext() 不存在（接口未定义）。
JDK-27 侧自造引擎验证同一语义。
