# java.util.Spliterators 测试设计

## 1. API 功能
- 数组/基本类型数组：`spliterator(Object[]/int[]/long[]/double[], ...)`。
- 集合/迭代器：`spliterator(Collection, int)`、`spliterator(Iterator, size, int)`、`spliteratorUnknownSize(Iterator, int)`。
- 空：`emptySpliterator`、`emptyInt/Long/DoubleSpliterator`。
- 转换：`iterator(Spliterator)`。

## 2. Java 用户如何使用
- 自定义源转 spliterator：`Spliterators.spliterator(array, 0)`。
- 未知大小迭代器：`Spliterators.spliteratorUnknownSize(it, characteristics)`。
- 空流：`Spliterators.emptySpliterator()`。
- 反向转换：`Spliterators.iterator(spliterator)`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/util/Arrays.java`：数组 spliterator 委托 `Spliterators.spliterator`。
- JDK 源码 `java/util/ArrayList.java`：`spliterator()` 使用 `Spliterators`。
- JDK 源码 `java/util/stream/StreamSupport.java`：把 spliterator 转流。
- 广泛用法：自定义集合/数据源接入流框架。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言 final 类、关键静态方法存在。
- 黑盒：
  - 数组 spliterator 大小与遍历、空 spliterator。
  - 集合 spliterator 大小与特征。
  - unknownSize 的 estimateSize==Long.MAX_VALUE 与遍历。
  - `iterator(spliterator)` 转换与耗尽。
