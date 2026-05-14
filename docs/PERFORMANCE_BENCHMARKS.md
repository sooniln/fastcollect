# Performance Benchmarks

JVM JMH benchmarks comparing FastCollect against several other JVM collection frameworks. While investigating benchmarks
for non-JVM platforms, kotlinx-benchmarking appears to be the primary tool for the job - unfortunately its results have
not proven trustworthy (it appears to have large memory leak issues on JS platforms and does not support many of the JMH
features available on JVM which make micro-benchmarking useful).

> [!WARNING]
> Micro-benchmarks of the sort performed here are NOT good indicators of real world performance. These benchmarks are
> primarily performed with random data (and it's trivial to write a fast HashSet/HashMap against random data - now try
> to keep it fast against more adversarial data patterns). These benchmark do not measure JVM optimizations that can
> often substantially change performance profiles, and they are executed on a single machine that is not representative
> of most machines. Take them with an extremely large grain of salt.

---

## List (IntArrayList or equivalent)

FastCollect provides a single ArrayDeque implementation which also serves as an ArrayList (no need to ship two separate
implementations which have significant feature overlap). This does introduce a slight performance penalty as ArrayDeque
bounds checking and logic is more complex than ArrayList, but difference is small enough that the decision seems
justified.

### Add (pre-size list with single allocation and then build to size by repeated single adds)

| Library / Size |      3,000 |     12,000 |     48,000 |    192,000 |    768,000 |  3,072,000 |
|:---------------|-----------:|-----------:|-----------:|-----------:|-----------:|-----------:|
| `fastcollect`  | `0.862 ns` | `0.862 ns` | `0.857 ns` | `0.859 ns` | `0.853 ns` | `0.854 ns` |
| `androidx`     | `0.813 ns` | `0.809 ns` | `0.808 ns` | `0.807 ns` | `0.812 ns` | `0.803 ns` |
| `fastutil`     | `0.921 ns` | `0.948 ns` | `0.921 ns` | `1.012 ns` | `0.926 ns` | `0.928 ns` |
| `eclipse`      | `0.804 ns` | `0.816 ns` | `0.786 ns` | `0.786 ns` | `0.791 ns` | `0.797 ns` |
| `kotlin`       | `4.727 ns` | `4.880 ns` | `4.757 ns` | `5.236 ns` | `4.890 ns` | `4.947 ns` |

### Grow (build list to size from empty by repeated single adds)

| Library / Size |      3,000 |      12,000 |       48,000 |      192,000 |       768,000 |      3,072,000 |
|:---------------|-----------:|------------:|-------------:|-------------:|--------------:|---------------:|
| `fastcollect`  | `4.138 us` | `19.749 us` |  `76.201 us` | `277.910 us` | `1287.160 us` |  `4735.170 us` |
| `androidx`     | `3.601 us` | `16.598 us` |  `62.213 us` | `301.990 us` | `1139.849 us` |  `4923.639 us` |
| `fastutil`     | `4.991 us` | `16.057 us` |  `70.518 us` | `243.733 us` |  `967.594 us` |  `4157.405 us` |
| `eclipse`      | `4.114 us` | `17.952 us` |  `60.780 us` | `214.151 us` | `1046.346 us` |  `3742.844 us` |
| `kotlin`       | `7.995 us` | `27.318 us` | `132.949 us` | `434.645 us` | `8290.726 us` | `47588.507 us` |

### Iterate (iterate over every element)

| Library / Size |      3,000 |     12,000 |     48,000 |     192,000 |      768,000 |     3,072,000 |
|:---------------|-----------:|-----------:|-----------:|------------:|-------------:|--------------:|
| `fastcollect`  | `0.294 us` | `1.186 us` | `4.496 us` | `17.972 us` |  `66.602 us` |  `272.948 us` |
| `androidx`     | `0.174 us` | `0.742 us` | `2.964 us` | `11.792 us` |  `47.605 us` |  `189.386 us` |
| `fastutil`     | `0.158 us` | `0.691 us` | `2.743 us` | `10.974 us` |  `44.309 us` |  `175.001 us` |
| `eclipse`      | `0.222 us` | `0.860 us` | `3.435 us` | `20.698 us` |  `71.311 us` |  `216.498 us` |
| `kotlin`       | `0.604 us` | `2.325 us` | `9.891 us` | `45.308 us` | `156.577 us` | `1266.579 us` |

### Search (indexOf/lastIndexOf() for every element)

| Library / Size |      3,000 |     12,000 |      48,000 |     192,000 |      768,000 |     3,072,000 |
|:---------------|-----------:|-----------:|------------:|------------:|-------------:|--------------:|
| `fastcollect`  | `0.393 us` | `1.528 us` |  `6.405 us` | `21.498 us` |  `72.401 us` |  `285.071 us` |
| `androidx`     | `0.366 us` | `1.443 us` |  `5.850 us` | `22.250 us` |  `83.434 us` |  `275.928 us` |
| `fastutil`     | `0.374 us` | `1.456 us` |  `5.780 us` | `26.784 us` |  `84.327 us` |  `296.851 us` |
| `eclipse`      | `0.367 us` | `1.423 us` |  `5.613 us` | `26.971 us` | `105.815 us` |  `266.883 us` |
| `kotlin`       | `0.864 us` | `2.847 us` | `11.268 us` | `42.275 us` | `190.406 us` | `1083.402 us` |

---

## Map (Int2IntHashMap or equivalent)

### GetHit (retrieve a key present in the table)

| Library / Size |      3,000 |     12,000 |     48,000 |     192,000 |     768,000 |   3,072,000 |  12,288,000 |
|:---------------|-----------:|-----------:|-----------:|------------:|------------:|------------:|------------:|
| `fastcollect`  | `1.527 ns` | `1.652 ns` | `8.544 ns` | `10.334 ns` | `10.848 ns` | `16.345 ns` | `30.522 ns` |
| `androidx`     | `3.822 ns` | `3.966 ns` | `4.207 ns` |  `6.467 ns` |  `8.144 ns` | `27.740 ns` | `48.506 ns` |
| `fastutil`     | `1.776 ns` | `1.851 ns` | `5.774 ns` |  `7.950 ns` |  `9.013 ns` | `15.851 ns` | `30.214 ns` |
| `eclipse`      | `1.150 ns` | `1.530 ns` | `1.754 ns` |  `4.476 ns` |  `4.957 ns` | `16.256 ns` | `18.540 ns` |
| `kotlin`       | `2.623 ns` | `3.257 ns` | `5.249 ns` |  `8.233 ns` | `30.125 ns` | `43.796 ns` | `58.580 ns` |
| `hashsmith`    | `5.091 ns` | `5.704 ns` | `8.757 ns` | `11.616 ns` | `31.514 ns` | `66.647 ns` | `96.788 ns` |

### GetMiss (attempt to retrieve a key not present in the table)

| Library / Size |      3,000 |     12,000 |      48,000 |     192,000 |     768,000 |   3,072,000 |  12,288,000 |
|:---------------|-----------:|-----------:|------------:|------------:|------------:|------------:|------------:|
| `fastcollect`  | `2.998 ns` | `3.113 ns` |  `9.916 ns` | `10.791 ns` | `12.867 ns` | `13.226 ns` | `34.289 ns` |
| `androidx`     | `3.908 ns` | `4.047 ns` |  `6.605 ns` |  `7.912 ns` |  `8.482 ns` | `10.152 ns` | `13.725 ns` |
| `fastutil`     | `3.755 ns` | `3.693 ns` | `11.532 ns` | `13.081 ns` | `17.571 ns` | `22.269 ns` | `54.590 ns` |
| `eclipse`      | `1.560 ns` | `1.363 ns` |  `4.277 ns` |  `7.293 ns` |  `8.137 ns` | `17.545 ns` | `21.244 ns` |
| `kotlin`       | `2.096 ns` | `2.249 ns` |  `6.860 ns` |  `9.110 ns` | `19.577 ns` | `29.543 ns` | `39.791 ns` |
| `hashsmith`    | `4.893 ns` | `4.759 ns` |  `8.730 ns` | `10.055 ns` | `11.154 ns` | `18.722 ns` | `25.366 ns` |

### PutHit (update a key already present in the table)

| Library / Size |      3,000 |      12,000 |      48,000 |     192,000 |     768,000 |   3,072,000 |   12,288,000 |
|:---------------|-----------:|------------:|------------:|------------:|------------:|------------:|-------------:|
| `fastcollect`  | `3.101 ns` |  `3.727 ns` | `11.434 ns` | `13.755 ns` | `14.501 ns` | `23.717 ns` |  `43.568 ns` |
| `androidx`     | `4.644 ns` |  `5.152 ns` |  `5.612 ns` | `10.455 ns` | `14.618 ns` | `44.043 ns` |  `50.368 ns` |
| `fastutil`     | `2.488 ns` |  `2.869 ns` |  `7.442 ns` |  `9.536 ns` | `10.307 ns` | `15.137 ns` |  `32.785 ns` |
| `eclipse`      | `1.588 ns` |  `1.565 ns` |  `1.815 ns` |  `4.617 ns` |  `4.964 ns` | `16.854 ns` |  `20.338 ns` |
| `kotlin`       | `8.069 ns` | `12.347 ns` | `13.358 ns` | `19.789 ns` | `68.439 ns` | `79.480 ns` | `140.045 ns` |
| `hashsmith`    | `8.733 ns` |  `9.928 ns` | `13.459 ns` | `19.529 ns` | `25.290 ns` | `92.668 ns` | `122.738 ns` |

### PutMiss (insert a key not present in the table)

| Library / Size |        3,000 |       12,000 |       48,000 |      192,000 |      768,000 |    3,072,000 |   12,288,000 |
|:---------------|-------------:|-------------:|-------------:|-------------:|-------------:|-------------:|-------------:|
| `fastcollect`  |  `26.490 ns` |  `28.848 ns` |  `26.782 ns` |  `27.514 ns` |  `28.296 ns` |  `29.032 ns` |  `25.139 ns` |
| `androidx`     |  `33.710 ns` |  `32.997 ns` |  `27.715 ns` |  `29.037 ns` |  `28.230 ns` |  `31.762 ns` |  `36.420 ns` |
| `fastutil`     |  `24.064 ns` |  `22.862 ns` |  `25.721 ns` |  `25.450 ns` |  `25.649 ns` |  `26.431 ns` |  `26.891 ns` |
| `eclipse`      |  `19.483 ns` |  `19.414 ns` |  `19.314 ns` |  `19.223 ns` |  `19.842 ns` |  `18.967 ns` |  `18.607 ns` |
| `kotlin`       |  `48.337 ns` |  `57.708 ns` |  `63.675 ns` |  `57.200 ns` |  `54.605 ns` |  `52.333 ns` |  `50.156 ns` |
| `hashsmith`    | `102.965 ns` | `105.039 ns` | `104.080 ns` | `101.288 ns` | `107.652 ns` | `119.129 ns` | `106.751 ns` |

### Grow (build map to size from empty by repeated insertions)

| Library / Size |       3,000 |       12,000 |        48,000 |       192,000 |        768,000 |       3,072,000 |       12,288,000 |
|:---------------|------------:|-------------:|--------------:|--------------:|---------------:|----------------:|-----------------:|
| `fastcollect`  | `24.797 us` | `248.097 us` | `1297.784 us` | `5595.212 us` | `24408.432 us` | `133225.559 us` |  `916382.220 us` |
| `androidx`     | `40.000 us` | `167.000 us` |  `800.000 us` | `3526.000 us` | `15897.000 us` |  `82979.000 us` |  `483066.815 us` |
| `fastutil`     | `17.064 us` |  `87.878 us` |  `828.564 us` | `3659.372 us` | `16970.737 us` |  `92506.530 us` |  `595329.073 us` |
| `eclipse`      | `18.126 us` | `136.105 us` |  `862.897 us` | `3770.526 us` | `16598.382 us` |  `95715.354 us` |  `443613.305 us` |
| `kotlin`       | `24.737 us` | `177.943 us` | `1081.215 us` | `9630.993 us` | `48709.632 us` | `288272.188 us` | `1832025.440 us` |
| `hashsmith`    | `48.274 us` | `202.775 us` | `1050.379 us` | `9037.988 us` | `66509.965 us` | `387416.837 us` | `2318260.080 us` |

### Iterate (iterate over every key/value pair in the map)

| Library / Size |      3,000 |      12,000 |       48,000 |       192,000 |        768,000 |      3,072,000 |      12,288,000 |
|:---------------|-----------:|------------:|-------------:|--------------:|---------------:|---------------:|----------------:|
| `fastcollect`  | `1.727 us` |  `8.476 us` |  `34.929 us` |  `545.266 us` |  `2282.453 us` |  `9660.753 us` |  `40754.721 us` |
| `androidx`     | `2.000 us` |  `6.000 us` |  `26.000 us` |  `438.000 us` |  `2010.000 us` |  `7546.000 us` |  `30977.847 us` |
| `fastutil`     | `5.513 us` | `25.659 us` | `211.248 us` | `1027.373 us` |  `3925.367 us` | `18182.800 us` |  `74575.608 us` |
| `eclipse`      | `8.492 us` | `51.551 us` | `420.508 us` | `1837.619 us` |  `7295.198 us` | `29564.563 us` | `116991.872 us` |
| `kotlin`       | `4.085 us` | `17.075 us` | `374.002 us` | `1672.900 us` | `11363.420 us` | `68874.721 us` | `143500.545 us` |
| `hashsmith`    | `4.788 us` | `21.937 us` | `128.313 us` | `1040.114 us` | `11268.538 us` | `91183.476 us` | `441677.530 us` |

### IterateFast (iterate without any allocations)

| Library / Size |      3,000 |     12,000 |      48,000 |      192,000 |       768,000 |     3,072,000 |     12,288,000 |
|:---------------|-----------:|-----------:|------------:|-------------:|--------------:|--------------:|---------------:|
| `fastcollect`  | `1.623 us` | `8.499 us` | `32.040 us` | `507.644 us` | `2155.723 us` | `9583.047 us` | `37865.616 us` |
| `fastutil`     | `2.251 us` | `6.479 us` | `63.208 us` | `527.141 us` | `2304.005 us` | `8637.487 us` | `34289.064 us` |

### IterateKeys (iterate over every key in the map)

| Library / Size |      3,000 |      12,000 |       48,000 |       192,000 |       768,000 |      3,072,000 |      12,288,000 |
|:---------------|-----------:|------------:|-------------:|--------------:|--------------:|---------------:|----------------:|
| `fastcollect`  | `1.759 us` | `14.368 us` |  `37.763 us` |  `524.584 us` | `2195.386 us` |  `9062.568 us` |  `37027.760 us` |
| `androidx`     | `1.000 us` |  `6.000 us` |  `23.000 us` |  `400.000 us` | `1687.000 us` |  `6913.000 us` |  `29568.755 us` |
| `fastutil`     | `1.732 us` |  `6.838 us` | `124.096 us` |  `555.131 us` | `2046.154 us` |  `8588.513 us` |  `36168.591 us` |
| `eclipse`      | `4.517 us` | `19.228 us` | `296.408 us` | `1287.283 us` | `5273.548 us` | `20973.961 us` |  `84890.892 us` |
| `kotlin`       | `4.010 us` | `15.904 us` | `348.536 us` | `1560.509 us` | `4987.954 us` | `23505.038 us` | `103986.287 us` |
| `hashsmith`    | `4.263 us` | `17.959 us` | `157.637 us` |  `967.039 us` | `3891.722 us` | `55108.381 us` | `440566.560 us` |

### IterateValues (iterate over every value in the map)

| Library / Size |       3,000 |      12,000 |       48,000 |       192,000 |       768,000 |      3,072,000 |      12,288,000 |
|:---------------|------------:|------------:|-------------:|--------------:|--------------:|---------------:|----------------:|
| `fastcollect`  |  `1.604 us` |  `8.199 us` |  `38.750 us` |  `537.687 us` | `2235.607 us` |  `9665.663 us` |  `39013.825 us` |
| `androidx`     |  `1.000 us` |  `7.000 us` |  `24.000 us` |  `399.000 us` | `1698.000 us` |  `6888.000 us` |  `28520.782 us` |
| `fastutil`     |  `1.921 us` |  `6.267 us` |  `52.328 us` |  `511.236 us` | `1970.305 us` |  `8428.571 us` |  `33306.300 us` |
| `eclipse`      | `10.120 us` | `50.374 us` | `512.015 us` | `2180.484 us` | `8870.803 us` | `35094.574 us` | `139450.891 us` |
| `kotlin`       |  `3.981 us` | `16.159 us` | `338.059 us` | `1794.181 us` | `5162.193 us` | `21088.003 us` |  `90588.062 us` |
| `hashsmith`    |  `4.390 us` | `17.755 us` | `131.295 us` |  `854.402 us` | `8990.353 us` | `85464.117 us` | `412200.725 us` |

---

## Set (IntHashSet or equivalent)

### GetHit (retrieve a key present in the table)

| Library / Size |      3,000 |     12,000 |     48,000 |     192,000 |     768,000 |   3,072,000 |
|:---------------|-----------:|-----------:|-----------:|------------:|------------:|------------:|
| `fastcollect`  | `1.492 ns` | `1.498 ns` | `8.210 ns` |  `9.176 ns` | `10.419 ns` | `10.611 ns` |
| `androidx`     | `3.806 ns` | `3.993 ns` | `4.062 ns` |  `5.463 ns` |  `7.556 ns` |  `8.692 ns` |
| `fastutil`     | `1.662 ns` | `1.726 ns` | `5.839 ns` |  `6.607 ns` |  `8.500 ns` |  `8.875 ns` |
| `eclipse`      | `2.234 ns` | `2.266 ns` | `5.439 ns` |  `8.203 ns` |  `8.720 ns` | `13.799 ns` |
| `kotlin`       | `2.365 ns` | `2.641 ns` | `4.394 ns` |  `8.228 ns` | `22.512 ns` | `38.344 ns` |
| `hashsmith`    | `5.428 ns` | `6.860 ns` | `8.773 ns` | `11.858 ns` | `15.563 ns` | `44.272 ns` |

### GetMiss (attempt to retrieve a key not present in the table)

| Library / Size |      3,000 |     12,000 |      48,000 |     192,000 |     768,000 |   3,072,000 |
|:---------------|-----------:|-----------:|------------:|------------:|------------:|------------:|
| `fastcollect`  | `2.844 ns` | `2.987 ns` |  `9.707 ns` | `10.473 ns` | `12.519 ns` | `12.990 ns` |
| `androidx`     | `4.053 ns` | `4.063 ns` |  `6.622 ns` |  `7.912 ns` |  `8.629 ns` | `10.759 ns` |
| `fastutil`     | `3.568 ns` | `3.353 ns` | `11.549 ns` | `12.556 ns` | `16.888 ns` | `17.929 ns` |
| `eclipse`      | `1.991 ns` | `1.579 ns` |  `1.651 ns` |  `4.640 ns` |  `5.220 ns` | `10.935 ns` |
| `kotlin`       | `2.071 ns` | `2.129 ns` |  `6.428 ns` |  `9.152 ns` | `13.876 ns` | `27.152 ns` |
| `hashsmith`    | `4.513 ns` | `4.378 ns` |  `6.605 ns` |  `9.085 ns` | `11.709 ns` | `33.619 ns` |

### PutHit (update a key already present in the table)

| Library / Size |      3,000 |     12,000 |      48,000 |     192,000 |     768,000 |   3,072,000 |
|:---------------|-----------:|-----------:|------------:|------------:|------------:|------------:|
| `fastcollect`  | `2.581 ns` | `2.787 ns` | `10.005 ns` | `10.822 ns` | `12.525 ns` | `12.933 ns` |
| `androidx`     | `4.724 ns` | `4.637 ns` |  `5.025 ns` |  `6.032 ns` |  `8.769 ns` | `11.692 ns` |
| `fastutil`     | `1.662 ns` | `1.655 ns` |  `5.627 ns` |  `6.523 ns` |  `8.349 ns` |  `8.871 ns` |
| `eclipse`      | `1.876 ns` | `1.978 ns` |  `4.749 ns` |  `6.638 ns` |  `9.204 ns` | `20.872 ns` |
| `kotlin`       | `7.487 ns` | `9.296 ns` | `12.725 ns` | `16.279 ns` | `52.025 ns` | `65.862 ns` |
| `hashsmith`    | `5.792 ns` | `8.179 ns` |  `8.732 ns` | `11.462 ns` | `15.564 ns` | `42.342 ns` |

### PutMiss (insert a key not present in the table)

| Library / Size |       3,000 |      12,000 |      48,000 |     192,000 |      768,000 |   3,072,000 |
|:---------------|------------:|------------:|------------:|------------:|-------------:|------------:|
| `fastcollect`  | `15.476 ns` | `16.257 ns` | `14.471 ns` | `13.876 ns` |  `14.444 ns` | `16.391 ns` |
| `androidx`     | `21.033 ns` | `21.461 ns` | `19.550 ns` | `19.920 ns` |  `21.349 ns` | `21.327 ns` |
| `fastutil`     | `14.781 ns` | `16.856 ns` | `14.567 ns` | `15.217 ns` |  `14.541 ns` | `15.105 ns` |
| `eclipse`      | `19.226 ns` | `19.045 ns` | `18.550 ns` | `18.388 ns` |  `18.680 ns` | `21.324 ns` |
| `kotlin`       | `46.561 ns` | `47.620 ns` | `50.554 ns` | `52.624 ns` |  `52.053 ns` | `49.477 ns` |
| `hashsmith`    | `55.704 ns` | `61.916 ns` | `61.311 ns` | `62.268 ns` | `161.434 ns` | `81.222 ns` |

### Grow (build map to size from empty by repeated insertions)

| Library / Size |      3,000 |     12,000 |     48,000 |    192,000 |     768,000 |    3,072,000 |
|:---------------|-----------:|-----------:|-----------:|-----------:|------------:|-------------:|
| `fastcollect`  | `0.022 ms` | `0.272 ms` | `1.346 ms` | `6.240 ms` | `25.525 ms` | `113.260 ms` |
| `androidx`     | `0.039 ms` | `0.161 ms` | `0.762 ms` | `3.448 ms` | `14.720 ms` |  `65.943 ms` |
| `fastutil`     | `0.027 ms` | `0.096 ms` | `0.777 ms` | `2.922 ms` | `12.986 ms` |  `62.895 ms` |
| `eclipse`      | `0.016 ms` | `0.121 ms` | `0.860 ms` | `3.577 ms` | `15.716 ms` |  `77.551 ms` |
| `kotlin`       | `0.021 ms` | `0.156 ms` | `1.035 ms` | `8.051 ms` | `44.351 ms` | `253.290 ms` |
| `hashsmith`    | `0.009 ms` | `0.037 ms` | `0.672 ms` | `2.911 ms` | `12.986 ms` |  `60.812 ms` |

### Iterate (iterate over every key/value pair in the map)

| Library / Size |      3,000 |     12,000 |     48,000 |    192,000 |     768,000 |   3,072,000 |
|:---------------|-----------:|-----------:|-----------:|-----------:|------------:|------------:|
| `fastcollect`  | `0.002 ms` | `0.007 ms` | `0.034 ms` | `0.499 ms` |  `2.085 ms` |  `8.247 ms` |
| `androidx`     | `0.001 ms` | `0.006 ms` | `0.023 ms` | `0.400 ms` |  `1.702 ms` |  `6.886 ms` |
| `fastutil`     | `0.002 ms` | `0.021 ms` | `0.053 ms` | `0.503 ms` |  `1.919 ms` |  `8.367 ms` |
| `eclipse`      | `0.003 ms` | `0.012 ms` | `0.294 ms` | `1.301 ms` |  `5.541 ms` | `21.344 ms` |
| `kotlin`       | `0.008 ms` | `0.031 ms` | `0.443 ms` | `1.973 ms` | `18.563 ms` | `41.150 ms` |
| `hashsmith`    | `0.004 ms` | `0.021 ms` | `0.131 ms` | `0.750 ms` |  `3.331 ms` | `47.195 ms` |

---
