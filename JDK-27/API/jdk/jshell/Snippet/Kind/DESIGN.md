# jdk.jshell.Snippet.Kind 设计案

## 1. API 功能
Snippet 分类枚举：{IMPORT, TYPE_DECL, METHOD, VAR, EXPRESSION, STATEMENT, ERRONEOUS}，附 isPersistent()。

## 2. Java 用户如何使用
eval 后经 snippet().kind() 判读源码形态；isPersistent() 区分声明（进 REPL 状态）与一次性执行。

## 3. 参考来源
- JDK-27 javadoc（jdk.jshell 模块，JDK-8 亦有同名 API 但本侧只做 JDK-27）。
- 实测：JDK-27.0.1 local 执行引擎。

## 4. 测试用例设计 + 易错规范细节
- 枚举表：7 常量、精确声明顺序、valueOf 回环、bogus name → IAE。
- isPersistent 表：IMPORT/TYPE_DECL/METHOD/VAR=true；EXPRESSION/STATEMENT/ERRONEOUS=false。
- 实测映射：每种源码形态 → Kind（含 import 四形态、五种类型声明、赋值/裸名表达式、void 调用归 STATEMENT、垃圾输入 → ERRONEOUS）。
- 易错：`x++` 这类有值的"表达式语句"归 VAR（临时变量）而非 STATEMENT；`class {` → ERRONEOUS 且 probableKind()=TYPE_DECL。
