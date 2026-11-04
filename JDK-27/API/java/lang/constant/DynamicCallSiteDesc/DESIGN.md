# java.lang.constant.DynamicCallSiteDesc — 设计方案

## 1. API 功能
invokedynamic 调用点描述（final）：`of(bsm,name,type,args...)`/`of(bsm,type)`；`withArgs`/`withNameAndType`；`invocationName/invocationType/bootstrapMethod/bootstrapArgs`；`resolveCallSiteDesc(Lookup)` 实际执行 bootstrap 得 CallSite。

## 2. Java 用户如何使用
```java
DynamicCallSiteDesc d = DynamicCallSiteDesc.of(bsmDesc, "target", mtd);
CallSite cs = d.resolveCallSiteDesc(MethodHandles.lookup());
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/constant/DynamicCallSiteDesc.java`。
- javac 为字符串拼接/lambda 生成 invokedynamic 即此结构（StringConcatFactory.makeConcatWithConstants）。
- JDK `test/jdk/java/lang/constant/DynamicCallSiteDescTest.java`。

## 4. 测试用例设计 + 自动化实现
白盒：final、无 public 构造器、11 方法、静态 of。
黑盒：of 全访问器（name/type/bsm/args 值与长度）；of(bsm,type) 默认名 "_"；withArgs 整体替换参数且保留 name/bsm；withNameAndType；按值 equals/hashCode；toString 含 nameBs/target；真实解析：测试类内 bootstrap 返回 ConstantCallSite，resolveCallSiteDesc 得到类型匹配且 invokeExact("abcd")==4 的 CallSite。
对应 `DynamicCallSiteDescTest.java` 的 `testBlackBox()`。
