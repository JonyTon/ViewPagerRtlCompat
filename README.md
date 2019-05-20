# ViewPagerRtlCompat

#### 介绍
ViewPager layout_direction rtl适配；阿语适配

主要针对 setCurrentItem，getCurrentItem，OnPageChangeListener进行适配！并可强制修改为FLAG_FORCE_LTR，FLAG_FORCE_RTL！

注意：只能对context的layout_direction适配，故而在xml中指定layout_direction是无效的！或者使用代码设置为FLAG_FORCE_LTR／FLAG_FORCE_RTL！

快速使用：com.github.JonyTon:ViewPagerRtlCompat:v1.0.1
