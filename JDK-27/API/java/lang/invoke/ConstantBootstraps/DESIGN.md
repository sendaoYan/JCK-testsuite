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

## 深化批次（2026-11-05）
新增 `testApiSurface/testEnumConstantDeep/testConstantTypes/testGetStaticFinalDeep/testInvokeAndVarHandleDeep`（旧 33 断言 → 新 114）：
- API 面封闭性：public 方法名集合恰为 {nullConstant, primitiveClass, enumConstant, getStaticFinal, invoke, fieldVarHandle, staticFieldVarHandle, arrayVarHandle, explicitCast}（javap 对照；**无 getFieldFinal**）；每个 public static 方法前 3 参为 (Lookup,String,Class) BSM 形状。
- enumConstant：自造 enum 全 4 常量恒等；未知常量/小写名→IAE "No enum constant"；null lookup/name/type→NPE；非枚举类型→IAE "is not an enum class"。
- primitiveClass 契约（任务书假设勘误）：**name 是单字符字段描述符而非类型名**——"I/J/V/Z/B/S/C/F/D" 全 9 项精确返回；"int"/"bytee"/"Ljava/lang/String;"→IAE "not primitive: "；name null→NPE；type 非 Class.class→IAE。
- nullConstant：null lookup/name 被接受（javadoc 声明 unused，实测无 NPE）；type null→NPE；基本型→IAE "not reference: int"。
- getStaticFinal：自造 static final int(4242)/String；错误字段类型与缺席字段均→NoSuchFieldError "does not have member field"；**非 final 字段→IncompatibleClassChangeError "not a final field"**；4 个 null 参→NPE；3 参推导 Long.MIN_VALUE 与自造枚举自类型常量。
- invoke BSM 形态：动态实参执行 "a_1"、结果 widening 到 CharSequence；类型不符→CCE "Cannot cast java.lang.String to java.lang.Integer"。
- fieldVarHandle 缺席字段→NSFE；arrayVarHandle 非数组→IAE "not an array"；explicitCast int/long/Number/null 全矩阵 + "s"→int CCE。