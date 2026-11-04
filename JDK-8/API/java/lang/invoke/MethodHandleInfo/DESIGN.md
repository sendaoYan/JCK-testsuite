# java.lang.invoke.MethodHandleInfo — 设计方案

## 1. API 功能
direct method handle 的符号信息视图：`MethodHandles.Lookup.revealDirect(mh)` 产出；`getReferenceKind()`（JVMS 5.4.3.5 九种 REF_*，常量值 1..9）、`getDeclaringClass()/getName()/getMethodType()/getModifiers()/isVarArgs()`；`reflectAs(Member.class, lookup)` 还原 Method/Field/Constructor（带访问检查）；静态 `referenceKindToString(int)` 与 `toString(kind, cls, name, type)` 格式化。组合句柄（asType 有实质转换/filterArguments/dropArguments/constant/bindTo/asCollector）不可 reveal（IAE "not a direct method handle"）。

## 2. Java 用户如何使用
```java
MethodHandle mh = lookup.findStatic(Foo.class, "bar", MethodType.methodType(int.class));
MethodHandleInfo info = lookup.revealDirect(mh);
assert info.getReferenceKind() == MethodHandleInfo.REF_invokeStatic;
Method m = info.reflectAs(Method.class, lookup); // 与原 Method equals
```

### 参考来源
- Oracle javadoc（8）：https://docs.oracle.com/javase/8/docs/api/java/lang/invoke/MethodHandleInfo.html
- Oracle javadoc（27）：https://docs.oracle.com/en/java/javase/27/docs/api/java.lang.invoke/java/lang/invoke/MethodHandleInfo.html
- OpenJDK 8u 源码：/data/yansendao/git/jdk8u-dev/jdk/src/share/classes/java/lang/invoke/MethodHandleInfo.java
- OpenJDK 27 源码：/data/yansendao/git/jdk/src/java.base/share/classes/java/lang/invoke/MethodHandleInfo.java
- JCK runtime-8b：/data/yansendao/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_lang/invoke/MethodHandles
（只列路径，不拷内容；JCK 仅作覆盖面对照，代码全部自写。）

## 4. 测试用例设计（含易错规范细节）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 九个 REF_* 常量精确值 1..9 | testReferenceKindConstants |
| P0 | 工厂矩阵 kind/declaring/name/type/modifiers：findStatic=6、findVirtual=5、findSpecial=7（declaring=owner）、findGetter=1、findSetter=3、findStaticGetter=2、findStaticSetter=4、接口 findVirtual=9（declaring=接口本身，mod 含 ABSTRACT 位）、unreflectConstructor=8（name="<init>"，type=(int)void）、unreflectGetter/Setter=1/3、varargs（mod 含 0x80 位） | testKindMatrix |
| P0 | reflectAs 还原 Method/Field/Constructor 与原对象 equals 且可调用；错型 reflectAs → CCE "Cannot cast" | testReflectAsRoundTrip |
| P1 | info.toString() 与四参静态格式化器全等；构造器串 "newInvokeSpecial X.<init>:(int)void" | testToStringRoundTrip |
| P1 | referenceKindToString 1..9 全表；0/10 → IAE "invalid reference kind" | testReferenceKindToStringTable |
| P1 | revealDirect 组合句柄 → IAE "not a direct method handle"（六种组合子）；asType 同型 no-op 仍是 direct | testRevealDirectNegatives |
| P1 | reflectAs 访问检查：publicLookup 对 package-private 成员 → IAE "member is private to package"（8/27 一致）；27 侧私有字段（nestmate）→ IAE "member is private" | testReflectAsAccessFailures |

易错点：
- JDK-8 无 nestmates：外部类 lookup 直接 findGetter 私有字段会 IllegalAccess，私有成员负例要用 setAccessible 后的 unreflect（8 侧因此改用 package-private 字段做访问负例）。
- toString 用 Class.getName（嵌套类带 $）+ MethodType 简单名（String 而非 java.lang.String）。
