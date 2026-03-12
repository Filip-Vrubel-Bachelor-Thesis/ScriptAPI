# ScriptAPI

Platform-agnostic contract for QuPath analysis scripts. A thin JAR containing only the `ScriptApi` interface — the shared language between user-authored scripts and any analysis platform (EMPAIA, local runner, etc.).

---

## Purpose

Scripts depend **only** on this JAR. They know nothing about EMPAIA, Docker, or QuPath internals beyond the types in `ScriptApi`. This makes scripts portable: the same `.groovy` file runs identically on any platform that implements the interface.

```
ScriptAPI (this repo)          qupath-extension-scriptlauncher
┌─────────────────────┐        ┌──────────────────────────────┐
│  ScriptApi          │◄───────│  EmpaiaScriptApi             │
│  (interface)        │        │  (EMPAIA implementation)     │
└─────────────────────┘        └──────────────────────────────┘
         ▲
         │ depends on
┌─────────────────────┐
│  User Groovy script │
│  (analysis logic)   │
└─────────────────────┘
```

---

## API Reference

```java
package qupath.ext.script.api;

public interface ScriptApi {

    // ── Inputs ────────────────────────────────────────────────────────────────

    /** Input ROI annotation for this job, already added to the hierarchy. Null = full slide. */
    PathObject getInputRoi();

    // ── Outputs ───────────────────────────────────────────────────────────────

    /** Post numeric results to the given output key. */
    void postValues(String outputKey, Collection<? extends Number> values);

    /** Post PathObject polygon detections to the given output key. */
    void postAnnotations(String outputKey, Collection<PathObject> detections);

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /** Report execution progress as a fraction [0.0, 1.0]. */
    void reportProgress(double fraction);

    /** Fail the job with a human-readable error message. */
    void fail(String message);

    // ── Runner (platform-managed, not called from scripts) ────────────────────

    void start(File script, ImageServer<?> server);
    double getProgress();
    boolean isFinished();
    Throwable getError();
}
```

---

## Writing a Script

Three variables are injected at runtime — do not import or instantiate them:

| Variable | Type | Description |
|---|---|---|
| `api` | `ScriptApi` | Post results and communicate with the platform |
| `imageData` | `ImageData` | The opened WSI |
| `hierarchy` | `PathObjectHierarchy` | QuPath object hierarchy |

```groovy
// Get the input region (or null for full slide)
def roi = api.getInputRoi()
if (roi != null) {
    hierarchy.addObject(roi)
    hierarchy.getSelectionModel().setSelectedObject(roi)
}
api.reportProgress(0.1)

// ... run your analysis ...

api.postAnnotations("output_annotations", hierarchy.getDetectionObjects())
api.postValues("output_values", [hierarchy.getDetectionObjects().size()])
api.reportProgress(1.0)
```

Scripts should **not** call `start()`, `isFinished()`, or `getError()` — those are used by the platform manager.

---

## Coordinates

The dependency is `compileOnly` — the JAR is provided at runtime by the platform (QuPath's classpath). Scripts only need it on the compilation classpath, not bundled.

In `qupath-extension-scriptlauncher/settings.gradle.kts`:
```kotlin
includeBuild("../ScriptAPI")
```

In `qupath-extension-scriptlauncher/build.gradle.kts`:
```kotlin
compileOnly("qupath.ext:script-api:0.1.0")
```

---

## Build

```bash
./gradlew build
# Output: build/libs/script-api-0.1.0.jar
```

Requires QuPath v0.6.0 installation at `../QuPath-v0.6.0-Linux/` (for `qupath-core` compile-only dependency).

---

## Requirements

- Java 21
- QuPath v0.6.0 at `../QuPath-v0.6.0-Linux/` (compile-only)
