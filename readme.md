This is very small performance test project to get a rough idea whether a Java
UUID or a string in UUID format is the faster option.

Summary
-------

Equals operations are a lot faster using the UUID type, while using it
as a key in a hash map seems to make no major difference.

When using default java serialization be aware that the serialization of strings
is further optimized within the JVM, so serializing a string is quite a lot
faster compared to a UUID.

I did a test using fast serialization library as well, and in this case the
serialization evens out.

Here's the test report generated on my machine using Oracle Java 8:
(higher score means faster)

```
Benchmark                                       Mode  Cnt    Score    Error   Units
OperationsTestFixture.stringCheckInMap         thrpt  200  311,125 ± 18,700  ops/us
OperationsTestFixture.stringEquals             thrpt  200   24,440 ±  0,044  ops/us
OperationsTestFixture.stringFastSerializeLoop  thrpt  200    4,753 ±  0,011  ops/us
OperationsTestFixture.stringSerializeLoop      thrpt  200    1,413 ±  0,002  ops/us
OperationsTestFixture.uuidCheckInMap           thrpt  200  284,865 ±  7,021  ops/us
OperationsTestFixture.uuidEquals               thrpt  200   46,462 ±  0,277  ops/us
OperationsTestFixture.uuidFastSerializeLoop    thrpt  200    4,374 ±  0,020  ops/us
OperationsTestFixture.uuidSerializeLoop        thrpt  200    0,395 ±  0,001  ops/us
```

