# javax.script.ScriptEngineFactory 测试设计

## 1. API 功能
脚本引擎工厂元数据：名称/版本/扩展名/MIME/语言/参数/代码生成/引擎实例。

按方法分组：
- 元数据: getEngineName/Version, getExtensions, getMimeTypes, getNames, getLanguageName/Version
- 参数: getParameter(String)
- 代码生成: getMethodCallSyntax, getOutputStatement, getProgram
- 实例: getScriptEngine()

## 2. Java 用户如何使用
ScriptEngineManager 通过工厂发现并创建引擎。测试用匿名工厂断言各 getter 与 getParameter 标准 key 映射。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.scripting/share/classes/javax/script/ScriptEngineFactory.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/java/util/ServiceLoader/modules/bananascript/org/banana/BananaScript.java

## 4. 测试用例设计 + 自动化实现
- 白盒: 接口、getEngineName 等
- 黑盒: 各 getter；getParameter ENGINE/ENGINE_VERSION/LANGUAGE/LANGUAGE_VERSION/NAME；语法/输出/程序生成；getScriptEngine

自动化实现位于同目录 `ScriptEngineFactoryTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。

## 5. 深化增量（契约覆盖）
追加 `testDeepContracts()`：黑盒行为矩阵（构造/读写/视图/null 负例精确异常与消息片段），
JDK-8 与 JDK-27 行为一致时共享同一断言文本。
