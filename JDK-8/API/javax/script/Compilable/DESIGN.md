# javax.script.Compilable 测试设计

## 1. API 功能
支持预编译脚本的引擎接口：compile(String/Reader) -> CompiledScript。

按方法分组：
- 操作: compile(String), compile(Reader)

## 2. Java 用户如何使用
引擎可把脚本预编译为 CompiledScript 重复执行。测试用匿名实现返回最小 CompiledScript 并 eval。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.scripting/share/classes/javax/script/Compilable.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/javax/script/Test1.java

## 4. 测试用例设计 + 自动化实现
- 白盒: 接口、无父接口、compile 方法
- 黑盒: compile(String/Reader) 返回 CompiledScript；getEngine 非空；eval

自动化实现位于同目录 `CompilableTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。

## 5. JDK-8 侧差异（Nashorn 实编译）
JDK-8 用 Nashorn `compile()` 真编译：编译一次多次 eval 结果一致；语法错误 ScriptException 行/列=1；
编译产物与逐次 eval 共享引擎状态。JDK-27 侧以自造 Compilable/CompiledScript 验证等价契约。
