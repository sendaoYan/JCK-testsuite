# ClassDefinition — 设计方案

## 1. API 功能
`java.lang.instrument.ClassDefinition`（final 类）描述一次类重定义的 (Class, 字节码) 对。构造 `ClassDefinition(Class<?>, byte[])`；查询 `getDefinitionClass()`/`getDefinitionClassFile()`。

## 2. Java 用户如何使用
```java
ClassDefinition cd = new ClassDefinition(MyClass.class, newClassBytes);
instrumentation.redefineClasses(cd);
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/instrument/RedefineMethodInBacktrace.java` 等构造 `ClassDefinition` 传入 `redefineClasses`。
- 大量 Java agent 项目用 `ClassDefinition` 包装热替换字节码。

## 4. 测试用例设计 + 自动化实现
白盒：final 具体类 + 继承 Object + 构造器 + getter 返回类型。
黑盒：构造后 getDefinitionClass/getDefinitionClassFile 的类与字节码往返（长度+内容），空字节码场景。
对应 `ClassDefinitionTest.java` 白盒 + 黑盒部分。
