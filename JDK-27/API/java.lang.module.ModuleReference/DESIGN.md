# java.lang.module.ModuleReference — 设计方案

## 1. API 功能
模块引用（abstract）：protected 构造器 `(ModuleDescriptor, URI)`；`descriptor()`/`location()`（final，null location → Optional.empty()）；抽象 `open()`→ModuleReader。

## 2. Java 用户如何使用
```java
class MyRef extends ModuleReference {
    MyRef(ModuleDescriptor d, URI loc) { super(d, loc); }
    public ModuleReader open() { return new MyReader(...); }
}
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/module/ModuleReference.java`。
- jdk.internal.module.ModuleReferenceImpl（系统/路径模块实现），ModuleFinder 返回的都是其子类。
- JDK `test/jdk/java/lang/module/ModuleFinderTest.java`。

## 4. 测试用例设计 + 自动化实现
白盒：abstract、protected 构造器、descriptor/location final、open 抽象。
黑盒：系统 java.base 引用（descriptor.name、jrt location、open 可读 Object.class）；用户子类回读：null location → empty Optional、descriptor 同一性、自定义 open() 行为被尊重；带 location 往返；descriptor()/location() 稳定；toString 为 Object 默认形式。
对应 `ModuleReferenceTest.java` 的 `testBlackBox()`。
