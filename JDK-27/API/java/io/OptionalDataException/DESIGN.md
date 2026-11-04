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
