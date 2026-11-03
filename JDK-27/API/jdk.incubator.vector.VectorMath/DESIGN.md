# jdk.incubator.vector.VectorMath 设计案

## 1. API 功能
标量饱和/无符号算术工具（`final class VectorMath`，全静态方法）。分组：饱和加减（`addSaturating/subSaturating`，int/long/byte/short 重载）、无符号比较（`minUnsigned/maxUnsigned`，int/long）。

## 2. Java 用户如何使用
`int c = VectorMath.addSaturating(Integer.MAX_VALUE, 1);` 饱和加在溢出时钳位到最大值；`long m = VectorMath.maxUnsigned(0L, -1L);` 把参数当无符号数比较。链路：饱和加减边界 → 无符号 min/max。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/jdk.incubator.vector/share/classes/jdk/incubator/vector/VectorMath.java`。
- 来源：JDK 源码中向量实现内部调用这些饱和/无符号语义（`java.lang` 的 `Math.addExact` 之外的无符号/饱和补充）。

## 4. 测试用例设计 + 自动化实现
白盒：public/final/static/关键方法。黑盒：断言溢出钳位（`MAX+1==MAX`、`MIN-1==MIN`）与正常路径、`byte` 钳位、无符号 min/max（`0` 与 `-1` 比较），全部精确确定。
