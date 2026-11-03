# javax.script.ScriptException 测试设计

## 1. API 功能
脚本异常，携带文件名/行号/列号，可包装底层异常。

按方法分组：
- 构造: (String), (Exception), (String,String,int), (String,String,int,int)
- 查询: getMessage, getFileName, getLineNumber, getColumnNumber

## 2. Java 用户如何使用
引擎执行失败时抛 ScriptException，消息拼接文件名与行列。测试断言各构造的消息格式与默认 -1/null。

## 3. 开源代码实际用法调研
- JDK 源码: ~/git/jdk25u-dev/src/java.scripting/share/classes/javax/script/ScriptException.java
- JDK 测试: ~/git/jdk25u-dev/test/jdk/javax/script/CauseExceptionTest.java

## 4. 测试用例设计 + 自动化实现
- 白盒: 具体类、继承 Exception、四个 getter
- 黑盒: 各构造消息格式；默认 fileName null/行 -1/列 -1；cause 包装

自动化实现位于同目录 `ScriptExceptionTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
