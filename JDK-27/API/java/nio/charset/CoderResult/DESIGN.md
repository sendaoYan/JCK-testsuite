# java.nio.charset.CoderResult 设计文档

## 1. API 功能

描述 `CharsetEncoder/Decoder` 单次编解码操作结果的对象，final 类。

- 常量/工厂：`UNDERFLOW`（输入不足）、`OVERFLOW`（输出不足）、`malformedForLength(int)`（非法输入）、
  `unmappableForLength(int)`（不可映射字符）。
- 查询：`isUnderflow()`/`isOverflow()`/`isError()`/`isMalformed()`/`isUnmappable()`/`length()`。
- 转换：`toString()`（`"UNDERFLOW"`/`"OVERFLOW"`/`"MALFORMED[n]"`/`"UNMAPPABLE[n]"`）。
- 异常投递：`throwException()` 把结果转换为对应的 `BufferUnderflowException`/`BufferOverflowException`/
  `MalformedInputException`/`UnmappableCharacterException`。

## 2. Java 用户如何使用

- 组合链路：`enc.encode(in, out, true)` → 得到 `CoderResult` → `if (cr.isUnderflow()) ...` 或 `cr.throwException()`。
- 边界：`UNDERFLOW/OVERFLOW.length()` 抛 `UnsupportedOperationException`；`malformedForLength(0)` 抛 `IllegalArgumentException`。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.base/share/classes/java/nio/charset/CoderResult.java`）：
  `names = {"UNDERFLOW","OVERFLOW","MALFORMED","UNMAPPABLE"}`；`toString()` 对错误追加 `[length]`；
  `throwException()` 按 type 分派到四类异常；错误长度缓存于 `malformed4/unmappable4` 数组与 `ConcurrentHashMap`。

## 4. 测试用例设计 + 自动化实现

文件：`CoderResultTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / final / 父类 `Object` / `UNDERFLOW`/`OVERFLOW` 常量。
- 黑盒：常量判定组合 → `toString()` → `length()` 异常 → `throwException()` 四类分派 →
  `malformedForLength/unmappableForLength` 的 isError/isMalformed/isUnmappable/length → 非法长度参数异常 → 缓存同一性。

### 第 4 部分增补（A3 批次：契约深化）

1. **工厂缓存**: malformed/unmappableForLength 同长度同实例（n=1/2 及 100——缓存随最大请求长度
   扩容，双侧实测）；不同长度/不同角色为不同对象；equals/hashCode 自洽；malformed≠unmappable。
2. **谓词矩阵补全**: MALFORMED[1]/UNMAPPABLE[1] toString；错误结果 isUnderflow/isOverflow 均
   false、isError 均 true、isMalformed/isUnmappable 互斥；两常量 isMalformed/isUnmappable 均
   false（补全既有 6 断言矩阵）。
3. **负长度**: malformedForLength(-1)/unmappableForLength(-1) → IAE（与 0 同）。
4. **单例交叉验证**: decode(空输入)返回 == CoderResult.UNDERFLOW；decode/encode(1 容量小输出)
   返回 == CoderResult.OVERFLOW——常量对象与编码器机制共用同一实例。
5. **异常消息**: malformedForLength(5).throwException() → MalformedInputException
   message="Input length = 5"、getInputLength()==5；unmappable(6) 同构；同一结果对象可重复
   throwException。
6. **白盒**: 所有声明构造器非 public；JDK-8 侧类**非 final**（两构造器: private+包私有）、
   JDK-27 侧 final 且单一 private 构造器——版本差异双侧各自断言。
