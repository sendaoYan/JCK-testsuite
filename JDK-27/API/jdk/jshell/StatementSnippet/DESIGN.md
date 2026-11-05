# jdk.jshell.StatementSnippet 设计案

## 1. API 功能
语句 snippet（无值可执行单元）；无自有公开方法。

## 2. Java 用户如何使用
eval("for(...);") → StatementSnippet，STATEMENT_SUBKIND（isExecutable=true, hasValue=false）。

## 3. 参考来源
- JDK-27 javadoc；实测 local 引擎。

## 4. 测试用例设计 + 易错规范细节
- void 调用归 STATEMENT（有值表达式才是 EXPRESSION）。
- 副作用跨 snippet 可见（stCounter += 5 后 varValue 读 15）。
- while(false) 体不可达 → 非 VALID，改用 do-while。
- broken 语句 → ERRONEOUS 且 probableKind()=STATEMENT。
- 语句不污染 methods()/types()/imports()；variables() 只含 VAR kind（含临时变量）。
