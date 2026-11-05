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

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- 系统引用：`location()=="jrt:/java.base"` 精确、toString "[module java.base, location=jrt:/java.base]"（系统实现的覆写）、`open()` 每次新 reader、open() 声明 IOException、与 boot 配置中 ResolvedModule.reference() 的 descriptor/location 一致。
- 自定义引用：identity equals/hashCode（不覆写）、toString 回落 Object 形（含 @）、null location → empty Optional、descriptor/location 为 final 访问器（反射断言）、protected (ModuleDescriptor, URI) 构造器精确签名。
- 抽象面：open() 保持 abstract、非 Serializable、包名 java.lang.module。

### 2. 使用方式
- CustomRef 子类继续承担 open() 的自定义行为验证。

### 3. 参考来源
- Oracle javadoc ModuleReference；实测系统引用的 toString 覆写行为。

### 4. 测试用例设计 + 易错规范细节
- 断言数 20→61。
- 易错点：
  - 抽象类 ModuleReference 本身不覆写 toString —— 精确格式只属于系统实现（子系统类），自定义子类得到 Object 形式；
  - equals 是身份语义：同 descriptor 同 location 的两个实例不相等；
  - `open()` 每次返回新 reader（系统与自定义均断言）。
