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

## 深化追加（契约级）
追加内容（JDK-27 侧断言数 42，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
### serialVersionUID 实测值（ObjectStreamClass.lookup，双侧一致）
- `InvalidClassException`：双侧同值 `-4333316296251054416L`
### 新增测试方法
1. testMessageComposition（classname 与 reason 组合角点：null reason →「Cn; null」；null classname → 裸 reason；双 null → getMessage()==null；classname 可变后影响 getMessage）
2. testThrowableProtocol（全矩阵）
3. testSerializationContract（classname 字段随往返）
4. testUidMismatchTrigger（流内 XOR 0x7F 改 suid 字节 → 双侧触发）

### 负例与精确异常（操作 → 类型 → 双侧一致 message 子串）
- suid 不匹配 → 「local class incompatible: ...; local class incompatible」含两个 UID 十六进制与 classname
- JDK-27 独有 (String,Throwable)/(String,String,Throwable) 构造器（JDK-8 编译期不可用）
- (String, null) 在 JDK-27 上对 (String,String)/(String,Throwable) 二义 → 测试显式 (String) null 转型

### JDK-8 vs JDK-27 差异
- JDK-27 新增 public (String,Throwable)/(String,String,Throwable) 构造器；JDK-8 无。
- JDK-27 上 (String, null) 构造二义（同时匹配 (String,String)/(String,Throwable)），需显式 (String) null。
### 序列化往返清单
- 构造实例（message/toString）往返；- cause 往返（深拷贝）；- suppressed 往返；- 真实触发实例再序列化。
