# java.lang.invoke.VarHandle.AccessMode — 设计方案

## 1. API 功能
访问模式枚举（VarHandle 嵌套 final 枚举）：31 个常量（GET/SET/GET_VOLATILE/.../WEAK_COMPARE_AND_SET_ACQUIRE/GET_AND_BITWISE_XOR_RELEASE 等），`methodName()/valueFromMethodName()` 互转、`valueOf`。

## 2. Java 用户如何使用
```java
VarHandle.AccessMode am = VarHandle.AccessMode.valueFromMethodName("compareAndSet");
if (vh.isAccessModeSupported(am)) ...
```

## 3. 开源代码实际用法调研
- JDK `VarHandle.AccessMode` 枚举定义与 `test/jdk/java/lang/invoke/VarHandles/VarHandleTestAccessMode...`。
- AtomicXxx 的 fullFence/release 序原语按 AccessMode 选择内部路径。

## 4. 测试用例设计 + 自动化实现
白盒：枚举、31 常量、values 长度。
黑盒：methodName↔valueFromMethodName 全量往返；bogus 方法名→IAE、null→NPE；valueOf 往返与 ordinal 稳定；accessModeType(GET)==([II)I、(SET) 返回 void；不支持的原子模式 isAccessModeSupported==false（只读数组视图等）。
对应 `AccessModeTest.java` 的 `testBlackBox()`。