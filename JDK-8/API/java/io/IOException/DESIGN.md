# java.io.IOException 测试方案 (DESIGN)

## 1. API 功能
- 4 构造器：`()`、`(String)`、`(String, Throwable)`、`(Throwable)`；java.io 异常层次受检根。

## 2. Java 用户如何使用
- 作为大多数 java.io 异常的基类捕获；支持 initCause 与 addSuppressed。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/IOException.java`。

## 4. 测试用例设计 + 自动化实现（IOExceptionTest.java，纯黑盒）
黑盒场景:
1. 四构造器消息/cause 矩阵。
2. initCause 仅一次 + 拒绝自引用。
3. addSuppressed 顺序保留。
4. 异常自身序列化保留消息。
5. 真实 API 抛 EOFException 可作 IOException 捕获。
6. try-with-resources 兼容。
7. toString 格式 + 栈帧捕获。
- 健壮性: 无反射白盒。

## 深化追加（契约级）
追加内容（JDK-8 侧断言数 85，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
### serialVersionUID 实测值（ObjectStreamClass.lookup，双侧一致）
- `IOException`：双侧同值 `7818375828146090155L`
### 新增测试方法
1. testConstructorEdges（cause 提取规则：(Throwable)→message==cause.toString()；(String,null)→cause 为 null）
2. testThrowableProtocol（getMessage/getLocalizedMessage/toString/initCause 二次 ISE/initCause(null) 仅未设置时允许/setStackTrace 往返+null NPE/addSuppressed 顺序+自抑制 IAE+null NPE/fillInStackTrace==this）
3. testSerializationContract（suid + 消息/cause/suppressed 往返）
4. testFamilyHierarchy（15 个 java.io 异常家族 instanceof 矩阵）
5. testRealThrowSurfaces（EOFException/FNFE 等真实抛出可作 IOException 捕获）

### 负例与精确异常（操作 → 类型 → 双侧一致 message 子串）
- initCause 二次 → IllegalStateException「Can't overwrite cause with ...」
- initCause(this) → IllegalArgumentException「Self-causation not permitted」（在 Throwable 基类层面）
- addSuppressed(this) → IllegalArgumentException「Self-suppression not permitted」
- setStackTrace(null) → NullPointerException

### JDK-8 vs JDK-27 差异
- 无（构造器面、message 文案、serialVersionUID、行为语义双侧一致）。
### 序列化往返清单
- 构造实例（message/toString）往返；- cause 往返（深拷贝）；- suppressed 往返；- 真实触发实例再序列化。
