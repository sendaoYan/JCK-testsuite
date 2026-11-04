# java.lang.invoke.MethodHandleInfo — 设计方案

## 1. API 功能
方法句柄符号化视图（interface）：`REF_*` 9 个常量（REF_getField=1..REF_invokeInterface=9）、`referenceKind/referenceName/declaringClass/methodType/modifiers`、`referenceKindToString/reflectAs`（静态）、`toString`。

## 2. Java 用户如何使用
```java
MethodHandleInfo info = MethodHandles.lookup().revealDirect(mh);
if (info.referenceKind() == MethodHandleInfo.REF_invokeStatic) ...
Method mm = (Method) info.reflectAs(Method.class, MethodHandles.lookup());
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/invoke/MethodHandleInfoTest.java`。
- MethodHandleProxies.wrapperInstanceTarget 等内部用 revealDirect 拿符号信息；调试/审计工具用它打印调用目标。

## 4. 测试用例设计 + 自动化实现
白盒：接口、REF_ 常量 1..9、方法齐全。
黑盒：referenceKindToString 9 种名（getField..invokeInterface）；revealDirect(findStatic/findVirtual) 的 kind/name/declaringClass/methodType/isVarArgsPackagePrivate；getModifiers 静态包私有形态；reflectAs 得到可 invoke 的 Method（21→42）；toString 格式 "REF_invokeStatic..."。
对应 `MethodHandleInfoTest.java` 的 `testBlackBox()`。
## 深化批次（2026-11-05）

新增：扩展 kind 矩阵（findSpecial/findGetter/findSetter/findStaticGetter/findStaticSetter/接口 findVirtual=9/unreflectConstructor="<init>"/unreflectGetter/Setter/varargs 0x88 位）、reflectAs Method/Field/Constructor 恒等还原+错型 CCE、info.toString()==四参格式化器、revealDirect 六种组合句柄 IAE "not a direct method handle"+同型 asType 仍 direct、publicLookup 访问负例（package-private IAE/nestmate 私有 IAE）。（21→91 断言）
