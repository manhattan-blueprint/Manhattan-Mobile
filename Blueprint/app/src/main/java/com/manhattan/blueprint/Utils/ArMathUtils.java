package com.manhattan.blueprint.Utils;

import android.util.Log;

public class ArMathUtils {

    /* Area of ∆ABC using the shoelace formula:
     *                    | x1 y1 1 |
     * Area(∆ABC) = 1/2 • | x2 y2 1 |
     *                    | x3 y3 1 |
    */
    private static int area(int[] A, int[] B, int[] C) {
        return Math.abs( (A[0] * B[1] + A[1] * C[0] + B[0] * C[1] ) -
                (C[0] * B[1] + A[1] * B[0] + C[1] * A[0] ) ) / 2;
    }

    public static boolean outOfBounds(int[] P, int[] A, int[] B, int[] C, int[] D, int width, int height) {
        int PAB = area(P, A, B);
        int PBC = area(P, B, C);
        int PCD = area(P, C, D);
        int PDA = area(P, D, A);
        int totalArea = PAB + PBC + PCD + PDA;
        int rectArea = width * height;
        return totalArea > rectArea + 2000;
    }

    public static double getAngleError(float currX, float currY, float prevX, float prevY, float rotation) {
        double angle = Math.atan2(prevY - currY, currX - prevX) * 180 / Math.PI;
        if (angle < 0) {
            angle = 180 + angle;
        }
        double diff;
        diff = Math.abs(angle - (90 - rotation));
        diff = Math.min(diff, 180 - diff);
        return diff;
    }
}
