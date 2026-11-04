# java.io.OptionalDataException 测试方案 (DESIGN)

## 1. API 功能
- final 类，包私有构造（(int length)/(boolean eof)），公共字段 `length`（可读原始字节数）与 `eof`。readObject 遇到原始数据（eof=false, length=N）或读到对象时流恰好结束（eof=true）时抛出。

## 2. Java 用户如何使用
- 对方按原始类型写、本地按对象读（或反之）时定位流不匹配；可凭 length 先 readInt 把数据读掉再读对象。

## 3. 开源代码实际用法调研
- 抛出与字段赋值 — `jdk25u-dev/src/java.base/share/classes/java/io/ObjectInputStream.java`（readObject0 对 DRAFT/block data 的处理）。
- 构造包私有 — `jdk25u-dev/src/java.base/share/classes/java/io/OptionalDataException.java`。

## 4. 测试用例设计 + 自动化实现（OptionalDataExceptionTest.java）
- 白盒: final、extends ObjectStreamException、length/eof 公共字段、构造全部包私有。
- 黑盒场景:
  1. 真实触发 1：只写 int 再 readObject → eof=false、length=4。
  2. length 随缓冲数据量缩放（long+int → 12）。
  3. 异常后数据仍可用 readInt 读回。
  4. 对照：先读 int 再读对象正常。
  5. 规则：writeObject 附加的自定义数据对无 readObject 的类被自动跳过，流位置正确。
  6. 包私有构造在强封装下 setAccessible 被拒（InaccessibleObjectException）。
- 健壮性: 全内存流。

产出：`OptionalDataExceptionTest.java`（白盒 7 + 黑盒 8 断言）。

## 深化追加（契约级）
追加内容（JDK-27 侧断言数 42，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
### serialVersionUID 实测值（ObjectStreamClass.lookup，双侧一致）
- `OptionalDataException`：双侧同值 `-8011121865681257820L`
### 新增测试方法
1. testReflectiveSurface（无公有构造器；公有 eof/length 字段；final 标志两侧断言不同）
2. testFieldAndThrowableProtocol（触发实例上 initCause/addSuppressed/字段就地赋值）
3. testSerializationContract（触发实例再序列化保留 eof/length）
4. testTriggerMatrix（trigger(int...)：可重复性 ×3、bool+long=9、writeUTF=13、干净 EOF → EOFException 对照）

### 负例与精确异常（操作 → 类型 → 双侧一致 message 子串）
- 触发实例 message 为 null、toString 无 message 段
- readObject 遇块数据可重复抛（数据不消费）：length 恒 4
- JDK-8 类非 final；JDK-27 类 final（版本差异断言分支）

### JDK-8 vs JDK-27 差异
- JDK-8 类非 final；JDK-27 类 final。
### 序列化往返清单
- 构造实例（message/toString）往返；- cause 往返（深拷贝）；- suppressed 往返；- 真实触发实例再序列化。
