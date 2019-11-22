package com.pennsim;

/**
 * Class designed to emulate the digital circuit Branch Predictor
 *
 * If you would like to learn more about how a Branch Predictors work
 * you cna google it or check out this transcript of a talk on it at
 * https://danluu.com/branch-prediction/
 */
public class BranchPredictor {

    private static final int TAG = 0;
    private static final int PREDICTION = 1;
    private int[][] predictor;
    private int indexMask = 0;

    public BranchPredictor() { }

    BranchPredictor(int value) {
        int tag = TAG;
        int localValue = value;
        byte prediction = PREDICTION;

        for (int i = 0; i < 16; ++i) {
            if ((localValue & prediction) == prediction) {
                ++tag;

                for (int j = 0; j < i; ++j) {
                    this.indexMask <<= 1;
                    this.indexMask |= 1;
                }
            }

            localValue >>= 1;
        }

        if (tag != PREDICTION) {
            throw new IllegalArgumentException("Branch predictor size must be a power of two.");
        } else {
            this.predictor = new int[value][2];
        }
    }

    int getPredictedPC(int value) {
        int maskedValue = value & this.indexMask;
        int predictedValue = value + 1;
        if (this.predictor[maskedValue][TAG] == value) {
            predictedValue = this.predictor[maskedValue][PREDICTION];
        }

        return predictedValue;
    }

    void update(int tagValue, int predictionValue) {
        this.predictor[tagValue & this.indexMask][TAG] = tagValue;
        this.predictor[tagValue & this.indexMask][PREDICTION] = predictionValue;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < this.predictor.length; ++i) {
            builder.append(i)
                    .append(":")
                    .append(" tag: ")
                    .append(this.predictor[i][TAG])
                    .append(" pred: ")
                    .append(this.predictor[i][PREDICTION]);
        }

        return builder.toString();
    }

    /**
     * Reset the predictor
     */
    public void reset() {
        for (int i = 0; i < this.predictor.length; ++i) {
            this.predictor[i][TAG] = 0;
            this.predictor[i][PREDICTION] = 0;
        }

    }
}
