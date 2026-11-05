# java.lang.runtime.ObjectMethods — 设计方案

## 1. API 功能
final 引导类（编译器为 record 生成 equals/hashCode/toString 时调用）：
- 唯一 public 方法 `bootstrap(Lookup, String name, TypeDescriptor type, Class<?> recordClass, String names, MethodHandle... getters)`
- name ∈ {equals, hashCode, toString}；type 为 MethodType 时返回 `ConstantCallSite`，为 `MethodHandle.class` 时返回裸 MethodHandle
- names 是 `;` 分隔的组件名列表，getters 为对应字段访问器

## 2. Java 用户如何使用
正常路径由 javac 生成 invokedynamic；也可显式调用：
```java
CallSite cs = (CallSite) ObjectMethods.bootstrap(lookup, "equals",
        MethodType.methodType(boolean.class, Pair.class, Object.class),
        Pair.class, "a;b", getterA, getterB);
boolean eq = (boolean) cs.dynamicInvoker().invokeExact(p, (Object) q);
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/runtime/ObjectMethods.java`：`makeEquals` 走 `CLASS_IS_INSTANCE + Objects.equals/eq`，`makeHashCode` 用 hashCombiner，`makeToString` 走 StringConcatFactory。
- JDK `test/jdk/java/lang/runtime/ObjectMethodsTest.java`：C/Empty 两个 fixture 的三种方法、`MethodHandle.class` 描述符、异常路径（错误 name/类型/名字不匹配）。
- record 编译产物（javap 任意 record）可见 `invokedynamic ObjectMethods.bootstrap`。

## 4. 测试用例设计 + 自动化实现
白盒：final、无 public 构造器、唯一 public static bootstrap、返回 Object。
黑盒（Pair fixture + findGetter 访问器 + 真 record Point 对照）：
- equals：自等/同值/异值/null/异类型全路径；
- hashCode 与 `Objects.hash(组件)` 一致（含 0/负数）；
- toString 精确格式 `Pair[a=1, b=2]`；
- 真 record 的编译器生成行为与显式引导一致；
- `MethodHandle.class` 描述符返回裸句柄可 invokeExact；
- 非法 name/类型/名字数不匹配抛 IAE、null lookup 抛 NPE；单组件 record 行为。
对应 `ObjectMethodsTest.java` 的 `testBlackBox()`。

## 5. 契约深化（本批）
- null 组件 record：equals 同值成立、hashCode 一致、toString 渲染 "tag=null"。
- 零组件 bootstrap：hashCode == 0、toString == "Bare[]"、equals 退化为 instanceof 判定（含 null false / 异类型 false）。
- null receiver：bootstrapped equals/hashCode/toString 句柄以 null 接收者调用均抛 NPE。
- 每次 bootstrap 返回新 CallSite（无跨调用缓存）；hash 组合器 x*31+y（混合 int/String 组件验证）。
- 工程坑（本批实证）：invokeExact 必须处于 cast 上下文——语句位置会被 javac 定型为 void 触发 WrongMethodTypeException，掩盖真正的 NPE。

## 契约深化（2026-11，批次 lcL）

### 1. API 功能（本轮新增维度）
- 组件类型组合子全表（record 自动 hashCode/toString/equals 的 x*31+y 组合）：boolean→Boolean.hashCode(true)=1231、long→Long.hashCode、double→Double.hashCode、float→Float.hashCode；三组件 31*(31*(31*0+a)+b.h)+c.h 公式锁定。
- 零组件 record：toString "EmptyRec[]"、hashCode==0、equals 同型 true、equals(null) false。
- **数组组件身份比较 gotcha（JLS 8.10.3）**：ArrRec(new int[]{1,2,3}) 不 equals 同内容实例、equals 同一数组实例 true、hashCode 身份基稳定、toString 渲染数组身份（[I@hex）。
- record 结构面：record 类 final、**extends java.lang.Record（非 Object——Record 再 extends Object）**、实例 instanceof java.lang.Record、getRecordComponents() 保声明序且类型精确、访问器返回构造实参、equals 对称、相等 record hashCode/toString 相等且各自稳定。

### 2. 使用方式
- 自造 7 个 fixture record（BoolRec/LongRec/DoubleRec/FloatRec/EmptyRec/Three/ArrRec）全部断组合子精确值。

### 3. 参考来源
- JLS 8.10.3（record 成员：hashCode 对每个组件按对应包装类 hashCode 以 31 组合；equals 对引用组件用该组件 equals——数组即身份比较）。
- jdk-27.0.1 实测：数组组件 equals 实测为身份比较（著名坑，锁定）。

### 4. 测试用例设计 + 易错规范细节
- testDeepComponentTypeMatrix（18）、testDeepArrayComponentContract（4）、testDeepRecordStructure（16）。
- 易错点：① record 的 superclass 是 java.lang.Record 而非 Object（首版即写错后实测纠正）；② 数组组件 equals 断言必须成对（同内容 false + 同实例 true）才构成完整契约；③ 混合类型组合子公式注意组件声明顺序。
