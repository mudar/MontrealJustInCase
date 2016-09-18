#-keep class ca.mudar.huegoallight.** { *; }
-keep class ca.mudar.mtlaucasou.ui.view.PlacemarksSearchView { *; }

-dontobfuscate

-keep class ca.mudar.mtlaucasou.model.geojson.base.** { *; }
-keep class ca.mudar.mtlaucasou.model.geojson.** { *; }
-keep class ca.mudar.mtlaucasou.model.** { *; }

-dontwarn com.roughike.bottombar.**

-dontwarn sun.misc.Unsafe
#-dontwarn com.google.common.collect.MinMaxPriorityQueue


