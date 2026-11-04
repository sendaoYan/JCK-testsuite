# java.lang.invoke.SerializedLambda（JDK-8 侧）— 设计方案

## 1. API 功能
可序列化 lambda 的传输形态（final、implements Serializable、**public 10 参构造**，两侧皆有）：
- 构造 `(Class capturingClass, String functionalInterfaceClass/Name/Signature, int implMethodKind, String implClass/Name/Signature, String instantiatedMethodType, Object[] capturedArgs)`。
- 读取器：`getCapturingClass/getFunctionalInterfaceClass/getFunctionalInterfaceMethodName/getFunctionalInterfaceMethodSignature/getImplClass/getImplMethodName/getImplMethodSignature/getImplMethodKind(REF_* 精确)/getInstantiatedMethodType(final)/getCapturedArgCount/getCapturedArg(i)`。
- lambda 类仅在函数式接口（或 marker）可序列化时才生成 `writeReplace`；`SerializedLambda.readResolve` 反向调用捕获类的 `$deserializeLambda$`。

## 2. Java 用户如何使用
```java
Method wr = f.getClass().getDeclaredMethod("writeReplace");
wr.setAccessible(true);
SerializedLambda sl = (SerializedLambda) wr.invoke(f);
sl.getImplMethodName(); sl.getCapturedArg(0);   // 反射捕获，不落盘
```

## 3. 参考来源（只列路径）
- Oracle javadoc：`java.lang.invoke.SerializedLambda`
- OpenJDK 源码：`src/java.base/share/classes/java/lang/invoke/SerializedLambda.java`
- OpenJDK 回归：`~/git/jdk/test/jdk/java/lang/invoke/lambda/`（序列化用例划分，代码自写）
- JCK runtime-8b：`~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_lang/invoke`

## 4. 测试用例设计（`SerializedLambdaTest.java`）

| 优先级 | 测试点 | 方法 |
| --- | --- | --- |
| P0 | public 构造 10 参形状（Class,String,String,String,int,String,String,String,String,Object[]）；getInstantiatedMethodType 为 final；getter 返回类型 | testSerializedLambda / testStructureDeep |
| P0 | javac 静态零捕获 lambda（`s -> s + "-ok"`，自造 SF extends Serializable）：writeReplace 反射捕获；fic 二进制名 `SerializedLambdaTest$SF`；kind=REF_invokeStatic(6)；implMethodName 以 lambda$ 开头；fiSig/inst/implSig 精确描述符；captured=0；toString 含 numCaptured=0 | testWriteReplaceStatic |
| P0 | 捕获 lambda：`final int k = Integer.parseInt("7")`（非常量表达式，强制真捕获）→ captured=1、arg0=7、implSig="(I)Ljava/lang/String;"、inst="()Ljava/lang/String;" | testWriteReplaceCaptured |
| P1 | 非 Serializable 接口（Supplier）的 lambda 无 writeReplace → NoSuchMethodException | testWriteReplaceMissing |
| P1 | getCapturedArg 越界/负下标 → ArrayIndexOutOfBoundsException（消息两侧不同，仅断类） | testCapturedArgBounds |
| P0 | javac lambda 真序列化 round-trip（BAOS 内存往返不落盘）：反序列化产物 instanceof 原接口且计算精确 | testJavacRoundTrip |
| P1 | altMetafactory FLAG_SERIALIZABLE 工厂 lambda：writeReplace 捕获（implMethodName=="decorate"、kind=6、inst/implSig 描述符、captured=0）；lambda 对象与 SerializedLambda 对象 readObject 均失败（JDK-8 观察值 IOException——类无匹配 $deserializeLambda$ 映射） | testFactoryLambda |

### 易错规范细节
- `final int k = 7` 是常量变量，编译器直接常量折叠、**不捕获**；必须用 `Integer.parseInt("7")` 之类运行期值。
- 嵌套接口二进制名用 `$`（`SerializedLambdaTest$SF`），`getImplClass`/`getFunctionalInterfaceClass` 在默认包下点斜杠形式相同，带包时 `getImplClass` 为斜杠形。
- 工厂（altMetafactory 手链）lambda 无法完整反序列化：javac 只为自己编译的 lambda 生成 `$deserializeLambda$` 映射；JDK-8 上 readObject 抛 `IOException("unexpected exception type")`，JDK-9+ 抛 `InvalidObjectException`（分侧断言）。
- `getInstantiatedMethodType` 是 final 方法（javap 佐证），子类不可覆写。
