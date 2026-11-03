# ValueLayout.OfInt — 设计方案

## 1. API 功能
`java.lang.foreign.ValueLayout.OfInt`（接口，继承 `ValueLayout`）是 `int` 载体（carrier）的值布局，size=4、alignment=4。常量 `ValueLayout.JAVA_INT` 即实例；`withName`/`withoutName`/`withByteAlignment`/`withOrder` 协变返回 `OfInt`。

## 2. Java 用户如何使用
```java
ValueLayout.OfInt l = ValueLayout.JAVA_INT;
int v = seg.get(l, 0);
seg.set(l, 0, 1234567);
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/foreign/TestLayouts` 验证各 primitive 布局的 size/alignment/carrier/order。
- jextract 用 `ValueLayout.JAVA_INT` 描述 C 对应标量类型。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 继承 `ValueLayout` + 四个协变方法返回 `OfInt`。
黑盒：常量 byteSize/byteAlignment/carrier/默认 order/withOrder/withName/withoutName/withByteAlignment；在段上 set/get、setAtIndex/getAtIndex、varHandle set/get 往返。
对应 `OfIntTest.java` 白盒 + 黑盒部分。
