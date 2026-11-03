# java.lang.invoke.VarHandle — 设计方案

## 1. API 功能
变量句柄（abstract 类）：数组元素/实例字段/静态字段访问，访问模式 31 种（get/set/volatile/acquire/release/opaque/compareAndSet/compareAndExchange/getAndSet/getAndAdd/getAndBitwise*/weak*）；`get/toMethodHandle/accessModeType/isAccessModeSupported/describeConstable/coordinateTypes/varType`。

## 2. Java 用户如何使用
```java
VarHandle vh = MethodHandles.arrayElementVarHandle(int[].class);
int old = (int) vh.getAndAdd(arr, 1, 10);  // 原子加
vh.setRelease(arr, 0, 5);
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/invoke/VarHandles/*`（instance/static/array 三大目录，千行级）。
- java.util.concurrent 原子类（AtomicXxx/StampedLock）内部全部改用 VarHandle 实现内存序原语。

## 4. 测试用例设计 + 自动化实现
白盒：abstract、方法存在、accessModeType 返回 MethodType。
黑盒：数组/实例字段/静态字段三路 get/set/volatile/acquire/release/opaque；compareAndSet 成败两态；compareAndExchange 返回旧值；getAndSet/Add/BitwiseOr 返回旧值；weak CAS；toMethodHandle(GET/SET) 实调；describeConstable 解析可写；coordinateTypes 精确；错坐标类型抛 IAE/CCE。
对应 `VarHandleTest.java` 的 `testBlackBox()`。