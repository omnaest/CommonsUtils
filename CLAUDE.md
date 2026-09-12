# CommonsUtils

Extended utility library (`org.omnaest.utils`) that builds on `CommonsLangAndIO` by adding caching, JSON file persistence, proxy recording, and cyclic processing. 40 Java files across 9 packages.

## Build

```cmd
mvn clean install
mvn test -Dtest=MyTestClass#myMethod
```

## Architecture

Follows the same `*Utils` static-facade pattern as `CommonsLangAndIO`:

- **Static facades** — `CacheUtils`, `ClassUtils`, `FileMapUtils`, `JsonFileUtils`, `ProcessorUtils`, `ProxyRecorderUtils`
- **Inner interfaces** — `Cache`, `UnaryCache`, `CycleProcessor`, `RepeatingFilteredProcessor`
- **`.internal`** — concrete implementations hidden behind interfaces
- **`.capacity`** — capacity-limiting eviction strategies as a sub-concern of cache

## Code style

- **Minimal Lombok** — 4× `@Data`, 4× `@Builder` (used in cache domain models)
- **No logging** — pure library
- **Interface-first caching** — `Cache` → `UnaryCache` → capacity-limited/duration-limited decorators stack via the decorator pattern
- **JSON persistence** — `JsonFileUtils`, `JSONFileSynchronizedMap`, `JSONDirectorySynchronizedValuesMap` use file-backed maps for lightweight persistence without a database

## Package map

| Package | What lives here |
|---|---|
| `org.omnaest.utils` | `CacheUtils`, `ClassUtils`, `FileMapUtils`, `JsonFileUtils`, `ProcessorUtils`, `ProxyRecorderUtils` facades |
| `cache` | `Cache`, `UnaryCache`, `Cacheable`, `CacheDecorator` interfaces + `CacheUtils` |
| `cache.internal` | `ConcurrentHashMapCache`, `JsonFileElementCache`, `JsonFolderFilesCache`, `DurationLimitedCache`, etc. |
| `cache.internal.capacity` | `CapacityLimitedCache/UnaryCache`, `RandomEvictionStrategy`, `EvictionStrategyHandler` |
| `element` | `ListenableElement` |
| `map` | `CyclicHashMap`, `FileSynchronizedMap` |
| `processor.cyclic` | `CycleProcessor`, `DefaultCycleProcessor` |
| `processor.repeating` | `RepeatingFilteredProcessor`, `DefaultRepeatingFilteredProcessor` |
| `proxy` | `ProxyRecorderUtils` support types |

## Key classes

- **`CacheUtils`** — main entry point; creates in-memory, file-backed, duration-limited, capacity-limited caches
- **`JsonFileUtils`** / **`JSONFileSynchronizedMap`** — file-backed JSON map persistence
- **`RandomAccessLogarithmicBlockFileStorageCache`** — binary file-backed cache for large datasets
- **`ProxyRecorderUtils`** — records method invocations on a proxy for later replay
- **`CycleProcessor`** — round-robin processing over a set of elements

## Dependencies (compile scope)

- `CommonsLangAndIO` — base utilities
- `CommonsJSONAndXML` — Jackson-based JSON serialization
- `CommonsRepository` — repository abstraction
- `CommonsLog` — logging

Test scope: `CommonsTest`.
