# MemoryLayout.PathElement — 设计方案

## 1. API 功能
`java.lang.foreign.MemoryLayout.PathElement`（接口）描述布局树中的路径元素。静态工厂 `groupElement(String|long)`、`sequenceElement(long|long,long|())`、`dereferenceElement()`。

## 2. Java 用户如何使用
```java
long off = struct.byteOffset(PathElement.groupElement("field"));
long idx = seq.byteOffset(PathElement.sequenceElement(2));
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/foreign/TestLayoutPaths` 验证各 PathElement 的 byteOffset/varHandle 解析。
- jextract 生成 `PathElement.groupElement("字段名")` 访问结构体成员。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 六静态工厂返回 `PathElement`。
黑盒：各工厂非空；`groupElement(String/long)`、`sequenceElement(long/long,long/())` 在 struct/sequence 上解析出正确 byteOffset。
对应 `PathElementTest.java` 白盒 + 黑盒部分。
