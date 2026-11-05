# jdk.jshell.Snippet.Status 设计案

## 1. API 功能
Snippet 生命周期状态枚举：{VALID, RECOVERABLE_DEFINED, RECOVERABLE_NOT_DEFINED, DROPPED, OVERWRITTEN, REJECTED, NONEXISTENT}。

## 2. Java 用户如何使用
js.status(snippet) 轮询；事件里 previousStatus/status 成对出现。NONEXISTENT 只作为首个事件的 previousStatus 出现。

## 3. 参考来源
- JDK-27 javadoc；实测 local 引擎。

## 4. 测试用例设计 + 易错规范细节
- 枚举表：7 值精确顺序 + valueOf 回环 + bogus IAE。
- 可达性闭环（全部 7 态经真实引擎触发）：
  - VALID：普通声明；RECOVERABLE_DEFINED：方法体引用未定义符号（diag 为空、unresolvedDependencies 非空——与 REJECTED 的分界）。
  - RECOVERABLE_NOT_DEFINED：`class C extends Undefined {}` 或 drop 基类后子类降级。
  - DROPPED：drop；OVERWRITTEN：同名重declare；REJECTED：语法错/未解析的 var 初始化式。
  - NONEXISTENT：作为新 snippet 首事件的 previousStatus 出现。
- 级联：补定义依赖后 RECOVERABLE_*→VALID 自动升级。
- 易错：drop 依赖变量后，已初始化的依赖方 var 保持 VALID（不级联）；只有类型层级依赖才会降级 RECOVERABLE_NOT_DEFINED。
