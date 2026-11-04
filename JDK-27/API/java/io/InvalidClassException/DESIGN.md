# java.io.InvalidClassException 测试方案 (DESIGN)

## 1. API 功能
- `(reason)` / `(cname, reason)` / `(reason, cause)` / `(cname, reason, cause)`：反序列化发现类无效（SUID 不匹配、无有效构造器、filter 拒绝等）；公共字段 `classname`；`getMessage()` 组合为 `cname + "; " + reason`。
- JDK 19 起增加带 cause 的构造（JDK-8277083）。

## 2. Java 用户如何使用
- 升级类定义后读旧数据、或 filter 拒绝恶意类时捕获；从 message 中取 `classname` 定位问题类。

## 3. 开源代码实际用法调研
- message 组合 `classname + "; " + super.getMessage()` — `jdk25u-dev/src/java.base/share/classes/java/io/InvalidClassException.java:104-112`。
- "no valid constructor" 触发 — `jdk25u-dev/src/java.base/share/classes/java/io/ObjectStreamClass.java`（Externalizable 构造器检查）。
- filter 拒绝 "filter status: REJECTED" — `jdk25u-dev/src/java.base/share/classes/java/io/ObjectInputStream.java`（filterCheck）。

## 4. 测试用例设计 + 自动化实现（InvalidClassExceptionTest.java）
- 白盒: extends ObjectStreamException、public classname、getMessage 重写、4 构造。
- 黑盒场景:
  1. reason-only：message 原样、classname null。
  2. cname+reason：classname 字段 + `"cname; reason"` 组合 message。
  3. 两个带 cause 构造保留 cause 与组合 message。
  4. 真实触发 1：`!*` filter 令 readObject 抛出且 message 含 REJECTED。
  5. 真实触发 2：Externalizable 类仅有带参构造 → "no valid constructor"。
  6. 可按 ObjectStreamException 捕获。
- 健壮性: filter 仅设置在独立流实例上（非全局），全内存。

产出：`InvalidClassExceptionTest.java`（白盒 6 + 黑盒 8 断言）。
