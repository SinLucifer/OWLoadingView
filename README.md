# OWLoadingView 一个模仿守望先锋加载的LoadingView
A LoadingView imitate OverWatch loading

## 使用方法

```xml
<com.sin.overwatchloading.OverWatchLoadingView
    android:id="@+id/loading"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_centerInParent="true"
    app:radius="40dp"
    app:color="#efb77a"/>    
```

```java
  OverWatchLoadingView owView = (OverWatchLoadingView)findViewById(R.id.loading);
  owView.start(); //loading
  owView.stop(); //ending
```

## 效果
![image](https://github.com/SinLucifer/OWLoadingView/blob/master/gif/preview.gif)

## THX
[chen_zhang_yu的博客](http://blog.csdn.net/chen_zhang_yu/article/details/53396801)
