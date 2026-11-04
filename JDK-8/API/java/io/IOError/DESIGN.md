# java.io.IOError 测试方案 (DESIGN)

## 1. API 功能
- 单构造器 `(Throwable)`；继承 Error（未检查）；getMessage 默认为 cause.toString()。

## 2. Java 用户如何使用
- 将严重 I/O 失败作为不可恢复 Error 抛出；包装任意 Throwable。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/IOError.java`。

## 4. 测试用例设计 + 自动化实现（IOErrorTest.java，纯黑盒）
黑盒场景:
1. cause 保留、消息=cause.toString() 且含细节。
2. 包装任意 Throwable（RuntimeException）。
3. 未检查传播（无需 throws）。
4. 是 Error 而非 Exception；作为 Throwable 捕获。
5. toString 命名类 + 栈帧捕获。
- 健壮性: 无反射白盒。

## 深化追加（契约级）
追加内容（JDK-8 侧断言数 40，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
### serialVersionUID 实测值（ObjectStreamClass.lookup，双侧一致）
- `IOError`：双侧同值 `67100927991680413L`
### 新增测试方法
1. testConstructorRules（唯一构造器参数 Throwable；IOError(null) 合法 → message/cause 均 null；message 为构造时快照）
2. testCauseProtocol（Error 通道派发；initCause 恒 ISE；suppressed/栈协议）
3. testSerializationContract（含 null-cause 实例往返）
4. testWrapBoundary（受检→非受检边界包装模式 + 恢复模式还原受检异常）

### 负例与精确异常（操作 → 类型 → 双侧一致 message 子串）
- IOError(null) 不抛 NPE（与 UncheckedIOException 对照）→ getMessage()==null、toString 无 message 段
- Error 通道：任何 Exception handler 都捕获不到 IOError

### JDK-8 vs JDK-27 差异
- 无（构造器面、message 文案、serialVersionUID、行为语义双侧一致）。
### 序列化往返清单
- 构造实例（message/toString）往返；- cause 往返（深拷贝）；- suppressed 往返；- 真实触发实例再序列化。
