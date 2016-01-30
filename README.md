# Infinity Loading
[![Release](https://jitpack.io/v/ndczz/infinity-loading.svg)](https://jitpack.io/#ndczz/infinity-loading) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-infinity--loading-brightgreen.svg?style=true)](https://android-arsenal.com/details/1/3078)

Simple Android loading view

![InfinityLoading](https://github.com/ndczz/infinity-loading/blob/master//loading.gif)

### Integration
```gradle
  allprojects {
      repositories {
          ....
          maven { url "https://jitpack.io" }
      }
  }

  dependencies {
      compile 'com.github.ndczz:infinity-loading:0.4'
  }
```

## Usage

```xml
<com.github.ndczz.infinityloading.InfinityLoading
        android:id="@+id/loading"
        android:layout_width="120dp"
        android:layout_height="60dp"
        android:layout_gravity="center_horizontal"
        app:infl_backColor="@color/colorPrimary"
        app:infl_drawBack="true"
        app:infl_reverse="true"
        app:infl_progressColor="@color/colorAccent"
        app:infl_strokeWidth="4dp" />
```
or

```java
  InfinityLoading loading = new InfinityLoading(context);
  loading.setProgressColor(Color.Red);
  ...
```

License
-------

    Copyright 2016 Alexey Shklyaev

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
