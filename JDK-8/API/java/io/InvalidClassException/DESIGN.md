# java.io.InvalidClassException 测试方案 (DESIGN)

## 1. API 功能
- 2 构造器：`(String reason)`、`(String cname, String reason)`（JDK 8 无 cause 构造，JDK 9+ 才加入）；公共字段 `String classname`；getMessage 组合为 `"cname; reason"`。

## 2. Java 用户如何使用
- 反序列化类不兼容/无合法构造时捕获；读取 classname 与 reason。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/InvalidClassException.java`；`ObjectStreamClass.getExternalizableConstructor` 抛 "no valid constructor"。

## 4. 测试用例设计 + 自动化实现（InvalidClassExceptionTest.java，纯黑盒）
黑盒场景:
1. reason-only：消息原样、classname null。
2. cname+reason：classname 字段 + 组合消息。
3. JDK 8 两构造 getCause 均为 null。
4. 无 public 无参构造的 Externalizable 类反序列化抛 "no valid constructor"。
5. 作为 ObjectStreamException 捕获。
- 健壮性: 无反射白盒（JDK 8 无 ObjectInputFilter）。

## 深化追加（契约级）
追加内容（JDK-8 侧断言数 35，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
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
