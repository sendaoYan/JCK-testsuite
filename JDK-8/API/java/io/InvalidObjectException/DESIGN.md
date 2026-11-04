# java.io.InvalidObjectException 测试方案 (DESIGN)

## 1. API 功能
- 单构造器 `(String)`（JDK 8 无 cause 构造，JDK 9+ 才加入 `(String, Throwable)`）；继承 ObjectStreamException；表示反序列化得到的对象不合法。

## 2. Java 用户如何使用
- readObject 校验回调抛出以拒绝不合法的对象图。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/InvalidObjectException.java`；`ObjectInputStream.readUnshared` 遇反向引用抛出。

## 4. 测试用例设计 + 自动化实现（InvalidObjectExceptionTest.java，纯黑盒）
黑盒场景:
1. `(String)` 构造消息、无 cause。
2. registerValidation 校验抛出的异常以原消息传播。
3. readUnshared 遇反向引用抛 InvalidObjectException。
4. 作为 IOException 捕获。
- 健壮性: 无反射白盒；serialVersionUID 显式固定。

## 深化追加（契约级）
追加内容（JDK-8 侧断言数 35，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
### serialVersionUID 实测值（ObjectStreamClass.lookup，双侧一致）
- `InvalidObjectException`：双侧同值 `3233174318281839583L`
### 新增测试方法
1. testThrowableProtocol（含 null/空 message 角点）
2. testSerializationContract
3. testReadResolveNull（readResolve 返回 null → readObject 得 null）
4. testValidationOrdering（registerValidation 优先级：最高先回调，实测回调序「CAB」）
5. testValidationExceptionRelay（validateObject 抛 ISE 以原样上抛，未包装）
6. testReadUnsharedNull / testTriggeredDiagnostics

### 负例与精确异常（操作 → 类型 → 双侧一致 message 子串）
- validateObject 抛非受检异常 → 原样中继（不包装成 InvalidObjectException）
- JDK-27 独有 (String,Throwable) 构造器

### JDK-8 vs JDK-27 差异
- JDK-27 新增 public (String,Throwable) 构造器；JDK-8 无。
### 序列化往返清单
- 构造实例（message/toString）往返；- cause 往返（深拷贝）；- suppressed 往返；- 真实触发实例再序列化。
