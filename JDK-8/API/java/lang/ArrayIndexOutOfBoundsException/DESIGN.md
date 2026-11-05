# ArrayIndexOutOfBoundsException — 设计方案（JDK-8 侧新建，lcO1 批）

## 1. API 功能
继承 `IndexOutOfBoundsException`。构造器 3 个（javap 双侧一致）：`()`、`(String)`、**`(int index)`**（注意：JDK-8 也有 (int) 构造器，与 IndexOutOfBoundsException 不同——后者 8 无 (int)）。
`serialVersionUID = -5116101128118950844L`（双侧一致）。

## 2. 使用方式
数组越界读写由 JVM 抛出；`(int)` 构造器用于带索引构造。

## 3. 参考来源
Oracle javadoc；双侧实测：`new AIOOBE(n).getMessage()` == `"Array index out of range: n"`（0/负数/MIN/MAX）——**双侧逐字一致直锁**；JVM 触发的消息双侧不同（见差异）。

## 4. 测试用例设计 + 易错规范细节（版本差异重点）
- `(int)` 构造消息矩阵：5/-7/0/2147483647/-2147483648 → "Array index out of range: N"，toString 首行全等。
- **JVM 触发矩阵（MODERN 分支锁差异）**：int[3] 读 a[3]/a[-1]、写 a[5]、`new int[3][5]` 外层 [3] 与内层 [0][5]、String[2] 读 [7]、long[2] 读 [2]、char[1] 读 [-2]——8 侧消息是裸索引（"3"/"-1"/"5"），27 侧是 "Index 3 out of bounds for length 3" 形，各锁各的。
- 层级：AIOOBE instanceof IOOBE/RuntimeException；IOOBE 实例不是 AIOOBE（单向子类型）；父类精确 == IndexOutOfBoundsException。
- boolean/double 数组触发、boolean[] 读、double[] 写均精确类。
- 通用契约 + SUID + 序列化；JVM 抛出实例详情。
- 断言数 8 → 90（27 侧 21 → 101）。
