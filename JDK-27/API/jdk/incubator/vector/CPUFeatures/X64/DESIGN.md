# jdk.incubator.vector.CPUFeatures.X64（CPU 特性反射） 设计案

## 1. API 功能
枚举 JVM 所在 x64 硬件支持的向量 ISA 扩展：恰 4 个字段 SUPPORTS_AVX/SUPPORTS_AVX2/SUPPORTS_AVX512F/SUPPORTS_AVX512DQ，public static boolean（刻意非 final），外类 CPUFeatures 包私有。

## 2. Java 用户如何使用
读取静态布尔判断是否可用 AVX-512 代码路径；非 x64 平台类初始化会失败。

## 3. 开源代码实际用法调研
来源：JDK-27 src.zip CPUFeatures.java；VectorSupport.getCPUFeatures() 下发逗号分隔特性串，X64 静态块 requires(isX64())。

## 4. 测试用例设计 + 易错点
反射断言字段名集合恰为 4 个、类型/修饰符（非 final 是契约一部分）、重复读取一致、外类非 public、嵌套类 public static。易错：勿断言具体 true/false（硬件相关）。

