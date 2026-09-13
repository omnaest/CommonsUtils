# CommonsUtils

Extended Java utilities building on `CommonsLangAndIO`, `CommonsJSONAndXML` and `CommonsRepository` —
caching, JSON-file persistence, file-backed maps, proxy recording and cyclic processing.

```xml
<dependency>
    <groupId>org.omnaest.utils</groupId>
    <artifactId>CommonsUtils</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## What is in here

This is an **aggregation library**: its root package hosts five independent feature areas rather than one,
each reached through a single static facade.

| Entry point | Feature |
|---|---|
| `CacheUtils` | in-memory, JSON-file, folder, capacity- and duration-limited caches |
| `ClassUtils` | reflection and classpath resources |
| `FileMapUtils` | file-backed and directory-backed `Map` implementations |
| `JsonFileUtils` | reading and writing JSON files |
| `ProcessorUtils` | cyclic and repeating filtered processors |

`ProxyRecorderUtils`, in `proxy/`, records method invocations on a proxy for later replay.

### Caching

`CacheUtils` is the facade; `cache.CacheFactory` is the door onto the implementations in `cache.internal`,
which are reached only through the interfaces in `cache`:

```java
Cache cache = CacheUtils.newConcurrentInMemoryCache();
Cache json  = CacheUtils.newJsonFileCache(new File("cache.json"));

CapacityLimitedCache limited = cache.withCapacityLimit(1000, Cache.EvictionStrategy.RANDOM);
UnaryCache<Person>   typed   = cache.asUnaryCache(Person.class);
```

Implement `EvictionStrategyHandler` to supply a custom eviction strategy.

## Package structure

Enforced mechanically by `PackageStructureTest`, using CommonsStyleSupport's ArchUnit rules. Because the five
feature areas are genuinely separate bounded contexts sharing one artifact, each facade is declared with
`@ContextRoot` and the count is committed under exact equality — a sixth root type fails the build until that
number is deliberately changed.

Implementation types live in `internal/` sub-packages and are reachable only from within their own subtree.
`CacheFactory` exists so the root facade never has to reach into `cache/internal` itself.

## Build

```cmd
mvn clean install
```

Requires `CommonsParent`, `CommonsTest`, `CommonsLog`, `CommonsLangAndIO`, `CommonsJSONAndXML` and
`CommonsRepository` to be installed first — see the workspace build order.
