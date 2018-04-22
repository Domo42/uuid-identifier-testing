package com.codebullets.test.identifiers;

import org.nustaq.serialization.FSTConfiguration;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@State(Scope.Thread)
public class OperationsTestFixture {
    static FSTConfiguration fstConf = FSTConfiguration.createDefaultConfiguration();
    private UUID[] uuidData = new UUID[10];
    private String[] stringIdData;
    private Blackhole bh;
    private Map<UUID, Object> uuidMap;
    private Map<String, Object> stringMap;
    private UUID uuidRefId;
    private String stringRefId;

    @Setup
    public void setup(final Blackhole bh) {
        this.bh = bh;
        generateTestData();

        fstConf.registerClass(UUID.class);
        fstConf.registerClass(String.class);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void uuidEquals() {
        for (int i = 1; i < 10; ++i) {
            boolean isEqual = uuidData[i -1].equals(uuidData[i]);
            sink(isEqual);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void stringEquals() {
        for (int i = 1; i < 10; ++i) {
            boolean isEqual = stringIdData[i -1].equals(stringIdData[i]);
            sink(isEqual);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void uuidCheckInMap() {
        boolean isContained = uuidMap.containsKey(uuidRefId);
        sink(isContained);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void stringCheckInMap() {
        boolean isContained = stringMap.containsKey(stringRefId);
        sink(isContained);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void uuidSerializeLoop() throws IOException, ClassNotFoundException {
        byte[] serialized = serialize(uuidRefId);
        Object deSerialized = deserialize(serialized);

        bh.consume(uuidRefId.equals(deSerialized));
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void stringSerializeLoop() throws IOException, ClassNotFoundException {
        byte[] serialized = serialize(stringRefId);
        Object deSerialized = deserialize(serialized);

        bh.consume(stringRefId.equals(deSerialized));
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void uuidFastSerializeLoop() throws IOException, ClassNotFoundException {
        byte[] serialized = fastSerialize(uuidRefId);
        Object deSerialized = fastDeserialize(serialized);

        bh.consume(uuidRefId.equals(deSerialized));
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void stringFastSerializeLoop() throws IOException, ClassNotFoundException {
        byte[] serialized = fastSerialize(stringRefId);
        Object deSerialized = fastDeserialize(serialized);

        bh.consume(stringRefId.equals(deSerialized));
    }

    public static void main(String [] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(OperationsTestFixture.class.getSimpleName())
                .measurementTime(TimeValue.seconds(5))
                .warmupIterations(5)
                .jvmArgsAppend("-XX:+UseG1GC", "-Xmx2048m", "-Xms1024m")
                .measurementIterations(5)
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public static void sink(boolean v) {
        // IT IS VERY IMPORTANT TO MATCH THE SIGNATURE TO AVOID AUTOBOXING.
        // The method intentionally does nothing.
    }

    private void generateTestData() {
        for (int i = 0; i < 10; ++i) {
            uuidData[i] = UUID.randomUUID();
        }

        stringIdData = Stream.of(uuidData).map(UUID::toString).toArray(String[]::new);
        uuidMap = Stream.of(uuidData).collect(Collectors.toMap(v -> v, v -> new Object()));
        stringMap = Stream.of(uuidData).collect(Collectors.toMap(UUID::toString, v -> new Object()));

        uuidRefId = UUID.randomUUID();
        stringRefId = uuidRefId.toString();
    }

    private byte[] fastSerialize(final Object object) {
        return fstConf.asByteArray(object);
    }

    private Object fastDeserialize(final byte[] bytes) {
        return fstConf.asObject(bytes);
    }

    private byte[] serialize(final Object object) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        try (ObjectOutputStream os = new ObjectOutputStream(bo)) {
            os.writeObject(object);
        }

        bo.close();
        return bo.toByteArray();
    }

    private Object deserialize(final byte[] bytes) throws IOException, ClassNotFoundException {
        Object deSerialized;

        try (ByteArrayInputStream bi = new ByteArrayInputStream(bytes)) {
            try (ObjectInputStream oi = new ObjectInputStream(bi)) {
                deSerialized = oi.readObject();
            }
        }

        return deSerialized;
    }
}
