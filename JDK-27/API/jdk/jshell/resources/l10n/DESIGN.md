# jdk.jshell.resources.l10n（引擎消息目录） 设计案

## 1. API 功能
JShell 核心引擎的 ListResourceBundle 消息目录：5 条诊断（修饰符/ObjectName）+ 4 条异常（null/alien/closed/var.not.valid），共 9 键，值含 {0}/{1} MessageFormat 占位符。

## 2. Java 用户如何使用
引擎内部使用（非法状态/外来 snippet/关闭后使用时抛出的消息文本都出自这里）；l10n_de/ja/zh_CN 是其完整翻译。

## 3. 开源代码实际用法调研
来源：JDK-27 lib/modules 中 jdk/jshell/resources/l10n.class + l10n.properties（jimage 提取比对）。

## 4. 测试用例设计 + 易错点
反射实例化为 ResourceBundle：精确 9 键集合、若干消息全文断言、MessageFormat 可渲染；闭环断言——eval("boolean equals(Object o)...") 的诊断以 jshell.diag.object.method.fatal 文本出现、关闭后 eval 抛 IllegalStateException 带 jshell.exc.closed 文本、跨 shell varValue 抛 jshell.exc.alien 文本。易错：closed 消息的 {0} 是 JShell 实例 toString；用 executionEngine("local") 避免测试内启动远端 JVM。

