# ValueLayout.OfDouble — 设计方案

## 1. API 功能
`java.lang.foreign.ValueLayout.OfDouble`（接口，继承 `ValueLayout`）是 `double` 载体（carrier）的值布局，size=8、alignment=8。常量 `ValueLayout.JAVA_DOUBLE` 即实例；`withName`/`withoutName`/`withByteAlignment`/`withOrder` 协变返回 `OfDouble`。

## 2. Java 用户如何使用
```java
ValueLayout.OfDouble l = ValueLayout.JAVA_DOUBLE;
double v = seg.get(l, 0);
seg.set(l, 0, 2.5);
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/foreign/TestLayouts` 验证各 primitive 布局的 size/alignment/carrier/order。
- jextract 用 `ValueLayout.JAVA_DOUBLE` 描述 C 对应标量类型。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 继承 `ValueLayout` + 四个协变方法返回 `OfDouble`。
黑盒：常量 byteSize/byteAlignment/carrier/默认 order/withOrder/withName/withoutName/withByteAlignment；在段上 set/get、setAtIndex/getAtIndex、varHandle set/get 往返。
对应 `OfDoubleTest.java` 白盒 + 黑盒部分。
