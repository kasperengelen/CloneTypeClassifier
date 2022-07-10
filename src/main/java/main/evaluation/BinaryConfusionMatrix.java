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

package main.evaluation;

/**
 * A binary confusion matrix that provides a number of metrics.
 */
public class BinaryConfusionMatrix
{
    private int m_TP = 0;
    private int m_TN = 0;
    private int m_FP = 0;
    private int m_FN = 0;

    /**
     * Constructor.
     *
     * @param tp The number of true positives.
     * @param fp The number of false positives.
     * @param fn The number of false negatives.
     * @param tn The number of true negatives.
     */
    public BinaryConfusionMatrix(int tp, int fp, int fn, int tn)
    {
        m_TP = tp;
        m_FP = fp;
        m_FN = fn;
        m_TN = tn;
    }

    /**
     * Retrieve the recall.
     */
    public float getRecall()
    {
        return m_TP / (float) (m_TP + m_FN);
    }

    /**
     * Retrieve the precision.
     */
    public float getPrecision()
    {
        return m_TP / (float) (m_TP + m_FP);
    }

    /**
     * Retrieve the accuracy.
     */
    public float getAccuracy()
    {
        return (m_TP + m_TN) / (float) (m_TP + m_TN + m_FP + m_FN);
    }

    /**
     * Retrieve the F1 score.
     */
    public float getF1()
    {
        float prec = this.getPrecision();
        float recall = this.getRecall();

        return 2 * (prec * recall) / (prec + recall);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("TP=").append(this.m_TP).append("\n");
        sb.append("FP=").append(this.m_FP).append("\n");
        sb.append("FN=").append(this.m_FN).append("\n");
        sb.append("TN=").append(this.m_TN).append("\n");
        sb.append("Prec=").append(this.getPrecision()).append("\n");
        sb.append("Rec=").append(this.getRecall()).append("\n");
        sb.append("Acc=").append(this.getAccuracy()).append("\n");
        sb.append("F1=").append(this.getF1()).append("\n");

        return sb.toString();
    }
}
