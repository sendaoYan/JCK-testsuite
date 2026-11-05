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

## 契约深化（2026-11，批次 lcL）

### 1. API 功能（本轮新增维度）
- **黑盒语言闭环**：typeSwitch/enumSwitch 所驱动的语言构造本身——pattern switch（String/Integer/Character 绑定 + null case（JDK 21+）+ when 守卫真/假分支 + default）与 enum switch 三常量全分支命中，输出逐字断言。
- **null 标签拒绝**：typeSwitch 标签数组含 null 元素 → bootstrap 时 IllegalArgumentException，精确消息 "null label found"（jdk-27.0.1 实测）。
- 超类型标签次序：{CharSequence, String} 时 String 命中标签 0（子类型+首中即止）、StringBuilder 命中 CharSequence、非 CharSequence 落 default；{String, CharSequence} 反序时 StringBuilder 落 1——次序敏感锁定。
- 重复类标签：首个获胜；无匹配 → 标签数（default 下标）。
- **enumSwitch 非法标签类型**：Integer 标签 → IllegalArgumentException 精确消息 "label with illegal type found: class java.lang.Integer, expected label of type either String or Class"（实测逐字锁定）。
- enum 未知名标签：所有常量落 default；重复名：首个获胜；按名匹配与 ordinal 无关（{"BLUE","GREEN","RED"} 中 RED 落 2）。
- 常量标签族：Boolean.class 标签按类型命中；**非 Class 常量按 equals 比较**（标签 5/7 对选择器 Integer(5)/(7)/(9) → 0/1/2）；null 选择器对常量标签仍 -1。

### 2. 使用方式
- 语言级 switch 表达式 + 手工 bootstrap（LOOKUP + MethodType(int,Object,int) / (int,Color,int)）双轨。

### 3. 参考来源
- JLS 14.11.2 / 14.11.3（switch 模式匹配语义：null case、类型标签首中即止、default 下标）；JEP 440/441。
- jdk-27.0.1 实测：null label 拒绝消息、非法标签类型消息、常量 equals 匹配、Boolean 标签。

### 4. 测试用例设计 + 易错规范细节
- testDeepLanguageSwitchClosure（11）、testDeepNullLabelRejected（1+守卫）、testDeepSupertypeLabelOrdering（5）、testDeepDuplicateClassLabels（2）、testDeepEnumIllegalLabelType（1+守卫）、testDeepEnumUnknownAndDuplicateNames（5）、testDeepConstantLabels（7）。
- 易错点：① null 作为 vararg 元素必须显式 new Object[]{null, ...}（直接传 null 会把整个数组置 null，是另一个用例）；② 标签命中是"首个匹配"而非"最精确匹配"；③ enumSwitch 标签必须是 String（或 Class），其它类型 bootstrap 即拒。
