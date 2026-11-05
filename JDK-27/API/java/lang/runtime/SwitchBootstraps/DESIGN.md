# java.lang.runtime.SwitchBootstraps — 设计方案

## 1. API 功能
final 引导类：模式匹配 switch（`case Type t` / `case enum`）的运行时支撑：
- `typeSwitch(Lookup, String, MethodType, Object... labels)`：调用类型 `(selector, int)int`；labels 为 Class（instanceof 语义，先到先得）、String/Integer/Long/Float/Double/Boolean（equals 常量）、EnumDesc；返回匹配下标，null selector 返回 -1，无匹配返回 labels.length
- `enumSwitch(Lookup, String, MethodType, Object... labels)`：labels 为枚举常量名，selector 为枚举类型

## 2. Java 用户如何使用
由 javac 为 `switch (obj) { case String s -> ... }` 生成 invokedynamic；显式调用：
```java
CallSite cs = SwitchBootstraps.typeSwitch(lookup, "",
    MethodType.methodType(int.class, Object.class, int.class), String.class, Integer.class);
int idx = (int) cs.dynamicInvoker().invoke((Object) "hi", 0); // 0
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/runtime/SwitchBootstraps.java`：`typeSwitch` 校验 `(selector,int)int` 形态、labels 元素类型；子类型按 label 顺序匹配。
- JDK `test/jdk/java/lang/runtime/SwitchBootstrapsTest.java`：`testTypes/testPrimitiveType/testEnum` 覆盖 Class/常量/枚举/类型返回/resume 语义（本设计直接对照其调用形态）。
- record deconstruction patterns 在 javac 生成的 booted switch 中复用同一路径。

## 4. 测试用例设计 + 自动化实现
白盒：final、private 构造器、两个 public static 引导方法返回 CallSite。
黑盒：
- typeSwitch（Class labels）：精确命中下标、null→-1、无匹配→default 下标、start index 跳过已过 label；
- 子类型顺序：Integer/Float 均命中超类 label；
- 常量 labels：equals 匹配；
- enumSwitch：常量名命中/缺省/null；
- 非法输入（null labels）在引导期抛 NPE/IAE；resume 语义。
对应 `SwitchBootstrapsTest.java` 的 `testBlackBox()`。

## 5. 契约深化（本批）
- typeSwitch 标签语义：int.class 标签匹配装箱 Integer（装箱匹配原始类标签）；无匹配 → 返回标签数（default 索引）；null selector → -1；Long 不匹配 int.class 标签。
- enumSwitch resume 语义：第二参为起始索引，跳过之前的标签；resume 越过全部标签 → 标签数（default）；标签顺序由常量名数组定义而非 ordinal；未列出常量 → default；null → -1。
- 两类 bootstrap 共享 null==-1 / default==labels.length 常量约定（契约级不变式）。
