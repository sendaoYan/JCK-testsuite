# java.nio.ByteOrder 测试方案

## 1. API 功能
- 常量: BIG_ENDIAN/LITTLE_ENDIAN (JDK-8 为 final class 非 enum); nativeOrder()/toString()

## 2. Java 用户如何使用
- 指定 ByteBuffer.order(ByteOrder) 后以 putInt/getInt 写读多字节; 切换 order 改变对同一字节序列的解释

## 3. 开源代码实际用法调研
- jdk8u-dev ByteOrder: final class + private name 字段, 无 public 构造器; 常量为单例对象

## 4. 测试用例设计 + 自动化实现
- 白盒: final/extends Object/无 public 构造/nativeOrder+toString 存在; 黑盒: toString、两常量互异、nativeOrder 是两常量之一且稳定、小端/大端 putInt/getInt 字节布局与重解释、默认大端。实现于 ByteOrderTest.java

健壮性: 无墙钟/随机/哈希顺序/全局状态/文件/网络/线程依赖 (MappedByteBuffer 除外, 用临时文件
并在 finally 清理); 字节序断言显式 order(...) 固定布局不依赖平台 nativeOrder; 中文/emoji 用
Unicode 字面量固定; 固定输入跨平台一致.


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
