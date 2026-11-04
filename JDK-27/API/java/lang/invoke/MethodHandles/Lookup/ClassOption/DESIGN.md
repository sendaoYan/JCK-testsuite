# java.lang.invoke.MethodHandles.Lookup.ClassOption — 设计方案

## 1. API 功能
defineHiddenClass 选项枚举（ Lookup 嵌套枚举）：`NESTMATE`（隐藏类加入巢）、`STRONG`（强引用类对象，防卸载）。

## 2. Java 用户如何使用
```java
MethodHandles.Lookup h = lk.defineHiddenClass(bytes, false,
        MethodHandles.Lookup.ClassOption.NESTMATE);
h.findSpecial(...); // 可访问宿主私有成员
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/invoke/defineHiddenClass/DefineClassWithBadData.java`、`HiddenClass` 系列。
- 动态语言实现（JRuby/Truffle）用 hidden class 生成每调用点形状类，NESTMATE 复用宿主访问权。

## 4. 测试用例设计 + 自动化实现
白盒：枚举、2 个常量、values/valueOf/name。
黑盒：真实 defineHiddenClass 本类嵌套字节码（getResourceAsStream），NESTMATE 下 findSpecial 访问私有 secret()==4711；隐藏类名含 "/0x"；两次 define 产生不同 Class；STRONG 下 initialize=false 后 newInstance 可用。
对应 `ClassOptionTest.java` 的 `testBlackBox()`。