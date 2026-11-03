# java.awt.font.MultipleMaster 设计案

## 1. API 功能分组
- **接口**：`MultipleMaster`。
- **轴查询**：`getNumDesignAxes/getDesignAxisRanges/getDesignAxisDefaults/getDesignAxisNames`。
- **派生字体**：`deriveMMFont(float[] axes)`、`deriveMMFont(float[] axes, float dx, float dy, float sx, float sy)`。

## 2. Java 用户如何使用
用于 Multiple Master 字体（Adobe MM 字体）：实现类描述设计轴（如 Weight/Width）的默认值与范围，`deriveMMFont(axes)` 按轴值派生新字体实例。JDK 自带字体通常非 MM，用户通过 `sun.font` 内部实现获取。

## 3. 开源代码实际用法调研
- `java.awt.Font` 相关注释提及 MultipleMaster 接口用于 MM 字体（JDK 源码）。
- `sun.font.SunFontManager`/MM 字体实现类实现该接口（`src/java.desktop/share/classes/sun/font/`）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`MultipleMasterTest.testBlackBox()`，匿名实现）：
1. 轴查询方法返回值。
2. deriveMMFont 两个重载返回非空 Font。
白盒（`testWhiteBox()`）：interface、6 个方法签名。
