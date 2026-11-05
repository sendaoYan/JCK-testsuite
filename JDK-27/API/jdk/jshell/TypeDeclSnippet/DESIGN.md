# jdk.jshell.TypeDeclSnippet 设计案

## 1. API 功能
类型声明 snippet（class/interface/enum/record/@interface），继承 DeclarationSnippet；无自有公开方法，语义全在 name()/subKind()/status()。

## 2. Java 用户如何使用
js.eval("class P{...}") → TypeDeclSnippet；types() 流查询。

## 3. 参考来源
- JDK-27 javadoc；实测 local 引擎。

## 4. 测试用例设计 + 易错规范细节
- 五种类型形态的 subKind 矩阵；types() 恰好收录全部类型声明。
- 闭环：声明后跨 snippet new/枚举常量/record 访问器/内部类使用。
- 生命周期：extends 已声明类型 VALID；extends 未定义类型 RECOVERABLE_NOT_DEFINED（name 仍可用）；bogus 成员访问 REJECTED。
- 易错：`new TD4(9).q()` 值为 "9"（int 渲染无引号），`TD3.A.name()` 值为 "\"A\""（String 渲染带引号）。
