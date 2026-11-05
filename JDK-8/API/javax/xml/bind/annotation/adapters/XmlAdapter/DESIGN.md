# XmlAdapter 契约测试设计

## 1. API 功能

`javax.xml.bind.annotation.adapters.XmlAdapter<ValueType, BoundType>` 是 JAXB 自定义值绑定的抽象基类：`ValueType` 是 XML 侧词法表示（一般是 String），`BoundType` 是 Java 侧表示。JAXB 在 marshal 时调 `marshal(BoundType)->ValueType`、unmarshal 时调 `unmarshal(ValueType)->BoundType`。两个抽象方法都声明 `throws Exception`，允许适配器抛任意受检异常（JAXB 会包装为异常事件）。构造器为 protected，仅供子类使用。

## 2. 使用方式

```java
static class IntAdapter extends XmlAdapter<String, Integer> {
    public Integer unmarshal(String v) { return Integer.valueOf(v.trim()); }
    public String marshal(Integer v)   { return v.toString(); }
}
XmlAdapter<String, Integer> gen = new IntAdapter();
gen.unmarshal("42"); gen.marshal(42);
```

配合 `@XmlJavaTypeAdapter(IntAdapter.class)` 标注字段即可让 JAXB 在读写 XML 时走适配器。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/adapters/XmlAdapter.html
- JSR 222 (JAXB 2.2) 第 8 章适配器
- OpenJDK 8u RI 实测；本仓库测试 `XmlAdapterTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 类契约：abstract、直接继承 Object、非接口/注解/枚举、**protected 单构造器**（反射 getDeclaredConstructors 长度 1 且 protected）。
2. 泛型：恰好两个类型参数，名称 `ValueType`/`BoundType`，均无界。
3. 方法集：恰好 2 个声明方法 `unmarshal`/`marshal`，均 public abstract、均声明 `throws Exception`、均单参数；擦除签名 `(Object)->Object`。
4. 实例化屏障：`XmlAdapter.class.newInstance()` 从外部包调用抛 IllegalAccessException（protected 构造器），非抽象性不是唯一屏障。
5. 子类闭环：IntAdapter 双向解析/打印（含 trim 与 null 直通）、round-trip、经 `XmlAdapter<String,Integer>` 泛型引用的桥接调用、FailingAdapter 的受检异常（IOException）原样穿透 throws Exception 声明。
6. RI 佐证：HexBinaryAdapter instanceof XmlAdapter。

易错规范细节：

- **泛型方向易记反**：`XmlAdapter<ValueType, BoundType>` 第一个是 XML 侧（unmarshal 的参数/marshal 的返回），第二个是 Java 侧。
- 反射 `newInstance()` 对 protected 构造器先撞 IllegalAccessException，抽象检查未必走到——断言要兼收两种异常路径。
- 子类方法可以收窄 throws（协变异常），本测试 FailingAdapter 保持 throws Exception。
