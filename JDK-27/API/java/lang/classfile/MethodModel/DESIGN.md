# java.lang.classfile.MethodModel — 设计案

## 1. API 功能
`java.lang.classfile.MethodModel`（interface，extends `CompoundElement<MethodElement>` + `AttributedElement` + **`ClassElement`**）。实例方法：`flags()`、`parent()`（Optional&lt;ClassModel&gt;）、`methodName()`/`methodType()`（Utf8Entry）、`methodTypeSymbol()`（default，MethodTypeDesc）、`code()`（Optional&lt;CodeModel&gt;：abstract/native 方法为 empty）。

## 2. Java 用户如何使用
`ClassFile.of().parse(bytes).methods()` 按声明序取得；`methodName().stringValue()` 定位方法；`methodType().stringValue()` 是原始描述符；`code()` 取指令体（cast 到 CodeAttribute 可得 maxStack/maxLocals）。方法级属性经 `findAttribute(Attributes.exceptions()/deprecated()/signature()/runtimeVisibleAnnotations())`。

## 3. 参考来源
- JDK 27 javadoc + `javap java.lang.classfile.MethodModel`。
- OpenJDK `test/jdk/jdk/classfile/`（ClassBuildingTest、SignaturesTest）。
- 本机实测：main 描述符、flagsMask 0x0009、method elementList=[AccessFlags, CodeModel]、abstract/native code() empty、genericM Signature 精确串。

## 4. 测试用例设计 + 易错规范细节
- 白盒（原有）+ 黑盒（原有：build→parse name/descriptor/typeSymbol/flags/parent/code）。
- `testSelfMainMethod`（深化）：真类定位 `main` —— methodType=="([Ljava/lang/String;)V"、methodTypeSymbol descriptorString 同、parameterCount 1、param0=="[Ljava/lang/String;"、return=="V"、flags PUBLIC+STATIC（flagsMask==0x0009、location()==METHOD）；`code().get().parent().get()==main`（**身份**，父子循环）；@Deprecated 方法（Deprecated + RuntimeVisibleAnnotations `Ljava/lang/Deprecated;` 双属性）；throws 方法（ExceptionsAttribute 精确表 [java/lang/IllegalStateException, java/lang/InterruptedException]）；泛型方法 Signature 精确串（前缀 `<T:Ljava/lang/Object;>` 后缀 `(TT;Ljava/util/List<Ljava/lang/String;>;)TT;`）；方法 elementList 直方图 [AccessFlags=1, CodeModel=1]；层次 instanceof（CompoundElement/**ClassElement**/AttributedElement）。
- `testMethodShapes`（深化）：abstract/native（code() empty、elementList 仅 [AccessFlags]）、静态带参方法（descriptor/parameterCount）、withMethod 注入 Exceptions 属性回读、每个方法 parent 身份断言、verify 干净。
- `testDefineClassLoop`（深化）：DynMethodModel+seq build add(II)I → defineClass → add(20,22)==42 运行闭环。
- 易错点：**MethodModel 是 ClassElement（成员层面）而非 MethodElement（元素层面）**；`code()` 为 Optional（abstract/native empty，不能直接 get）；methodTypeSymbol 是 default 方法。
