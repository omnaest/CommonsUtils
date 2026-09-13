# CommonsUtils

Extended utility library (`org.omnaest.utils`) that builds on `CommonsLangAndIO` by adding caching, JSON file persistence, proxy recording, and cyclic processing. 41 Java files across 9 packages.

## Build

```cmd
mvn clean install
mvn test -Dtest=MyTestClass#myMethod
```

## Architecture

Follows the same `*Utils` static-facade pattern as `CommonsLangAndIO`:

- **Static facades** — `CacheUtils`, `ClassUtils`, `FileMapUtils`, `JsonFileUtils`, `ProcessorUtils` at the root; `ProxyRecorderUtils` in `proxy/`
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
| `org.omnaest.utils` | `CacheUtils`, `ClassUtils`, `FileMapUtils`, `JsonFileUtils`, `ProcessorUtils` facades, each declared a `@ContextRoot`; plus `JsonFileElementCache`, which is unused dead code (see below) |
| `cache` | `Cache`, `UnaryCache`, `Cacheable`, `CacheDecorator` interfaces; `CacheFactory` — the door onto `cache.internal`, the only place that names those implementation types; `AbstractCache` — public abstract base (7 primitives / 8 derived methods) that `cache.internal` subclasses extend; `EvictionStrategyHandler` — the extension point a custom eviction strategy implements |
| `cache.internal` | `ConcurrentHashMapCache`, `JsonFolderFilesCache`, `DurationLimitedCache`, `JsonSingleFileCache`, `NoOperationCache`, etc. |
| `cache.internal.capacity` | `CacheCapacityLimiter`, `RandomEvictionStrategy` |
| `element` | `ListenableElement` |
| `map` | `FileSynchronizedMap`, `JSONFileSynchronizedMap`, `JSONDirectorySynchronizedValuesMap` |
| `processor.cyclic` | `CycleProcessor`, `DefaultCycleProcessor`, `CyclicHashMap` |
| `processor.repeating` | `RepeatingFilteredProcessor`, `DefaultRepeatingFilteredProcessor` |
| `proxy` | `ProxyRecorderUtils` support types |

## Key classes

- **`CacheUtils`** — main entry point; creates in-memory, file-backed, duration-limited, capacity-limited caches. Delegates every construction to `cache.CacheFactory` so the root facade never reaches into `cache.internal`
- **`JsonFileUtils`** / **`JSONFileSynchronizedMap`** — file-backed JSON map persistence
- **`RandomAccessLogarithmicBlockFileStorageCache`** — binary file-backed cache for large datasets
- **`ProxyRecorderUtils`** — records method invocations on a proxy for later replay
- **`CycleProcessor`** — round-robin processing over a set of elements

## Package structure enforcement

`PackageStructureTest` runs CommonsStyleSupport's rules over this project. Because
`org.omnaest.utils` is a split package across CommonsLangAndIO, CommonsJSONAndXML and this module, the
test imports `target/classes` directly rather than using `StyleProfile.mainClasses()`, which would pull
in ~963 classes instead of this project's own 99.

This is an **aggregation library**: its root package hosts five bounded contexts, not one. Each door is
declared with `@ContextRoot`, and the count is committed in `style-context-root-declarations.properties`
under exact equality — adding a sixth root type fails until that number is deliberately edited.

One violation is **pinned rather than skipped**: `JsonFileElementCache` is a stateful class at the
context root, so it fails `entryPointIsInterfaceOrUtilsFactory`. The test asserts that exact message, so
a second violation reds the build and *fixing* this one reds it too. The class has zero references
anywhere in the workspace and duplicates `CacheUtils.toFileCachedElement(..)`; deleting it is the
standing recommendation, which would clear the last violation and require dropping the pin.

## Dependencies (compile scope)

- `CommonsLangAndIO` — base utilities
- `CommonsJSONAndXML` — Jackson-based JSON serialization
- `CommonsRepository` — repository abstraction
- `CommonsLog` — logging

Test scope: `CommonsTest`, `CommonsStyleSupport` (the package-structure rules).

`provided` scope: `CommonsStyleAnnotations` — zero-dependency, carries `@ContextRoot`, which
decorates main sources and so has to be visible to the main compiler. Not transitive, so consumers
never resolve it.
