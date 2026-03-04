package io.empath.api;

import qupath.lib.objects.PathObject;

import java.util.Collection;

/**
 * EmpathApi — the public contract between user-authored QuPath/Groovy scripts
 * and the EMPAIA platform.
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
public interface EmpathApi {

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
     * Reports a failure to EMPAIA and terminates the job with an error message
     * visible to the user in the frontend.
     * After calling this method, the script should not post any further outputs.
     *
     * @param message a human-readable description of what went wrong
     */
    void fail(String message);
}
