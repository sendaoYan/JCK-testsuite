# jdk.jfr.tracing.MethodTracer 设计案（JDK-27）

## 1. API 功能
`jdk.jfr.tracing` 是 module jdk.jfr 的 contains 包（未导出），MethodTracer 是 JFR 内部方法级追踪的门面：6 个 `trace*`/`timing*` 方法（`(long, long)` 签名）向已激活的 jdk.MethodTiming/jdk.MethodTrace 事件提交记录。

## 2. Java 用户如何使用
无公开用户面——包未导出，仅供 JDK 内部（jcmd/追踪基础设施）反射访问；测试经 `@modules jdk.jfr/jdk.jfr.tracing:+open` 白盒检视。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/tracing/MethodTracer.java（包声明在 module jdk.jfr 的 contains 列表）。

## 4. 测试用案设计 + 自动化实现
白盒（结构断言：类名/方法面/包未导出）+ 黑盒（可调用性：全部 trace/timing 方法可反射调用不抛）。骨架 `static int passed; check(boolean,String)`。

## 5. 深化（本轮）
断言规模：基线 → 12+深化。
### 行为闭环
- timestamp 语义：连续两次取时间戳单调不减。
- 6 个 trace/timing 方法 `(long, long)` 实参可调用（未激活对应事件时静默返回，不抛异常）。
- 包未导出断言：`jdk.jfr` 模块对本包 `isExported("jdk.jfr.tracing") == false`、`isOpen(...) == false`（需 --add-opens 才可深访问）。
### 实测负例异常表
无专门负例（内部门面，未激活时静默）。
### 8/27 差异
8 侧无 jdk.jfr.tracing 包（27 新增）。
