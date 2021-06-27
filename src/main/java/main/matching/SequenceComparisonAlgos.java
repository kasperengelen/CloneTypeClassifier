package main.matching;

import java.util.*;
import java.util.function.BiFunction;

/**
 * Algorithms that compare two sequences.
 */
public class SequenceComparisonAlgos
{
    /**
     * Interface for implementations of sequence comparison algorithms.
     */
    @FunctionalInterface
    public interface IMatchingAlgo<ElemType, EqType> {
        List<SequenceElementMatch<EqType>> compute(List<ElemType> seq1, List<ElemType> seq2, BiFunction<ElemType, ElemType, EqType> eq_predicate);
    }

    /**
     * Represents a match between two sequences.
     * @param <EqType> The type that contains information about the match between the elements.
     */
    public static class SequenceElementMatch<EqType> {
        public final int idx_1;
        public final int idx_2;
        public final EqType eq_type;

        /**
         * Constructor.
         *
         * @param idx_1 The index of the element of the first sequence.
         * @param idx_2 The index of the element of the second sequence.
         * @param eq_type The object that contains information about the match between the two elements.
         */
        public SequenceElementMatch(int idx_1, int idx_2, EqType eq_type)
        {
            this.idx_1 = idx_1;
            this.idx_2 = idx_2;
            this.eq_type = eq_type;
        }
    }

    /**
     * A naive comparison algorithm that tries to match any element of the first sequence with any element of the second sequence. Quadratic complexity.
     *
     * @param seq1 The first sequence.
     * @param seq2 The second sequence.
     * @param eq_predicate A function that takes one element of each sequence and returns null if there is no match, or returns an object if there is a match. The returned object must contain extra information about the match.
     * @param <ElemType> The type of the elements of the sequence.
     * @param <EqType> The type of the object that contains information about a match between two elements.
     *
     * @return A list of objects, each of which describe a match between two elements. Note that these matches may form a many-to-many mapping.
     */
    public static <ElemType, EqType> List<SequenceElementMatch<EqType>> computeNaiveMatch(List<ElemType> seq1, List<ElemType> seq2, BiFunction<ElemType, ElemType, EqType> eq_predicate)
    {
        List<SequenceElementMatch<EqType>> retval = new ArrayList<>();

        // iterate over all pairs of elements (i,j)
        for (int i = 0; i < seq1.size(); i++) {
            for (int j = 0; j < seq2.size(); j++) {
                // check if elements are matched, produce information about the match
                EqType eq = eq_predicate.apply(seq1.get(i), seq2.get(j));

                // there was a match
                if(eq != null) {
                    retval.add(new SequenceElementMatch<>(i, j, eq));
                }
            }
        }

        return retval;
    }

    /**
     * Represents a cell in the LCS matrix.
     */
    private static class LCSCell<T> {
        private final LCSCell<T> m_prev;
        private final SequenceElementMatch<T> m_elem;
        private final int m_length;

        /**
         * Constructor.
         *
         * @param new_match Information about the match.
         * @param prev Link to the previous cell.
         */
        public LCSCell(SequenceElementMatch<T> new_match, LCSCell<T> prev) {
            m_prev = prev;
            m_elem = new_match;

            // increment length of the matched subsequence by one
            if(m_prev != null) {
                m_length = m_prev.m_length + 1;
            } else {
                m_length = 1;
            }
        }

        /**
         * Take the maximal match of two matches. The cell with the longest match will be returned.
         */
        public static <X> LCSCell<X> max(LCSCell<X> a, LCSCell<X> b) {
            if(a == null) {
                return b;
            }

            if(b == null) {
                return a;
            }

            if(a.m_length >= b.m_length) {
                return a;
            } else {
                return b;
            }
        }

        /**
         * Retrieve the longest common subsequence. The sequence that has been matched up until this cell will be returned.
         */
        public List<SequenceElementMatch<T>> getLCS() {
            // prepare return value, which is a sequence of cells
            SequenceElementMatch<T>[] retval = new SequenceElementMatch[m_length];

            // start at the last element
            int i = m_length - 1;
            LCSCell<T> cur = this;

            // travel from current to the beginning
            while(cur != null) {
                // add visited element
                retval[i] = cur.m_elem;

                // go to previous element
                cur = cur.m_prev;
                i--;
            }

            return Arrays.asList(retval);
        }
    }

    /**
     * Compute the LCS of the two sequences. The return type is a list of matches between the two sequences.
     *
     * @param seq1 The first sequence.
     * @param seq2 The second sequence.
     * @param eq_predicate A function that takes one element of each sequence and returns null if there is no match, or returns an object if there is a match. The returned object must contain extra information about the match.
     * @param <ElemType> The type of the elements of the sequence.
     * @param <EqType> The type of the object that contains information about a match between two elements.
     *
     * @return A list of objects, each of which describe a match between two elements. The matches form a solution to the LCS problem.
     */
    public static <ElemType, EqType> List<SequenceElementMatch<EqType>> computeLCS(List<ElemType> seq1, List<ElemType> seq2, BiFunction<ElemType, ElemType, EqType> eq_predicate) {
        LCSCell<EqType>[][] matrix = new LCSCell[seq1.size()][seq2.size()];

        if(seq1.size() == 0 || seq2.size() == 0) {
            return List.of();
        }

        // fill the LCS matrix
        for(int i = 0; i < seq1.size(); i++) {
            for (int j = 0; j < seq2.size(); j++) {
                // compare elements
                EqType eq = eq_predicate.apply(seq1.get(i), seq2.get(j));

                if(eq != null) {
                    // there was a match and we add it to the matrix

                    SequenceElementMatch<EqType> match = new SequenceElementMatch<>(i, j, eq);

                    // if we are at the edge of the matrix, there is no "previous"
                    LCSCell<EqType> prev = (i == 0 || j == 0) ? null : matrix[i-1][j-1];

                    // set matrix cell
                    matrix[i][j] = new LCSCell<>(match, prev);
                } else {
                    // there was no match, so we just make a link to the previous cell

                    // if we are at the edge of the matrix, there is no "previous"
                    LCSCell<EqType> top  = (i == 0) ? null : matrix[i - 1][j];
                    LCSCell<EqType> left = (j == 0) ? null : matrix[i][j - 1];

                    // set matrix cell
                    matrix[i][j] = LCSCell.max(top, left);
                }
            }
        }

        // if there were not matches, the result is "null"
        if(matrix[seq1.size()-1][seq2.size()-1] == null) {
            return List.of();
        } else {
            return matrix[seq1.size() - 1][seq2.size() - 1].getLCS();
        }
    }
}
