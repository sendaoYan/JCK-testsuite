# ExceptionInInitializerError — 设计方案（JDK-8 侧新建，lcO1 批）

## 1. API 功能
`java.lang.ExceptionInInitializerError` 继承 `LinkageError`，由 JVM 在类/接口静态初始化（`<clinit>`）抛出非 Error 异常时包装抛出（JLS 12.4.2）。
公共构造器 3 个：`()`、`(Throwable)`、`(String)`；特有访问器 `getException()`（返回 `<clinit>` 抛出的异常），`getCause()` 与其同一。
`serialVersionUID = 1521711792217232256L`（双侧一致）。三个构造形态的 cause 槽位全部锁定：任何 `initCause` 调用都抛 IllegalStateException（"Can't overwrite cause with ..."）。

## 2. 使用方式
典型是捕获后用 `getCause()/getException()` 取出初始化失败的根因；第二次再触碰该类（读静态字段/调静态方法/new/Class.forName）抛 `NoClassDefFoundError: Could not initialize class <binary-name>`。

## 3. 参考来源
JLS 12.4.2（类初始化两阶段失败语义）；Oracle javadoc；双侧实测：NCDFE 二次消息 "Could not initialize class X" 在 8/27 逐字一致；static 块抛 Error（OOM）直接传播不包装（双侧一致）。

## 4. 测试用例设计 + 易错规范细节（招牌闭环）
- **首次访问闭环**：`MarkerHolder.MARKER`（独立 holder 持有同一 RuntimeException 实例）由 `BadInitRt` 的 static 块抛出 → 首次读 `BadInitRt.A` 得 EIIE，`getCause()==MARKER` 同一实例、`getException()==getCause()`、消息 null、printStackTrace 含 `Caused by: java.lang.RuntimeException: MARKER`。
- **二次失败闭环**（4 个独立触发点）：读静态字段 B / 调静态方法 / new 实例 / `Class.forName(name)` 全部 → `NoClassDefFoundError`，消息逐字 `"Could not initialize class " + BadInitRt.class.getName()`。
- **静态初始化表达式**：`static final int X = 1/0` → EIIE 包装 ArithmeticException（cause 消息 "/ by zero"）+ 二次 NCDFE。
- **Error 不包装**：static 块抛 OutOfMemoryError → 直接传播 OOM（非 EIIE）；二次访问 NCDFE。
- 构造矩阵/通用契约/序列化 round-trip（wrapped exception 保持）/反射面（无 (String,Throwable) 构造器）。
- 易错点：static 块无条件 throw 需写成 `if (true) throw ...;` 才能通过 "initializer must be able to complete normally" 编译检查；marker 必须放独立类，否则二次读取字段本身会先抛 NCDFE。
- 断言数 9 → 92。
