# java.io.InvalidObjectException 测试方案 (DESIGN)

## 1. API 功能
- `(String)` / `(String, Throwable)`（JDK 19+）：反序列化出的对象未通过自检（validateObject 抛出）或 readUnshared 遇到回引用。

## 2. Java 用户如何使用
- 在 readObject 中 `registerValidation` 注册校验回调，回调抛本异常令 readObject 失败；不变量破损即拒绝该对象图。

## 3. 开源代码实际用法调研
- 校验回调抛出传播 — `jdk25u-dev/src/java.base/share/classes/java/io/ObjectInputStream.java`（doCallbacks/validateObject 路径）。
- readUnshared 回引用失败 — ObjectInputStream.readObject0 的 TC_ENDBLOCKDATA/unshared 分支（官方 `test/jdk/java/io/Serializable/readUnshared/` 系列覆盖）。

## 4. 测试用例设计 + 自动化实现（InvalidObjectExceptionTest.java）
- 白盒: extends ObjectStreamException、2 构造、受检。
- 黑盒场景:
  1. 两构造的 message/cause。
  2. 真实触发 1：registerValidation 回调抛 InvalidObjectException("invariant broken") 从 readObject 传播且 message 一致。
  3. 真实触发 2：同一对象写两次后第二个回引用用 readUnshared 读 → 抛出。
  4. 可按 IOException 捕获。
- 健壮性: 全内存流。

产出：`InvalidObjectExceptionTest.java`（白盒 4 + 黑盒 6 断言）。

## 深化追加（契约级）
追加内容（JDK-27 侧断言数 39，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
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
