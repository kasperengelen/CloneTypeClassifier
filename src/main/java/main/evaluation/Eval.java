package main.evaluation;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.Problem;
import main.Main;
import main.clone.ClonePair;
import main.clone.EnumCloneType;
import main.matching.IMatcher;
import main.matching.IMethodMatching;
import main.matching.MatchingException;
import main.method.Method;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Evaluation utility for clone type classifiers. This allows for evaluating matching algorithms using a specified dataset.
 */
public class Eval
{
    /**
     * Evaluation result.
     * This contains a multi-class confusion matrix, the correctly classified clone pairs, the incorrectly classified clone pairs, and the count of clone pairs
     * that caused an exception.
     */
    public static class Result
    {
        private final MultiClassConfusionMatrix m_confusionMatrix;
        private final List<ClonePair> m_correctPairs;
        private final List<ClonePair> m_misclassifiedPairs;
        private final int m_erroredPairs;

        /**
         *
         * @param confusion_matrix The confusion matrix.
         * @param correct_pairs The clone pairs that were correctly classified.
         * @param misclassified_pairs The clone pairs that were incorrectly classified.
         * @param errored_pairs The number of pairs that could not be classified due to the fact that an exception occurred while classifying.
         */
        public Result(MultiClassConfusionMatrix confusion_matrix, List<ClonePair> correct_pairs, List<ClonePair> misclassified_pairs, int errored_pairs) {
            m_confusionMatrix = confusion_matrix;
            m_correctPairs = correct_pairs;
            m_misclassifiedPairs = misclassified_pairs;
            m_erroredPairs = errored_pairs;
        }

        /**
         * The confusion matrix.
         */
        public MultiClassConfusionMatrix getConfusionMatrix()
        {
            return m_confusionMatrix;
        }

        /**
         * The clone pairs that were correctly classified.
         */
        public List<ClonePair> getCorrectPairs() {
            return m_correctPairs;
        }

        /**
         * The clone pairs that were incorrectly classified.
         */
        public List<ClonePair> getMisclassifiedClonePairs()
        {
            return m_misclassifiedPairs;
        }

        /**
         * The number of pairs that could not be classified due to the fact that an exception occurred while classifying.
         */
        public int getErroredPairs()
        {
            return m_erroredPairs;
        }
    }

    /**
     * Run the evaluation using the specified parameters.
     *
     * @param pairs The clone pairs that will be used to evaluate the classifier.
     * @param num_clones The number of clones, starting from the beginning of the list, that will be considered in the evaluation.
     * @param matcher The {@link IMatcher} that will be used to match pairs of methods.
     * @param print_status Whether or not to print a progress indicator every 100 clones using {@link Main#log(String, Object...)}.
     *
     * @return The result of the evaluation. This will include a confusion matrix, as well as a list of misclassified clone pairs.
     */
    public static Result eval(List<ClonePair> pairs, int num_clones, IMatcher matcher, boolean print_status) throws IOException
    {
        MultiClassConfusionMatrix confusion_matrix = new MultiClassConfusionMatrix(List.of(EnumCloneType.values()));
        List<ClonePair> correct = new ArrayList<>();
        List<ClonePair> misclassified = new ArrayList<>();

        int error_count = 0;

        for (int i = 0; i < Math.min(pairs.size(), num_clones); i++) {
            ClonePair pair = pairs.get(i);

            try {
                IMethodMatching matching = matcher.match(pair.getMethod1(), pair.getMethod2());

                EnumCloneType truth = pair.getManualClassification();
                EnumCloneType pred = matching.classify();

                // add the prediction to the confusion matrix
                confusion_matrix.addPrediction(pred, truth);

                if (pred != truth) {
                    misclassified.add(pair);
                } else {
                    correct.add(pair);
                }

            } catch (MatchingException e) {
                error_count++;

                Main.log("Error when matching pair:");
                Main.log("\tIdx: %d", i);
                Method method1 = pair.getMethod1();
                Main.log("\tSource 1: (%s, %d, %d)", method1.getPath().getPath(), method1.getBegin(), method1.getEnd());
                Method method2 = pair.getMethod2();
                Main.log("\tSource 2: (%s, %d, %d)", method2.getPath().getPath(), method2.getBegin(), method2.getEnd());

                if(e.getCause() instanceof ParseProblemException) {
                    for (Problem problem : ((ParseProblemException) e.getCause()).getProblems()) {
                        Main.log("Reason: '%s'", problem.getMessage());
                    }
                } else {
                    Main.log("Reason: '%s'", e.getMessage());
                }

            }

            // print progress
            if(print_status && i % 100 == 0) {
                Main.log("Current=%d/%d", i, num_clones);
            }
        }

        return new Result(confusion_matrix, correct, misclassified, error_count);
    }
}
