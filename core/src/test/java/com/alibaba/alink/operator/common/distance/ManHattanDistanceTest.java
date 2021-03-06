package com.alibaba.alink.operator.common.distance;

import com.alibaba.alink.common.linalg.DenseMatrix;
import com.alibaba.alink.common.linalg.DenseVector;
import com.alibaba.alink.common.linalg.SparseVector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.types.Row;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for ManHattanDistance
 */
public class ManHattanDistanceTest {
    private ManHattanDistance distance = new ManHattanDistance();
    private DenseVector denseVector1 = new DenseVector(new double[] {1, 2, 4, 1, 3});
    private DenseVector denseVector2 = new DenseVector(new double[] {4, 6, 1, 2, 4});
    private SparseVector sparseVector1 = new SparseVector(5, new int[] {1, 3}, new double[] {0.1, 0.4});
    private SparseVector sparseVector2 = new SparseVector(5, new int[] {2, 3}, new double[] {0.4, 0.1});

    @Test
    public void testContinuousDistance() {
        Assert.assertEquals(distance.calc(denseVector1, denseVector2), 12.0, 0.01);
        Assert.assertEquals(distance.calc(denseVector1.getData(), denseVector2.getData()), 12.0, 0.01);
        Assert.assertEquals(distance.calc(denseVector1, sparseVector1), 10.5, 0.01);
        Assert.assertEquals(distance.calc(sparseVector1, sparseVector2), 0.8, 0.01);
        Assert.assertEquals(distance.calc(sparseVector1, denseVector2), 16.5, 0.01);
    }

    private FastDistanceMatrixData initMatrixData() {
        List<Row> dense = new ArrayList<>();
        dense.add(Row.of(0, denseVector1));
        dense.add(Row.of(1, denseVector2));
        return (FastDistanceMatrixData)distance.prepareMatrixData(dense, 1).get(0);
    }

    @Test
    public void testUpdateLabel() {
        Assert.assertNull(distance.prepareVectorData(Tuple2.of(denseVector1, null)).getLabel());
        Assert.assertNull(initMatrixData().getLabel());
    }

    @Test
    public void testCalDistanceVecVec() {
        DenseMatrix denseResult = distance.calc(distance.prepareVectorData(Tuple2.of(denseVector1, null)),
            (FastDistanceData)distance.prepareVectorData(Tuple2.of(denseVector2, null)));
        Assert.assertEquals(denseResult.get(0, 0), 12.0, 0.01);

        DenseMatrix sparseResult = distance.calc(distance.prepareVectorData(Tuple2.of(sparseVector1, null)),
            (FastDistanceData)distance.prepareVectorData(Tuple2.of(

                sparseVector2, null)));
        Assert.assertEquals(sparseResult.get(0, 0), 0.8, 0.01);
    }

    @Test
    public void testCalDistanceMatrixMatrix() {
        FastDistanceMatrixData matrixData = initMatrixData();
        DenseMatrix res = distance.calc(matrixData, matrixData);
        double[] expect = new double[] {0.0, 12.0, 12.0, 0.0};
        double[] predict = res.getData();
        for (int i = 0; i < expect.length; i++) {
            Assert.assertEquals(expect[i], predict[i], 0.01);
        }
    }

    @Test
    public void testCalDistanceVecMatrix() {
        FastDistanceMatrixData matrixData = initMatrixData();
        FastDistanceVectorData vectorData = distance.prepareVectorData(Row.of(0, sparseVector1), 1);

        double[] predict = distance.calc(matrixData, vectorData).getData();
        double[] expect = new double[] {10.5, 16.5};
        for (int i = 0; i < expect.length; i++) {
            Assert.assertEquals(expect[i], predict[i], 0.01);
        }

        predict = distance.calc(vectorData, matrixData).getData();
        for (int i = 0; i < expect.length; i++) {
            Assert.assertEquals(expect[i], predict[i], 0.01);
        }
    }



}