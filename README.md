dview-mark-view

![Release](https://jitpack.io/v/dora4/dview-mark-view.svg)
--------------------------------

#### Gradle依赖配置

```groovy
// 添加以下代码到项目根目录下的build.gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
// 添加以下代码到app模块的build.gradle
dependencies {
    implementation 'com.github.dora4:dview-mark-view:1.1'
}
```

#### 使用方式

```kt
// 添加文字 mark
binding.doraMarkView.addTextMark("热门", bgColor = 0xFFE53935.toInt(), gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL)
binding.doraMarkView.addTextMark("限时", bgColor = 0xFFF57C00.toInt(), gravity = Gravity.BOTTOM or Gravity.END)

// 添加 Drawable mark
binding.doraMarkView.setMarkDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fire))

// 添加 View mark
val hotLabel = LayoutInflater.from(this).inflate(R.layout.view_hot_label, null)
binding.doraMarkView.setMarkView(hotLabel)
```
