# Character.Subset — 设计方案

## 1. API 功能
`java.lang.Character.Subset`（public 非.final，唯一 protected 构造器 `Subset(String name)`）表示字符集的子集；`equals/hashCode/toString` 均 final，其中 `equals`/`hashCode` 沿用 Object 身份语义，`toString` 返回构造时传入的名字。子类代表：`Character.UnicodeBlock`。

## 2. Java 用户如何使用
- 用户一般不直接使用，而是通过 `Character.UnicodeBlock.of(char)` 拿到的实例（其本身是 Subset）；自定义子集可匿名继承 protected 构造器。
```java
Character.Subset s = new Character.Subset("my") {};
s.toString(); // "my"
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/Character/Subset` 与 ICU4J 等字符集库以 `Subset` 作为 UnicodeBlock 的公共父类型；JDK 内部仅 `UnicodeBlock` 一个公开子类。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.Character$Subset")` 验证 public、非 final、唯一 protected 构造器、`equals/hashCode/toString` 为 final、私有 `name` 字段。
黑盒：匿名子类 `toString` 返回名字、equals 身份语义（两个匿名子类互不相等）、hashCode 稳定、`UnicodeBlock.BASIC_LATIN instanceof Subset`。
对应 `CharacterSubsetTest.java` 白盒 + 黑盒部分。
