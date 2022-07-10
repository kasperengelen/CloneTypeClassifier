/**
 *   Copyright (C) 2020  Kasper Engelen
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.

 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.

 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package main.matching;

import com.github.javaparser.ParseProblemException;
import main.clone.EnumCloneType;
import main.method.Line;
import main.method.Method;
import main.method.Token;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Matching between two methods based on the lines of the methods. Here, the comparison units are entire lines.
 */
public class LineMatching implements IMethodMatching
{
    // the lines per method
    private final List<Line> m_method1Lines;
    private final List<Line> m_method2Lines;

    // the matches for each line
    private final EnumCloneType[] m_method1Matched;
    private final EnumCloneType[] m_method2Matched;

    private final int m_minSize;
    private final float m_minDensity;

    /**
     * Constructor.
     *
     * @param method_1 The first method.
     * @param method_2 The second method.
     * @param matching_algo The algorithm that will be used to match lines of the first method with lines of the second method.
     * @param min_size The minimum size of the clone segment. The size of such a segment is the amount of lines between the first and last line of the segment. Set to 0 to ignore.
     * @param min_density The minimum density of the clone segment. This is the number of Type-1 or Type-2 matched lines divided by the segment size. Set to 0.0 to ignore.
     * @param braces_preprocessing Remove all braces before the comparison algorithm is applied.
     * @param braces_postprocessing Remove all braces after the comparison algorithm is applied.
     *
     * @throws MatchingException In case there is an IO error, or a parsing error.
     */
    public LineMatching(Method method_1, Method method_2, SequenceComparisonAlgos.IMatchingAlgo<Line, EnumCloneType> matching_algo, int min_size, float min_density, boolean braces_preprocessing, boolean braces_postprocessing) throws MatchingException
    {
        try {
            m_minSize = min_size;
            m_minDensity = min_density;

            // get lines, and apply preprocessing to braces at this stage if needed
            if(braces_preprocessing) {
                m_method1Lines = M_filterBraceLines(method_1.getLines());
                m_method2Lines = M_filterBraceLines(method_2.getLines());
            } else {
                m_method1Lines = method_1.getLines();
                m_method2Lines = method_2.getLines();
            }

            // initialise arrays of matches, all initially set to null
            m_method1Matched = new EnumCloneType[m_method1Lines.size()];
            m_method2Matched = new EnumCloneType[m_method2Lines.size()];

            // we apply the comparison algorithm
            List<SequenceComparisonAlgos.SequenceElementMatch<EnumCloneType>> matches = matching_algo.compute(m_method1Lines, m_method2Lines, LineMatching::M_compareLines);

            // for each possible match that was found by the algorithm, try to update the match arrays
            for (SequenceComparisonAlgos.SequenceElementMatch<EnumCloneType> match : matches) {
                // we take "match" here, since the algo may both give a single element a Type-1 or Type-2 match. We will select the Type-1 match in such a case.
                m_method1Matched[match.idx_1] = EnumCloneType.max(m_method1Matched[match.idx_1], match.eq_type);
                m_method2Matched[match.idx_2] = EnumCloneType.max(m_method2Matched[match.idx_2], match.eq_type);
            }

            // process braces at this stage if needed
            if(braces_postprocessing) {
                M_filterBracketMatches(m_method1Lines, m_method1Matched);
                M_filterBracketMatches(m_method2Lines, m_method2Matched);
            }

            // fill in any gaps with Type-3
            IMethodMatching.fillInType3Matches(m_method1Matched);
            IMethodMatching.fillInType3Matches(m_method2Matched);

        } catch(IOException | ParseProblemException e) {
            throw new MatchingException(e);
        }
    }

    /**
     * Filter all "}" lines.
     */
    private static List<Line> M_filterBraceLines(List<Line> lines) {
        return lines.stream().filter(x -> !x.getLineContent().equalsIgnoreCase("}") && !x.getLineContent().equalsIgnoreCase("{")).collect(Collectors.toList());
    }

    /**
     * Compare the two lines and determine if there is a match between the lines.
     */
    private static EnumCloneType M_compareLines(Line line_A, Line line_B) {
        // get tokens
        List<Token> tokens_A = line_A.getTokens();
        List<Token> tokens_B = line_B.getTokens();

        // remove final keywords since it may prevent clone detection
        tokens_A.removeIf(t -> t.getContents().equals("final"));
        tokens_B.removeIf(t -> t.getContents().equals("final"));

        // tokens don't have equal length => no match
        if(tokens_A.size() != tokens_B.size()) {
            return null;
        }

        // keeps track of the currently best-possible type of match
        // note that if both lines are empty, this will be matched as type 1
        EnumCloneType current = EnumCloneType.TYPE_1;

        // compare the lines token by token
        for(int i = 0; i < line_A.getTokens().size(); i++) {
            Token A = tokens_A.get(i);
            Token B = tokens_B.get(i);

            // compare the token, and see how strictly we can match them
            current = EnumCloneType.min(current, Token.compareTokens(A, B, false));

            // no match between the lines
            if(current == null) {
                return null;
            }
        }

        return current;
    }

    /**
     * Detect matched lines that solely consist of '}'. All such lines that are not directly or indirectly adjacent to a non-separator line are removed.
     * This is so that the '}' character can not introduce matches on its own, so this is a form of noise-reduction.
     *
     * Result will be stored in the "line_matches" parameter.
     */
    private static void M_filterBracketMatches(List<Line> lines, EnumCloneType[] line_matches) {
        // lines that will be deleted in the second pass
        boolean[] marked_for_deletion = new boolean[lines.size()];

        // keeps track of whether we are inside the clone segment or not
        boolean in_clone_segment = false;

        for (int i = 0; i < lines.size(); i++) {

            // we've encountered a non-match => we have left a clone segment
            if(line_matches[i] == null) {
                in_clone_segment = false;

                // this is a null-element, there is nothing left to do
                continue;
            }

            // we've encountered a matched line that is not a bracket => we have entered a clone segment
            if(!lines.get(i).getLineContent().equals("}")) { // it is also true that "line_matches[i] != null"
                in_clone_segment = true;

                // we are only interested in removing braces from outside the clone segment
                continue;
            }

            // from now on we have only matched lines that are equal to "}"

            // if we are outside of a clone segment and encounter a '}' => mark for deletion
            if(!in_clone_segment) {
                marked_for_deletion[i] = true;
            }
        }

        in_clone_segment = false;
        // we now go in reverse so that we can mark brackets as matched if they precede a matched statement
        for (int i = lines.size() - 1; i >= 0; i--) {

            // we've encountered a non-match => we have left a clon²e segment
            if(line_matches[i] == null) {
                in_clone_segment = false;
                continue;
            }

            // we've encountered a matched line that is not a bracket => we have entered a clone segment
            if(!lines.get(i).getLineContent().equals("}")) {
                in_clone_segment = true;
                continue;
            }

            // now we have only matched lines that are equal to "}"

            // if we are outside of a clone segment and encounter a '}' and it is marked for deletion => delete
            if(!in_clone_segment && marked_for_deletion[i]) {
                line_matches[i] = null;
            }
        }
    }

    @Override
    public EnumCloneType classify()
    {
        // classify each method separately
        EnumCloneType method1_type = IMethodMatching.classifyMethod(m_method1Matched, m_minSize, m_minDensity);
        EnumCloneType method2_type = IMethodMatching.classifyMethod(m_method2Matched, m_minSize, m_minDensity);

        // take least strict type
        return EnumCloneType.min(method1_type, method2_type);
    }

    @Override
    public void writeMatchedMethod1(BiConsumer<String, Color> writer)
    {
        M_writeMethod(writer, m_method1Lines, m_method1Matched);
    }

    @Override
    public void writeMatchedMethod2(BiConsumer<String, Color> writer)
    {
        M_writeMethod(writer, m_method2Lines, m_method2Matched);
    }

    /**
     * Helper method to write the specified lines, with the specified matches, to the specified writer.
     *
     * @param writer A function that will write lines to an output.
     * @param lines The lines that will be displayed.
     * @param matches An array that contains an entry for each line, and determines how that line was matched.
     */
    private static void M_writeMethod(BiConsumer<String, Color> writer, List<Line> lines, EnumCloneType[] matches) {
        // iterate over all lines
        for(int i = 0; i < lines.size(); i++) {
            Color color = Color.WHITE;

            // Type-1 is green, Type-2 is yellow, Type-3 is pink.
            if(matches[i] == EnumCloneType.TYPE_1) {
                color = Color.GREEN;
            } else if(matches[i] == EnumCloneType.TYPE_2) {
                color = Color.YELLOW;
            } else if(matches[i] == EnumCloneType.TYPE_3) {
                color = Color.PINK;
            }

            writer.accept(lines.get(i).getLineContent() + "\n", color);
        }
    }

}
