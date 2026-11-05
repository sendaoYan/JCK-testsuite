# ClassCastException — 设计方案（JDK-8 侧新建，lcO1 批）

## 1. API 功能
继承 `RuntimeException`。构造器 2 个：`()`、`(String)`。`serialVersionUID = -9223365651070458532L`（双侧一致）。

## 2. 使用方式
引用类型强转失败时 JVM 抛出；null 引用可 cast 到任意类型（不抛）；`Class.cast` 失败同样抛 CCE。

## 3. 参考来源
JLS 5.5（casting conversion）；双侧实测消息格式差异（本轮锁定的核心差异之一）。

## 4. 测试用例设计 + 易错规范细节（消息差异重点）
- 触发矩阵（MODERN 分支逐字锁）：`(Object)Integer → String`：8 侧 `"java.lang.Integer cannot be cast to java.lang.String"`（逐字）；27 侧 `"class java.lang.Integer cannot be cast to class java.lang.String (… module java.base of loader 'bootstrap')"`（锁 startsWith 前缀 + contains module 信息）。数组 `[I → String`、接口 `Thread → Comparable`、`Double/Boolean/String→Integer/ArrayList→CharSequence` 同法。
- 正例（不抛）：null cast 返回 null、正确 cast 同一引用（==）、向上转接口/父类、cast 到 Object 恒成功、`Integer → Number`。
- 层级（精确父类 RuntimeException）+ JVM 抛出实例详情（cause null、栈非空）。
- 通用契约（构造矩阵/cause/suppressed/trace/print/SUID/序列化 round-trip）。
- 断言数 7 → 71（27 侧 20 → 82）。
