package main.evaluation;

import main.clone.EnumCloneType;

import java.util.List;

/**
 * Confusion matrix for multiple classes
 */
public class MultiClassConfusionMatrix
{
    // size of the matrix
    private final int m_dim;

    // the different clone types that are represented in the matrix.
    private final List<EnumCloneType> m_classes;

    // confusion matrix. Rows=ground truth, Cols=predictions
    private final int[][] m_matrix;

    /**
     * Constructor.
     *
     * @param classes The different classes that have an entry in the matrix.
     */
    public MultiClassConfusionMatrix(List<EnumCloneType> classes)
    {
        m_classes = classes;
        m_dim = m_classes.size();
        m_matrix = new int[m_dim][m_dim];
    }

    /**
     * Add a prediction result to the matrix.
     * @param predicted The class that was prediction.
     * @param truth The ground truth.
     */
    public void addPrediction(EnumCloneType predicted, EnumCloneType truth)
    {
        // increment the array entry
        m_matrix[truth.ordinal()][predicted.ordinal()]++;
    }

    /**
     * Retrieve a binary confusion matrix for the specified class.
     */
    public BinaryConfusionMatrix getBinaryMatrix(EnumCloneType cls)
    {
        // the number that will be used to access the matrix entries associated with this class.
        int cls_nr = m_classes.indexOf(cls);

        int tp = 0;
        int fp = 0;
        int fn = 0;
        int tn = 0;

        for (int row = 0; row < m_dim; row++) {
            for (int col = 0; col < m_dim; col++) {
                boolean in_row = (row == cls_nr);
                boolean in_col = (col == cls_nr);
                int cell = m_matrix[row][col];

                if (in_row) {
                    if (in_col) {
                        // TP
                        tp += cell;
                    } else {
                        // FN
                        fn += cell;
                    }
                } else {
                    if (in_col) {
                        // FP
                        fp += cell;
                    } else {
                        // TN
                        tn += cell;
                    }
                }

            }
        }

        return new BinaryConfusionMatrix(tp, fp, fn, tn);
    }
}
