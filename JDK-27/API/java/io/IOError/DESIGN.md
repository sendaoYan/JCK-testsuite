# java.io.IOError 测试方案 (DESIGN)

## 1. API 功能
- `(Throwable cause)`：严重不可恢复 I/O 错误（Error 而非 Exception），message 默认 cause.toString()，无 message-only 构造。

## 2. Java 用户如何使用
- 表示"不该被 catch (IOException) 恢复"的致命 I/O 问题（如 Console 读失败）；一般不捕获，任其终止线程。

## 3. 开源代码实际用法调研
- 类型定义 — `jdk25u-dev/src/java.base/share/classes/java/io/IOError.java`。
- 抛出先例 — `jdk25u-dev/src/java.base/share/classes/java/io/Console.java`（readLine/readPassword 的底层失败包装为 IOError）。

## 4. 测试用例设计 + 自动化实现（IOErrorTest.java）
- 白盒: extends Error、唯一 (Throwable) 构造、非 RuntimeException。
- 黑盒场景:
  1. 构造：cause 保留、message=cause.toString()。
  2. 任意 Throwable（不止 IOException）可包装。
  3. 无 (String) 构造（反射证实）。
  4. 不受检传播。
  5. 是 Error 不是 Exception（类型系统证明，杜绝被 catch(IOException) 误捕）。
  6. toString/stackTrace。
- 健壮性: 全内存。

产出：`IOErrorTest.java`（白盒 4 + 黑盒 10 断言）。

## 深化追加（契约级）
追加内容（JDK-27 侧断言数 45，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
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
