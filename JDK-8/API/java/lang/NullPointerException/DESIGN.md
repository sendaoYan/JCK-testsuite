# NullPointerException — 设计方案（JDK-8 侧新建，lcO1 批）

## 1. API 功能
继承 `RuntimeException`。构造器 2 个：`()`、`(String)`；**无 `(String,Throwable)`（javap 双侧确认，反射断言 NoSuchMethodException）**。`serialVersionUID = 5162710183389028792L`（双侧一致）。

## 2. 使用方式
null 解引用（读字段/调方法/数组读写/拆箱/`synchronized(null)`）由 JVM 抛出；`Objects.requireNonNull` 手动抛出（无消息/带消息两形态）。

## 3. 参考来源
JLS 15.11.1 等（null 引用字段访问抛 NPE）；JEP 358（JDK 14+ Helpful NullPointerExceptions，27 侧默认开启）；双侧实测：8 侧触发消息为 null，27 侧 helpful 消息（方法名/字段名形式，如 `Cannot invoke "String.length()" because ... is null`、字段版含 `NpeHolder.field`）。

## 4. 测试用例设计 + 易错规范细节
- 触发矩阵：`((String)null).length()`（**27 只锁类型级特征**：消息 startsWith `Cannot invoke "String.length()"` 且 contains "is null"——局部变量名 `<localN>` 依赖编译期 -g 信息不锁）、null 数组读/写、null 字段读（27 锁 `NpeHolder.field` 字段名，稳定）、拆箱 null Integer（27 锁 contains "Integer.intValue()"）、`synchronized (null)`。
- `Objects.requireNonNull` 三形态（null→NPE 消息 null；(null,"m1")→消息 "m1"；非 null 透传）。
- 构造面：()/(String)/null 形态 + (String,Throwable) ABSENT + declared ctor 数 == 2。
- 层级 + 通用契约 + SUID + 序列化 + JVM 抛出实例详情。
- 断言数 7 → 73（27 侧 20 → 85）。
