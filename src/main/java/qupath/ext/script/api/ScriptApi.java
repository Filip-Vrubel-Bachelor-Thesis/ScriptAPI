package qupath.ext.script.api;

import qupath.lib.images.servers.ImageServer;
import qupath.lib.objects.PathObject;

import java.io.File;
import java.util.Collection;

/**
 * ScriptApi — the platform-agnostic contract between user-authored QuPath/Groovy
 * scripts and any analysis platform (EMPAIA, local, etc.).
 *
 * <p>An instance is injected into every script at runtime under the variable
 * name {@code api}. Scripts should not instantiate this interface themselves.
 *
 * <p>Example Groovy script:
 * <pre>{@code
 * def roi       = api.getInputRoi()
 * def hierarchy = getCurrentHierarchy()
 * // ... run analysis ...
 * def detections = hierarchy.getDetectionObjects()
 * api.postAnnotations("output_annotations", detections)
 * api.postValues("output_values", [detections.size()])
 * }</pre>
 */
public interface ScriptApi {

    /**
     * Returns the input ROI for this job as a {@link PathObject} annotation.
     * The ROI has already been added to the current image hierarchy.
     *
     * @return the input ROI annotation, or {@code null} if none was provided
     */
    PathObject getInputRoi();

    /**
     * Posts a collection of numeric values to the given EMPAIA output key.
     * The values are wrapped in a {@code PostFloatCollection} as required by
     * the EMPAIA App API.
     *
     * @param outputKey the output key as defined in the EAD (e.g. "output_values")
     * @param values    the numeric results to post
     */
    void postValues(String outputKey, Collection<? extends Number> values);

    /**
     * Posts a collection of {@link PathObject} detections as polygon annotations
     * to the given EMPAIA output key.
     * Only objects with polygon ROIs are posted; others are silently skipped.
     *
     * @param outputKey  the output key as defined in the EAD (e.g. "output_annotations")
     * @param detections the detection objects whose ROIs should be posted
     */
    void postAnnotations(String outputKey, Collection<PathObject> detections);

    /**
     * Reports the current progress of the script execution as a fraction.
     * The platform manager will periodically read this value and forward it
     * to the job platform (e.g. EMPAIA's progress endpoint).
     *
     * <p>Call this from within your script to indicate how far along the
     * analysis is. The value is clamped to [0.0, 1.0].
     *
     * @param fraction completion fraction, where 0.0 = just started, 1.0 = done
     */
    void reportProgress(double fraction);

    /**
     * Reports a failure and terminates the job with an error message
     * visible to the user in the frontend.
     * After calling this method, the script should not post any further outputs.
     *
     * @param message a human-readable description of what went wrong
        * @return {@code true} if reporting the failure succeeded (HTTP 2xx), otherwise {@code false}
     */
        boolean failJob(String message);

    // ── Script lifecycle (formerly ScriptRunner) ──────────────────────────────

    /**
     * Starts executing the given script asynchronously. Returns immediately.
     * The platform manager is expected to poll {@link #isFinished()} until done.
     *
     * <p>The implementation is responsible for opening the image from the server,
     * adding the input ROI to the hierarchy, and injecting {@code api},
     * {@code imageData}, and {@code hierarchy} bindings into the script.
     *
     * @param script the Groovy script file to execute
     * @param server the image server providing pixel data
     */
    void start(File script, ImageServer<?> server);

    /**
     * Returns the current progress as a fraction [0.0, 1.0].
     * Updated asynchronously as the script calls {@link #reportProgress(double)}.
     */
    double getProgress();

    /**
     * Returns {@code true} once the script has finished, either successfully
     * or with an error.
     */
    boolean isFinished();

    /**
     * Returns the exception thrown during execution, or {@code null} if the
     * script completed successfully (or has not finished yet).
     */
    Throwable getError();
}
