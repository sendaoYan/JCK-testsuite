# java.lang.invoke.ConstantBootstraps — 设计方案

## 1. API 功能
condy 通用引导（final 工具类）：`enumConstant`、`getStaticFinal`（3 参：声明类由字段类型推导、基本型映射包装类；4 参：显式声明类，字段须 final）、`nullConstant`（限引用类型）、`fieldVarHandle/staticFieldVarHandle/arrayVarHandle`、`explicitCast`（null 直通、失配 CCE）。

## 2. Java 用户如何使用
```java
Object max = ConstantBootstraps.getStaticFinal(lk, "MAX_VALUE", int.class);      // 2147483647
Object e   = ConstantBootstraps.enumConstant(lk, "SOURCE", RetentionPolicy.class);
VarHandle vh = ConstantBootstraps.arrayVarHandle(lk, "a", VarHandle.class, int[].class);
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/invoke/ConstantBootstrapsTest.java`。
- javac 把 `static final` primitive/String ldc 编译为 4 参 getStaticFinal condy；Enum.$VALUES 与枚举常量经 enumConstant 引导。

## 4. 测试用例设计 + 自动化实现
白盒：final、无私有构造、7 方法全静态公有。
黑盒：enumConstant 解析 SOURCE/RUNTIME、未知常量→IAE；nullConstant 引用类型为 null、基本型→IAE；getStaticFinal 4 参取 Integer.MAX_VALUE 与本类 final 字段、3 参推导 Integer 包装与 RetentionPolicy.SOURCE 自类型常量、缺席→NoSuchFieldError；fieldVarHandle 读写字段（7→12）；staticFieldVarHandle（40→41）；arrayVarHandle 数组读写；explicitCast 同型/超型/null 直通、"str"→Integer 抛 CCE。
对应 `ConstantBootstrapsTest.java` 的 `testBlackBox()`。