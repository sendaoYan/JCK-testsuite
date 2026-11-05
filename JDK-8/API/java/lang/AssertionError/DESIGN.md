# AssertionError — 设计方案（JDK-8 侧新建，lcO1 批）

## 1. API 功能
`java.lang.AssertionError` 继承自 `Error`，由 `assert` 语句失败时 JVM 抛出，也可直接构造。
公共构造器共 9 个（javap 实测）：`()`、`(Object)`、`(boolean)`、`(char)`、`(int)`、`(long)`、`(float)`、`(double)`、`(String,Throwable)`。
注意：**没有** public `(String)`（存在 private `AssertionError(String)` 供内部委托）与 public `(Throwable)` 构造器——反射面断言 NoSuchMethodException。
消息语义：`new AssertionError(x)` 的 `getMessage()` == `String.valueOf(x)`（无消息形态为 null；`(Object)null` 的消息是字面量 `"null"`；`(String,Throwable)` 形态消息原样保留且即使 cause 为 null 也锁定 cause 槽位，`initCause` 一律 IllegalStateException）。
`serialVersionUID = -5013299493970297370L`（ObjectStreamClass 实测，与 JDK-27 一致）。

## 2. 使用方式
- `assert cond : msg;` 需 `-ea` 启用才会求值/抛出；未启用时语句为 no-op（测试用 `assert flag = true` 副作用探测当前状态，双路径均可绿）。
- 直接 `throw new AssertionError(...)` 作为不可恢复断言失败信号；携带 cause 用 `(String,Throwable)` 构造器。

## 3. 参考来源
Oracle Java SE javadoc（java/lang/AssertionError）；javap 双侧（8/27 构造器集合一致）；双侧实测消息矩阵（true/false/'x'/42/MIN/MAX/1.5f/0.1f/NaN/Infinity/"null" 等逐字全部一致，直锁）。

## 4. 测试用例设计 + 易错规范细节
- 9 构造器逐一：每种原始类型形态 getMessage 逐字全等（双侧一致直锁，无需 MODERN 分支）；边界值 Integer.MIN/MAX_VALUE、Long.MIN/MAX_VALUE、NaN、±Infinity、-0.0。
- `(Object)` 与原始形态重载一致性（Integer.valueOf(42) vs 42 同文本）；`(Object)null` → `"null"`。
- `(String,Throwable)`：message+cause 同一实例、null message 保留、ctor 后 initCause → ISE（含 cause 为 null 时也锁定）。
- 通用 Throwable 契约：initCause self → IAE "Self-causation not permitted"、addSuppressed null → NPE "Cannot suppress a null exception."、self → IAE "Self-suppression not permitted"、setStackTrace/fillInStackTrace/顶帧精确、printStackTrace 首行全等 + `\tat ` + `Suppressed:` 段。
- catch 语义：catch(Error)/catch(Throwable) 能捕获、catch(Exception) 不能。
- assert 语句：`testAssertStatement` 双路径（已修复：未开 -ea 时旧代码 `throw new AssertionError("assert not enabled")` 会被本方法自己的 catch 捕获导致 standalone FAIL；现先探测 -ea 再走对应分支）。jtreg 用 `@run main/othervm -ea` 走真触发路径。
- 序列化 round-trip（message+suppressed 保持）；SUID 精确锁定。
- 断言数 15 → 120（-ea 与否均 PASS）。
