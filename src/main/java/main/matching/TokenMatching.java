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
import main.method.Method;
import main.method.Token;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Matching between two methods based on the tokens of the methods. Here, the comparison units are single tokens.
 */
public class TokenMatching implements IMethodMatching
{
    // tokens per method
    private final List<Token> m_method1Tokens;
    private final List<Token> m_method2Tokens;

    // the matches for each token
    private final EnumCloneType[] m_method1Matched;
    private final EnumCloneType[] m_method2Matched;

    private final int m_minSize;
    private final float m_minDensity;

    /**
     * Constructor.
     *
     * @param method_1 The first method.
     * @param method_2 The second method.
     * @param matching_algo The algorithm that will be used to match the tokens of the first method with lines of the second method.
     * @param min_size The minimum size of the clone segment. The size of such a segment is the amount of lines between the first and last line of the segment. Set to 0 to ignore.
     * @param min_density The minimum density of the clone segment. This is the number of Type-1 or Type-2 matched lines divided by the segment size. Set to 0.0 to ignore.
     *
     * @throws MatchingException In case there is an IO error, or a parsing error.
     */
    public TokenMatching(Method method_1, Method method_2, SequenceComparisonAlgos.IMatchingAlgo<Token, EnumCloneType> matching_algo, int min_size, float min_density) throws MatchingException
    {
        try {
            m_minSize = min_size;
            m_minDensity = min_density;

            // get tokens
            m_method1Tokens = method_1.getTokens();
            m_method2Tokens = method_2.getTokens();

            // initialise arrays of matches, all initially set to null
            m_method1Matched = new EnumCloneType[m_method1Tokens.size()];
            m_method2Matched = new EnumCloneType[m_method2Tokens.size()];

            // apply comparison algorithm
            BiFunction<Token, Token, EnumCloneType> eq_predicate = (token1, token2) -> Token.compareTokens(token1, token2, false);
            List<SequenceComparisonAlgos.SequenceElementMatch<EnumCloneType>> matches = matching_algo.compute(m_method1Tokens, m_method2Tokens, eq_predicate);

            // for each possible match that was found by the algorithm, try to update the match arrays
            for (SequenceComparisonAlgos.SequenceElementMatch<EnumCloneType> match : matches) {
                // we take "match" here, since the algo may both give a single element a Type-1 or Type-2 match. We will select the Type-1 match in such a case.
                m_method1Matched[match.idx_1] = EnumCloneType.max(m_method1Matched[match.idx_1], match.eq_type);
                m_method2Matched[match.idx_2] = EnumCloneType.max(m_method2Matched[match.idx_2], match.eq_type);
            }

            // fill in any gaps with Type-3
            IMethodMatching.fillInType3Matches(m_method1Matched);
            IMethodMatching.fillInType3Matches(m_method2Matched);

        } catch(IOException | ParseProblemException e) {
            throw new MatchingException(e);
        }
    }

    @Override
    public EnumCloneType classify()
    {
        // classify each method separately
        EnumCloneType method1_type = IMethodMatching.classifyMethod(m_method1Matched, m_minSize, m_minDensity);
        EnumCloneType method2_type = IMethodMatching.classifyMethod(m_method2Matched, m_minSize, m_minDensity);

        // take minimum
        return EnumCloneType.min(method1_type, method2_type);
    }

    @Override
    public void writeMatchedMethod1(BiConsumer<String, Color> writer)
    {
        M_writeMethod(writer, m_method1Tokens, m_method1Matched);
    }

    @Override
    public void writeMatchedMethod2(BiConsumer<String, Color> writer)
    {
        M_writeMethod(writer, m_method2Tokens, m_method2Matched);
    }

    /**
     * Helper method to write the specified tokens, with the specified matches, to the specified writer.
     *
     * @param writer A function that will write lines to an output.
     * @param tokens The tokens that will be displayed.
     * @param matches An array that contains an entry for each line, and determines how that line was matched.
     */
    private static void M_writeMethod(BiConsumer<String, Color> writer, List<Token> tokens, EnumCloneType[] matches) {
        // iterate over tokens
        for(int i = 0; i < tokens.size(); i++) {
            Color color = Color.WHITE;

            // Type-1 is green, Type-2 is yellow, Type-3 is pink.
            if(matches[i] == EnumCloneType.TYPE_1) {
                color = Color.GREEN;
            } else if(matches[i] == EnumCloneType.TYPE_2) {
                color = Color.YELLOW;
            } else if(matches[i] == EnumCloneType.TYPE_3) {
                color = Color.PINK;
            }

            // "Category::TokenContent"
            writer.accept(tokens.get(i).getCategory().name() + "::" + tokens.get(i).getContents() + "\n", color);
        }
    }
}
