package main.matching;

import main.clone.EnumCloneType;

import java.awt.*;
import java.util.function.BiConsumer;

/**
 * Represents a matching between two methods. This allows for classifying the matching with a clone type, as well as printing the matched source code
 * to the specified output.
 */
public interface IMethodMatching
{
    /**
     * Classify the match as a clone type.
     */
    EnumCloneType classify();

    /**
     * Display the matched source of the first method. The specified writer will be used to write the source text, colored based on the matching, to a target.
     * Practically speaking, the "writer" will repeatedly accept comparison units. Each comparison unit may thus have its own color, so that it is possible
     * to indicate how each comparison unit was matched.
     *
     * @param writer BiConsumer that writes the specified string with the specified color.
     */
    void writeMatchedMethod1(BiConsumer<String, Color> writer);

    /**
     * Display the matched source of the second method. The specified writer will be used to write the source text, colored based on the matching, to a target.
     * Practically speaking, the "writer" will repeatedly accept comparison units. Each comparison unit may thus have its own color, so that it is possible
     * to indicate how each comparison unit was matched.
     *
     * @param writer BiConsumer that writes the specified string with the specified color.
     */
    void writeMatchedMethod2(BiConsumer<String, Color> writer);

    /**
     * Helper method.
     *
     * In the specified array, each EnumCloneType element corresponds to a comparison unit of the original source code. Each such element indicates the way
     * that comparison unit was classified as: Type-1, Type-2, Type-3, or not matched (null).
     *
     * Note: "null" elements may ONLY exist at the begin or end of the array, they must NOT be surrounded by Type-1, Type-2, or Type-3 elements. Use {@link IMethodMatching#fillInType3Matches(EnumCloneType[])} to ensure this condition.
     *
     * This method will classify the entire method according to the following rules:
     *      - "null" prefix and suffix of the array is ignored.
     *      - If there exist a Type-3 element, the method is classified as Type-3,
     *      - else if there exists a Type-2 element, the method is classified as Type-2,
     *      - else, if there exist a Type-1 element, the method is classified as Type-1,
     *      - else, the method is classified as False Positive.
     *      - If the number of Type-1 or Type-2 elements is less than "min_size", the method is classified as False Positive.
     *      - If the number of Type-1 or Type-2 elements, divided by the total amount of non-null elements, is less than "min_density", the method is classified as False Positive.
     *
     * @param matches An array, each element of which corresponds to a comparison unit of the original source code, that indicates how the original comparison unit was classified as.
     * @param min_size The minimum size of the clone segment.
     * @param min_density The minimum density of the clone segment.
     */
     static EnumCloneType classifyMethod(EnumCloneType[] matches, int min_size, float min_density) {
        // initialize to null, since we may not encounter any matches
        EnumCloneType retval = null;

        // init metrics to zero
        int clone_segment_size = 0;
        int type12_lines = 0;

        // iterate over each element of the source code
        for (EnumCloneType match : matches) {

            // skip the non-matched prefix and suffix.
            // There are not "null" elements inbetween the prefix and suffixes.
            if(match == null) {
                continue;
            }

            // any non-null line is part of the clone segment. The clone segment the number of lines in
            clone_segment_size++;

            // count the number of lines that are T1 or T2 matches. This is to calculate density.
            if(match == EnumCloneType.TYPE_1 || match == EnumCloneType.TYPE_2) {
                type12_lines++;
            }

            if(retval == null) {
                // take first line
                retval = match;
            } else {
                retval = EnumCloneType.min(retval, match);
            }
        }

        // if no matches were detected => false positive
        if(retval == null) {
            retval = EnumCloneType.FP;
        }

        // if the minimum size has been specified, and the size is lower that the minimum size => FP
        if(clone_segment_size < min_size) {
            retval = EnumCloneType.FP;
        }

        // if the minimum density has been specified, and the density is lower than the minimum density => FP
        if((type12_lines / (float) clone_segment_size) < min_density) {
            retval = EnumCloneType.FP;
        }

        return retval;
    }

    /**
     * Helper method.
     *
     * If there are "gaps" (i.e. null-elements) in the specified matches, that are on both sides surrounded by TYPE-1 or TYPE-3
     * then those gaps will be filled by TYPE-3, since according to the clone types definition, such gaps are indicative of TYPE-3 clones.
     *
     * @param line_matches An array that contains null, Type-1, or Type-2.
     */
    static void fillInType3Matches(EnumCloneType[] line_matches) {
        int first_idx = -1;
        int last_idx = -1;

        // determine the index of the first non-null element
        for (int i = 0; i < line_matches.length; i++) {
            if(line_matches[i] != null) {
                first_idx = i;
                break;
            }
        }

        // determine the index of the last non-null element
        for (int i = line_matches.length - 1; i >= 0; i--) {
            if(line_matches[i] != null) {
                last_idx = i;
                break;
            }
        }

        // check if there are not matched elements
        if(first_idx == -1 || last_idx == -1) {
            return;
        }

        // fill all null elements inbetween first and last with TYPE 3
        // ignore all null prefixes and suffixes
        for(int i = first_idx; i <= last_idx; i++) {
            if(line_matches[i] == null) {
                line_matches[i] = EnumCloneType.TYPE_3;
            }
        }
    }
}
