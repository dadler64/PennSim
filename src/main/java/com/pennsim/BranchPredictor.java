package com.pennsim;

public class BranchPredictor {

    private static final int TAG = 0;
    private static final int PREDICTION = 1;
    private int[][] predictor;
    private int indexMask = 0;
    private Machine mac;

    public BranchPredictor() {
    }

    public BranchPredictor(Machine var1, int var2) {
        this.mac = var1;
        int var3 = 0;
        int var4 = var2;
        byte var5 = 1;

        for (int var6 = 0; var6 < 16; ++var6) {
            if ((var4 & var5) == var5) {
                ++var3;

                for (int var7 = 0; var7 < var6; ++var7) {
                    this.indexMask <<= 1;
                    this.indexMask |= 1;
                }
            }

            var4 >>= 1;
        }

        if (var3 != 1) {
            throw new IllegalArgumentException("Branch predictor size must be a power of two.");
        } else {
            this.predictor = new int[var2][2];
        }
    }

    public int getPredictedPC(int var1) {
        int var2 = var1 & this.indexMask;
        int var3 = var1 + 1;
        if (this.predictor[var2][0] == var1) {
            var3 = this.predictor[var2][1];
        }

        return var3;
    }

    public void update(int var1, int var2) {
        this.predictor[var1 & this.indexMask][0] = var1;
        this.predictor[var1 & this.indexMask][1] = var2;
    }

    public String toString() {
        String var1 = "";

        for (int var2 = 0; var2 < this.predictor.length; ++var2) {
            var1 = var1 + var2 + ":" + " tag: " + this.predictor[var2][0] + " pred: "
                    + this.predictor[var2][1];
        }

        return var1;
    }

    public void reset() {
        for (int var1 = 0; var1 < this.predictor.length; ++var1) {
            this.predictor[var1][0] = 0;
            this.predictor[var1][1] = 0;
        }

    }
}
