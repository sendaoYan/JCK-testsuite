# AddressLayout — 设计方案

## 1. API 功能
`java.lang.foreign.AddressLayout`（接口，继承 `ValueLayout`）描述机器地址/指针布局。核心：`withTargetLayout(MemoryLayout)`/`withoutTargetLayout()`/`targetLayout()` 绑定/解绑被指类型；继承 `order()/withOrder/carrier()/withName/withoutName/withByteAlignment` 并协变返回 `AddressLayout`。

## 2. Java 用户如何使用
常量 `ValueLayout.ADDRESS`/`ADDRESS_UNALIGNED` 即地址布局；用户典型地 `ValueLayout.ADDRESS.withTargetLayout(JAVA_INT)` 描述「指向 int 的指针」，再通过 `MemorySegment.get(AddressLayout, offset)` 读指针。

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/foreign` 中 `TestUpcall`/`TestDowncall` 大量用 `ValueLayout.ADDRESS` 及 `withTargetLayout` 描述指针参数。
- 项目源码 `~/git/jdk25u-dev` 中 `StructLayoutImpl`/`ValueLayouts` 对 ADDRESS carrier 为 `MemorySegment`、size 为指针宽度的实现。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 继承 `ValueLayout` + 各方法协变返回 `AddressLayout`、`targetLayout` 返回 `Optional`。
黑盒：`ADDRESS` 的 byteSize 为 4/8、alignment==size、order==native、carrier==MemorySegment；`withTargetLayout`/`withoutTargetLayout` 往返；`withOrder`/`withName`/`withByteAlignment` 往返；equal 往返。
对应 `AddressLayoutTest.java` 白盒 + 黑盒部分。
