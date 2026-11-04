# java.nio.ByteOrder 测试方案

## 1. API 功能
- 常量: `BIG_ENDIAN`, `LITTLE_ENDIAN`
- 枚举方法: `values()`, `valueOf(String)`, `toString()`, `ordinal()`, `name()`
- 本机字节序: `nativeOrder()`

## 2. Java 用户如何使用
- 设置缓冲区字节序: `ByteBuffer.allocate(n).order(ByteOrder.LITTLE_ENDIAN)`.
- 组合链路: `order(LITTLE_ENDIAN) -> putInt -> flip -> 逐字节验证布局 -> order(BIG_ENDIAN) -> getInt`.
- 查询本机字节序: `ByteOrder.nativeOrder()`.

## 3. 开源代码实际用法调研
- `test/jdk/java/nio/channels/DatagramChannel/EmptyBuffer.java`: `bb.order(ByteOrder.BIG_ENDIAN); bb.putInt(1).putLong(1);`
- `test/jdk/java/nio/channels/DatagramChannel/Sender.java`: 同上 `order(ByteOrder.BIG_ENDIAN)`.
- `test/jdk/java/nio/Buffer/OrderShort.java`: 校验 duplicate/slice/asReadOnlyBuffer 继承字节序.

## 4. 测试用例设计 + 自动化实现
推导: 枚举表面 (常量/values/valueOf/toString)、nativeOrder 为两常量之一、字节序对
ByteBuffer 多字节读写布局的影响 (显式 order 固定布局)。实现于 `ByteOrderTest.java` 的
`testBlackBox()`; 结构断言在 `testWhiteBox()`.

健壮性: 字节布局用显式 order 固定, 不依赖平台 nativeOrder 的具体取值; valueOf 异常用精确
类型 (IllegalArgumentException/NullPointerException); 无全局状态/文件/网络/线程依赖.


### A1 批次深化（追加）
- 端序契约: nativeOrder()==LITTLE_ENDIAN(x86_64 注释)、allocate/allocateDirect/wrap 默认均
  BIG_ENDIAN、order 切换只改解释不改字节(long 精确布局 0x0102030405060708L)、类型视图
  (asCharBuffer 等)继承创建 byte buffer 的显式 order 而非本机序、native 布局 vs BE 布局逐字节对比、
  equals(null)/跨类型 false、实现稳定行为: duplicate/slice/asReadOnlyBuffer 将 order 重置为
  BIG_ENDIAN(双侧双存储实测, javadoc 仅对类型视图承诺 order 继承)。
- 版本差异: JDK-8 ByteOrder 为 final 类(非枚举, 无 values()/valueOf(String), 恰好 2 个 public
  常量, 反射逐一断言); JDK-9+ 为枚举——values() 防御性拷贝、EnumSet.allOf==2、
  nativeOrder().ordinal()==0(声明序 LITTLE_ENDIAN 在前)。
- 断言数 20 -> 35 (JDK-8) / 35 -> 51 (JDK-27)。
