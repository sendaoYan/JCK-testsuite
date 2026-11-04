# java.io.ObjectStreamException 测试方案 (DESIGN)

## 1. API 功能
- 抽象类，所有序列化机制异常的父类；JDK 8 仅两个 protected 构造：`()` 与 `(String)`（`(Throwable)`/`(String,Throwable)` 为后续版本新增）。

## 2. Java 用户如何使用
- `catch (ObjectStreamException e)` 一次性捕获全部序列化异常（InvalidClass/InvalidObject/NotActive/NotSerializable/OptionalData/StreamCorrupted/WriteAborted）。

## 3. 开源代码实际用法调研
- 类型定义 — `jdk8u-dev/jdk/src/share/classes/java/io/ObjectStreamException.java`（protected 构造族）。
- 各具体子类 — 同目录下 InvalidClassException.java 等 7 个文件。

## 4. 测试用例设计 + 自动化实现（ObjectStreamExceptionTest.java，纯黑盒）
黑盒场景:
1. 抽象类反射实例化被拒。
2. 子类经 `()` 与 `(String)` 两个 protected 构造的 message 行为。
3. 子类异常被 `catch (ObjectStreamException)` 捕获。
4. 具体 JDK 子类均属于 ObjectStreamException/IOException 家族（OptionalDataException 无公共构造，用 isAssignableFrom 校验）。
5. toString/stackTrace 行为。
- 健壮性: 全内存。

## 深化追加（契约级）
追加内容（JDK-8 侧断言数 72，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
### serialVersionUID 实测值（ObjectStreamClass.lookup，双侧一致）
- `ObjectStreamException`：双侧同值 `7260898174833392607L`
### 新增测试方法
1. testReflectiveStructure（protected 构造器计数：JDK-8=2、JDK-27=4；abstract 断言；无公有构造器）
2. testThrowableProtocol（经子类 MyStreamException）
3. testSerializationContract（根 suid + 7 个子类 suid 钉死）
4. testFamilyDispatch（6 个具体成员 × ObjectStreamException/IOException/Exception/Serializable 四通道派发）

### 负例与精确异常（操作 → 类型 → 双侧一致 message 子串）
- JDK-27 新增 (Throwable)/(String,Throwable) protected 构造器（JDK-8 仅 ()/(String)）

### JDK-8 vs JDK-27 差异
- JDK-8 protected 构造器 2 个（()/(String)）；JDK-27 为 4 个（增 (Throwable)/(String,Throwable)）。
### 序列化往返清单
- 构造实例（message/toString）往返；- cause 往返（深拷贝）；- suppressed 往返；- 真实触发实例再序列化。
