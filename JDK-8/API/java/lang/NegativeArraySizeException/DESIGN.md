# NegativeArraySizeException — 设计方案（JDK-8 侧新建，lcO1 批）

## 1. API 功能
继承 `RuntimeException`。构造器 2 个：`()`、`(String)`（无 (int)——与 AIOOBE 不同）。`serialVersionUID = -8960118058596991861L`（双侧一致）。

## 2. 使用方式
`new T[负数]`（任意维度、任意元素类型）与 `java.lang.reflect.Array.newInstance(T, 负数)` 时 JVM/反射抛出。

## 3. 参考来源
JLS 15.10.1（数组创建表达式负长度抛 NASE）；双侧实测消息差异：8 侧触发消息为 null；27 侧消息是尺寸文本（"-1"/"-7"/"-2147483648"/"-5"）。

## 4. 测试用例设计 + 易错规范细节
- 触发矩阵（MODERN 分支）：`new int[-1]/[-7]/[Integer.MIN_VALUE]`、`new Object[-1]`、`new byte[-2]`、二维 `new String[1][-3]`、反射 `Array.newInstance(int.class,-1)` 与 `(String.class,-5)`——8 侧消息 null、27 侧精确数字，各锁。
- 正例：`new int[0]`、`newInstance(int.class,3)`、零长数组不抛。
- 层级（RuntimeException 精确父类）+ 通用契约 + SUID + 序列化。
- 断言数 7 → 70（27 侧 20 → 81）。
