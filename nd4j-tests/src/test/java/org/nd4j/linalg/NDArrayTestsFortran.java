/*
 *
 *  * Copyright 2015 Skymind,Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 *
 */

package org.nd4j.linalg;



import java.io.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ndarray.LinearViewNDArray;
import org.nd4j.linalg.api.ops.impl.transforms.comparison.Eps;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.factory.Nd4jBackend;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.jcublas.CublasPointer;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.nd4j.linalg.util.Shape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * NDArrayTests for fortran ordering
 *
 * @author Adam Gibson
 */
public  class NDArrayTestsFortran  extends BaseNd4jTest {
    private static Logger log = LoggerFactory.getLogger(NDArrayTestsFortran.class);

    public NDArrayTestsFortran() {
    }

    public NDArrayTestsFortran(String name) {
        super(name);
    }

    public NDArrayTestsFortran(String name, Nd4jBackend backend) {
        super(name, backend);
    }

    public NDArrayTestsFortran(Nd4jBackend backend) {
        super(backend);
    }

    @Before
    public void before() {
        super.before();
    }

    @After
    public void after() {
        super.after();
    }



    @Test
    public void testScalarOps() throws Exception {
        INDArray n = Nd4j.create(Nd4j.ones(27).data(), new int[]{3, 3, 3});
        assertEquals(27d, n.length(), 1e-1);
        n.checkDimensions(n.addi(Nd4j.scalar(1d)));
        n.checkDimensions(n.subi(Nd4j.scalar(1.0d)));
        n.checkDimensions(n.muli(Nd4j.scalar(1.0d)));
        n.checkDimensions(n.divi(Nd4j.scalar(1.0d)));

        n = Nd4j.create(Nd4j.ones(27).data(), new int[]{3, 3, 3});
        assertEquals(27, n.sum(Integer.MAX_VALUE).getDouble(0), 1e-1);
        INDArray a = n.slice(2);
        assertEquals(true, Arrays.equals(new int[]{3, 3}, a.shape()));

    }

    @Test
    public void testPrepend() {
        INDArray appendTo = Nd4j.ones(3, 3);
        INDArray ret = Nd4j.append(appendTo, 3, 1, -1);
        assertArrayEquals(new int[]{3, 6}, ret.shape());

        INDArray linspace = Nd4j.linspace(1,4,4).reshape(2,2);
        INDArray assertion = Nd4j.create(new double[][]{
                {1, 1, 1, 1, 3},
                {1, 1, 1, 2, 4}
        });

        INDArray prepend = Nd4j.prepend(linspace, 3, 1.0, -1);
        assertEquals(assertion, prepend);

    }

    @Test
    public void testTensorAlongDimension() {
        INDArray twoTwoByThree = Nd4j.linspace(1,12,12).reshape(2, 2, 3);
        INDArray tensors = twoTwoByThree.tensorAlongDimension(0, 1, 2);
        assertArrayEquals(new int[]{2,3},tensors.shape());
        assertEquals(2, twoTwoByThree.tensorssAlongDimension(1, 2));
        INDArray firstTensor = Nd4j.create(new double[][]{{1,3},{2,4}});
        assertEquals(firstTensor,twoTwoByThree.tensorAlongDimension(0,0,1));
        INDArray secondTensor = Nd4j.create(new double[][]{{5, 7}, {6, 8}});
        assertEquals(secondTensor,twoTwoByThree.tensorAlongDimension(1,0,1));

    }


    @Test
    public void testMultiDimSum() {
        INDArray assertion = Nd4j.create(new double[]{10, 26, 42});
        INDArray twoTwoByThree = Nd4j.linspace(1,12,12).reshape(2, 2, 3);
        INDArray multiSum = twoTwoByThree.sum(0, 1);
        assertEquals(assertion,multiSum);
    }



    @Test
    public void testAppend() {
        INDArray appendTo = Nd4j.ones(3, 3);
        INDArray ret = Nd4j.append(appendTo, 3, 1, -1);
        assertArrayEquals(new int[]{3, 6}, ret.shape());

        INDArray linspace = Nd4j.linspace(1,4,4).reshape(2, 2);
        INDArray otherAppend = Nd4j.append(linspace, 3, 1.0, -1);
        INDArray assertion = Nd4j.create(new double[][]{
                {1, 3, 1, 1, 1},
                {2, 4, 1, 1, 1}
        });

        assertEquals(assertion, otherAppend);


    }


    @Test
    public void testConcatMatrices() {
        INDArray a = Nd4j.linspace(1,4,4).reshape(2,2);
        INDArray b = a.dup();


        INDArray concat1 = Nd4j.concat(1, a, b);
        INDArray oneAssertion = Nd4j.create(new double[][]{{1, 3, 1, 3}, {2, 4, 2, 4}});
        assertEquals(oneAssertion,concat1);

        INDArray concat = Nd4j.concat(0, a, b);
        INDArray zeroAssertion = Nd4j.create(new double[][]{{1, 3}, {2, 4}, {1, 3}, {2, 4}});
        assertEquals(zeroAssertion, concat);
    }

    @Test
    public void testPad() {
        INDArray start = Nd4j.linspace(1,9,9).reshape(3, 3);
        INDArray ret = Nd4j.pad(start, new int[]{5, 5}, Nd4j.PadMode.CONSTANT);
        double[][] data = new double[][]
                {{ 0,0,0,0,0,0,0,0,0,0,0,0,0.},
                        { 0,0,0,0,0,0,0,0,0,0,0,0,0.},
                        { 0,0,0,0,0,0,0,0,0,0,0,0,0.},
                        { 0,0,0,0,0,0,0,0,0,0,0,0,0.},
                        { 0,0,0,0,0,0,0,0,0,0,0,0,0.},
                        { 0,0,0,0,0,1,4,7,0,0,0,0,0.},
                        { 0,0,0,0,0,2,5,8,0,0,0,0,0.},
                        { 0,0,0,0,0,3,6,9,0,0,0,0,0.},
                        { 0,0,0,0,0,0,0,0,0,0,0,0,0.},
                        { 0,0,0,0,0,0,0,0,0,0,0,0,0.},
                        { 0,0,0,0,0,0,0,0,0,0,0,0,0.},
                        { 0,0,0,0,0,0,0,0,0,0,0,0,0.},
                        { 0,0,0,0,0,0,0,0,0,0,0,0,0.}};
        INDArray assertion = Nd4j.create(data);
        assertEquals(assertion, ret);


    }


    @Test
    public void testColumnMmul() {
        DataBuffer data = Nd4j.linspace(1, 10, 18).data();
        INDArray x2 = Nd4j.create(data, new int[]{2,3,3});
        data = Nd4j.linspace(1, 12, 9).data();
        INDArray y2 = Nd4j.create(data, new int[]{3,3});
        INDArray z2 = Nd4j.create(3,2);
        z2.putColumn(0, y2.getColumn(0));
        z2.putColumn(1, y2.getColumn(1));
        INDArray nofOffset = Nd4j.create(3,3);
        nofOffset.assign(x2.slice(0));
        assertEquals(getFailureMessage(),nofOffset,x2.slice(0));

        INDArray slice = x2.slice(0);
        INDArray zeroOffsetResult = slice.mmul(z2);
        INDArray offsetResult = nofOffset.mmul(z2);
        assertEquals(getFailureMessage(),zeroOffsetResult,offsetResult);


        INDArray slice1 = x2.slice(1);
        INDArray noOffset2 = Nd4j.create(slice1.shape());
        noOffset2.assign(slice1);
        assertEquals(getFailureMessage(),slice1,noOffset2);

        INDArray noOffsetResult = noOffset2.mmul(z2);
        INDArray slice1OffsetResult = slice1.mmul(z2);

        assertEquals(getFailureMessage(),noOffsetResult,slice1OffsetResult);



    }


    @Test
    public void testRowVectorGemm() {
        INDArray linspace = Nd4j.linspace(1, 4, 4);
        INDArray other = Nd4j.linspace(1,16,16).reshape(4, 4);
        INDArray result = linspace.mmul(other);
        INDArray assertion = Nd4j.create(new double[]{30., 70., 110., 150.});
        assertEquals(assertion, result);
    }


    @Test
    public void testRepmat() {
        INDArray rowVector = Nd4j.create(1, 4);
        INDArray repmat = rowVector.repmat(4, 4);
        assertTrue(Arrays.equals(new int[]{4, 4}, repmat.shape()));
    }

    @Test
    public void testReadWrite() throws Exception {
        INDArray write = Nd4j.linspace(1, 4, 4);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        Nd4j.write(write,dos);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        DataInputStream dis = new DataInputStream(bis);
        INDArray read = Nd4j.read(dis);
        assertEquals(write, read);

    }

    @Test
    public void testReadWriteTxt() throws Exception {
        INDArray write = Nd4j.create(5);
        File writeTo = new File(UUID.randomUUID().toString());
        Nd4j.writeTxt(write,writeTo.getAbsolutePath(),"\t");
        INDArray read = Nd4j.readTxt(writeTo.getAbsolutePath());
        assertEquals(write,read);

    }




    @Test
    public void testConcatScalars() {
        INDArray first = Nd4j.arange(0,1).reshape(1,1);
        INDArray second = Nd4j.arange(0,1).reshape(1, 1);
        INDArray firstRet = Nd4j.concat(0, first, second);
        assertTrue(firstRet.isColumnVector());
        INDArray secondRet = Nd4j.concat(1, first, second);
        assertTrue(secondRet.isRowVector());


    }

    @Test
    public void testReadWriteDouble() throws Exception {
        INDArray write = Nd4j.linspace(1, 4, 4);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        Nd4j.write(write, dos);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        DataInputStream dis = new DataInputStream(bis);
        INDArray read = Nd4j.read(dis);
        assertEquals(write, read);

    }



    @Test
    public void testMultiThreading() throws Exception {
        ExecutorService ex = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for(int i = 0; i < 100; i++) {
            ex.execute(new Runnable() {
                @Override
                public void run() {
                    INDArray dot = Nd4j.linspace(1, 8, 8);
                    System.out.println(Transforms.sigmoid(dot));
                }
            });
        }

        ex.shutdown();
        ex.awaitTermination(1, TimeUnit.MINUTES);

    }



    @Test
    public void testBroadCasting() {
        INDArray first = Nd4j.arange(0, 3).reshape(3, 1);
        INDArray ret = first.broadcast(3, 4);
        INDArray testRet = Nd4j.create(new double[][]{
                {0, 0, 0, 0},
                {1, 1, 1, 1},
                {2, 2, 2, 2}
        });
        assertEquals(testRet, ret);
        INDArray r = Nd4j.arange(0, 4).reshape(1, 4);
        INDArray r2 = r.broadcast(4, 4);
        INDArray testR2 = Nd4j.create(new double[][]{
                {0, 1, 2, 3},
                {0, 1, 2, 3},
                {0, 1, 2, 3},
                {0, 1, 2, 3}
        });
        assertEquals(testR2, r2);

    }





    @Test
    public void testLinearViewGetAndPut() throws Exception {
        INDArray test = Nd4j.linspace(1, 4, 4).reshape(2, 2);
        INDArray linear = test.linearView();
        linear.putScalar(2, 6);
        linear.putScalar(3, 7);
        assertEquals(6, linear.getFloat(2), 1e-1);
        assertEquals(7, linear.getFloat(3), 1e-1);
    }



    @Test
    public void testSortWithIndicesDescending() {
        INDArray toSort = Nd4j.linspace(1, 4, 4).reshape(2, 2);
        //indices,data
        INDArray[] sorted = Nd4j.sortWithIndices(toSort.dup(), 1, false);
        INDArray sorted2 = Nd4j.sort(toSort.dup(), 1, false);
        assertEquals(sorted[1], sorted2);
        INDArray shouldIndex = Nd4j.create(new float[]{1, 1, 0, 0}, new int[]{2, 2});
        assertEquals(getFailureMessage(), shouldIndex, sorted[0]);


    }


    @Test
    public void testSortWithIndices() {
        INDArray toSort = Nd4j.linspace(1, 4, 4).reshape(2, 2);
        //indices,data
        INDArray[] sorted = Nd4j.sortWithIndices(toSort.dup(), 1, true);
        INDArray sorted2 = Nd4j.sort(toSort.dup(), 1, true);
        assertEquals(sorted[1], sorted2);
        INDArray shouldIndex = Nd4j.create(new float[]{0, 0, 1, 1}, new int[]{2, 2});
        assertEquals(getFailureMessage(),shouldIndex, sorted[0]);


    }

    @Test
    public void testSwapAxesFortranOrder() {
        INDArray n = Nd4j.create(Nd4j.linspace(1, 30, 30).data(), new int[]{3, 5, 2});
        INDArray slice = n.swapAxes(2, 1);
        INDArray assertion = Nd4j.create(new double[]{1, 4, 7, 10, 13});
        INDArray test = slice.slice(0).slice(0);
        assertEquals(assertion, test);
    }


    @Test
    public void testGetIndicesVector() {
        INDArray line = Nd4j.linspace(1, 4, 4);
        INDArray test = Nd4j.create(new float[]{2, 3});
        INDArray result = line.get(new NDArrayIndex(0), NDArrayIndex.interval(1, 3));
        assertEquals(test, result);
    }


    @Test
    public void testVStackColumn() {
        INDArray linspaced = Nd4j.linspace(1,3,3).reshape(3, 1);
        INDArray stacked = linspaced.dup();
        INDArray assertion = Nd4j.create(new double[]{1, 2, 3, 1, 2, 3}, new int[]{6, 1});
        assertEquals(assertion, Nd4j.vstack(linspaced, stacked));
    }


    @Test
    public void testDimShuffle() {
        INDArray n = Nd4j.linspace(1, 4, 4).reshape(2, 2);
        INDArray twoOneTwo = n.dimShuffle(new Object[]{0, 'x', 1}, new int[]{0, 1}, new boolean[]{false, false});
        assertTrue(Arrays.equals(new int[]{2, 1, 2}, twoOneTwo.shape()));

        INDArray reverse = n.dimShuffle(new Object[]{1, 'x', 0}, new int[]{1, 0}, new boolean[]{false, false});
        assertTrue(Arrays.equals(new int[]{2, 1, 2}, reverse.shape()));

    }

    @Test
    public void testGetVsGetScalar() {
        INDArray a = Nd4j.linspace(1, 4, 4).reshape(2, 2);
        float element = a.getFloat(0, 1);
        double element2 = a.getDouble(0, 1);
        assertEquals(element, element2, 1e-1);
        INDArray a2 = Nd4j.linspace(1, 4, 4).reshape(2, 2);
        float element23 = a2.getFloat(0, 1);
        double element22 = a2.getDouble(0, 1);
        assertEquals(element23, element22, 1e-1);

    }

    @Test
    public void testDivide() {
        INDArray two = Nd4j.create(new float[]{2, 2, 2, 2});
        INDArray div = two.div(two);
        assertEquals(getFailureMessage(),Nd4j.ones(4), div);

        INDArray half = Nd4j.create(new float[]{0.5f, 0.5f, 0.5f, 0.5f}, new int[]{2, 2});
        INDArray divi = Nd4j.create(new float[]{0.3f, 0.6f, 0.9f, 0.1f}, new int[]{2, 2});
        INDArray assertion = Nd4j.create(new float[]{1.6666666f, 0.8333333f, 0.5555556f, 5}, new int[]{2, 2});
        INDArray result = half.div(divi);
        assertEquals(getFailureMessage(), assertion, result);
    }


    @Test
    public void testSigmoid() {
        INDArray n = Nd4j.create(new float[]{1, 2, 3, 4});
        INDArray assertion = Nd4j.create(new float[]{0.73105858f, 0.88079708f, 0.95257413f, 0.98201379f});
        INDArray sigmoid = Transforms.sigmoid(n, false);
        assertEquals(getFailureMessage(), assertion, sigmoid);

    }

    @Test
    public void testNeg() {
        INDArray n = Nd4j.create(new float[]{1, 2, 3, 4});
        INDArray assertion = Nd4j.create(new float[]{-1, -2, -3, -4});
        INDArray neg = Transforms.neg(n);
        assertEquals(getFailureMessage(),assertion, neg);

    }


    @Test
    public void testCosineSim() {

        INDArray vec1 = Nd4j.create(new double[]{1, 2, 3, 4});
        INDArray vec2 = Nd4j.create(new double[]{1, 2, 3, 4});
        double sim = Transforms.cosineSim(vec1, vec2);
        assertEquals(getFailureMessage(),1, sim, 1e-1);

        INDArray vec3 = Nd4j.create(new float[]{0.2f, 0.3f, 0.4f, 0.5f});
        INDArray vec4 = Nd4j.create(new float[]{0.6f, 0.7f, 0.8f, 0.9f});
        sim = Transforms.cosineSim(vec3, vec4);
        assertEquals(getFailureMessage(),0.98, sim, 1e-1);

    }


    @Test
    public void testExp() {
        INDArray n = Nd4j.create(new double[]{1, 2, 3, 4});
        INDArray assertion = Nd4j.create(new double[]{2.71828183f, 7.3890561f, 20.08553692f, 54.59815003f});
        INDArray exped = Transforms.exp(n);
        assertEquals(assertion, exped);
    }


    @Test
    public void testSlices() {
        INDArray arr = Nd4j.create(Nd4j.linspace(1, 24, 24).data(), new int[]{4, 3, 2});
        for (int i = 0; i < arr.slices(); i++) {
            INDArray slice  = arr.slice(i).slice(1);
            int slices = slice.slices();
            assertEquals(1, slices);
        }

    }




    @Test
    public void testScalar() {
        INDArray a = Nd4j.scalar(1.0);
        assertEquals(true, a.isScalar());

        INDArray n = Nd4j.create(new float[]{1.0f}, new int[]{1, 1});
        assertEquals(n, a);
        assertTrue(n.isScalar());
    }


    @Test
    public void testVectorAlongDimension1() {
        INDArray arr = Nd4j.create(1, 5, 5);
        assertEquals(arr.vectorsAlongDimension(0),5);
        assertEquals(arr.vectorsAlongDimension(1), 5);
        for(int i = 0; i < arr.vectorsAlongDimension(0); i++) {
            if(i < arr.vectorsAlongDimension(0) - 1 && i > 0)
                assertEquals(25,arr.vectorAlongDimension(i,0).length());
        }

    }

    @Test
    public void testWrap() throws Exception {
        int[] shape = {2, 4};
        INDArray d = Nd4j.linspace(1, 8, 8).reshape(shape[0], shape[1]);
        INDArray n = d;
        assertEquals(d.rows(), n.rows());
        assertEquals(d.columns(), n.columns());

        INDArray vector = Nd4j.linspace(1, 3, 3);
        INDArray testVector = vector;
        for (int i = 0; i < vector.length(); i++)
            assertEquals(vector.getDouble(i), testVector.getDouble(i), 1e-1);
        assertEquals(3, testVector.length());
        assertEquals(true, testVector.isVector());
        assertEquals(true, Shape.shapeEquals(new int[]{3}, testVector.shape()));

        INDArray row12 = Nd4j.linspace(1, 2, 2).reshape(2, 1);
        INDArray row22 = Nd4j.linspace(3, 4, 2).reshape(1, 2);

        assertEquals(row12.rows(), 2);
        assertEquals(row12.columns(), 1);
        assertEquals(row22.rows(), 1);
        assertEquals(row22.columns(), 2);
    }

    @Test
    public void testGetRowFortran() throws Exception {
        INDArray n = Nd4j.create(Nd4j.linspace(1, 4, 4).data(), new int[]{2, 2});
        INDArray column = Nd4j.create(new float[]{1, 3});
        INDArray column2 = Nd4j.create(new float[]{2, 4});
        INDArray testColumn = n.getRow(0);
        INDArray testColumn1 = n.getRow(1);
        assertEquals(column, testColumn);
        assertEquals(column2, testColumn1);


    }

    @Test
    public void testGetColumnFortran() {
        INDArray n = Nd4j.create(Nd4j.linspace(1, 4, 4).data(), new int[]{2, 2});
        INDArray column = Nd4j.create(new float[]{1, 2});
        INDArray column2 = Nd4j.create(new float[]{3, 4});
        INDArray testColumn = n.getColumn(0);
        INDArray testColumn1 = n.getColumn(1);
        assertEquals(column, testColumn);
        assertEquals(column2, testColumn1);

    }


    @Test
    public void testGetColumns() {
        INDArray matrix = Nd4j.linspace(1, 6, 6).reshape(2, 3);
        INDArray matrixGet = matrix.getColumns(new int[]{1, 2});
        INDArray matrixAssertion = Nd4j.create(new double[][]{{3, 5}, {4, 6}});
        assertEquals(matrixAssertion, matrixGet);
    }


    @Test
    public void testVectorInit() {
        DataBuffer data = Nd4j.linspace(1, 4, 4).data();
        INDArray arr = Nd4j.create(data, new int[]{4});
        assertEquals(true, arr.isRowVector());
        INDArray arr2 = Nd4j.create(data, new int[]{1, 4});
        assertEquals(true, arr2.isRowVector());

        INDArray columnVector = Nd4j.create(data, new int[]{4, 1});
        assertEquals(true, columnVector.isColumnVector());
    }

    @Test
    public void testConcatColumnWise() {
        INDArray rand = Nd4j.rand(123, new int[]{1, 1000});
        INDArray otherParameters = Nd4j.toFlattened(rand);
        INDArray wordvectors = Nd4j.rand(100, 68000);
        INDArray flattened = Nd4j.toFlattened(wordvectors);
        Nd4j.concat(0, otherParameters, flattened);
    }

    @Test
    public void testAssignOffset() {
        INDArray arr = Nd4j.ones(5, 5);
        INDArray row = arr.slice(1);
        row.assign(1);
        assertEquals(Nd4j.ones(5), row);
    }

    @Test
    public void testColumns() {
        INDArray arr = Nd4j.create(new int[]{3, 2});
        INDArray column2 = arr.getColumn(0);
        assertEquals(true, Shape.shapeEquals(new int[]{3, 1}, column2.shape()));
        INDArray column = Nd4j.create(new double[]{1, 2, 3}, new int[]{1,3});
        arr.putColumn(0, column);

        INDArray firstColumn = arr.getColumn(0);

        assertEquals(column, firstColumn);


        INDArray column1 = Nd4j.create(new double[]{4, 5, 6}, new int[]{1,3});
        arr.putColumn(1, column1);
        assertEquals(true, Shape.shapeEquals(new int[]{3, 1}, arr.getColumn(1).shape()));
        INDArray testRow1 = arr.getColumn(1);
        assertEquals(column1, testRow1);


        INDArray evenArr = Nd4j.create(new double[]{1, 2, 3, 4}, new int[]{2, 2});
        INDArray put = Nd4j.create(new double[]{5, 6}, new int[]{2});
        evenArr.putColumn(1, put);
        INDArray testColumn = evenArr.getColumn(1);
        assertEquals(put, testColumn);


        INDArray n = Nd4j.create(Nd4j.linspace(1, 4, 4).data(), new int[]{2, 2});
        INDArray column23 = n.getColumn(0);
        INDArray column12 = Nd4j.create(new double[]{1, 2}, new int[]{1,2});
        assertEquals(column23, column12);


        INDArray column0 = n.getColumn(1);
        INDArray column01 = Nd4j.create(new double[]{3, 4}, new int[]{1,2});
        assertEquals(column0, column01);


    }


    @Test
    public void testPutRow() {
        INDArray d = Nd4j.linspace(1, 4, 4).reshape(2, 2);
        INDArray n = d.dup();

        //works fine according to matlab, let's go with it..
        //reproduce with:  A = reshape(linspace(1,4,4),[2 2 ]);
        //A(1,2) % 1 index based
        float nFirst = 3;
        float dFirst = d.getFloat(0, 1);
        assertEquals(nFirst, dFirst, 1e-1);
        assertEquals(d.data(), n.data());
        assertEquals(true, Arrays.equals(new int[]{2, 2}, n.shape()));

        INDArray newRow = Nd4j.linspace(5, 6, 2);
        n.putRow(0, newRow);
        d.putRow(0, newRow);


        INDArray testRow = n.getRow(0);
        assertEquals(newRow.length(), testRow.length());
        assertEquals(true, Shape.shapeEquals(new int[]{2}, testRow.shape()));


        INDArray nLast = Nd4j.create(Nd4j.linspace(1, 4, 4).data(), new int[]{2, 2});
        INDArray row = nLast.getRow(1);
        INDArray row1 = Nd4j.create(new double[]{2, 4}, new int[]{2});
        assertEquals(row, row1);


        INDArray arr = Nd4j.create(new int[]{3, 2});
        INDArray evenRow = Nd4j.create(new double[]{1, 2}, new int[]{2});
        arr.putRow(0, evenRow);
        INDArray firstRow = arr.getRow(0);
        assertEquals(true, Shape.shapeEquals(new int[]{2}, firstRow.shape()));
        INDArray testRowEven = arr.getRow(0);
        assertEquals(evenRow, testRowEven);


        INDArray row12 = Nd4j.create(new double[]{5, 6}, new int[]{2});
        arr.putRow(1, row12);
        assertEquals(true, Shape.shapeEquals(new int[]{2}, arr.getRow(0).shape()));
        INDArray testRow1 = arr.getRow(1);
        assertEquals(row12, testRow1);


        INDArray multiSliceTest = Nd4j.create(Nd4j.linspace(1, 16, 16).data(), new int[]{4, 2, 2});
        INDArray test = Nd4j.create(new double[]{2, 10}, new int[]{2});
        INDArray test2 = Nd4j.create(new double[]{6, 14}, new int[]{2});

        INDArray multiSliceRow1 = multiSliceTest.slice(1).getRow(0);
        INDArray multiSliceRow2 = multiSliceTest.slice(1).getRow(1);

        assertEquals(test, multiSliceRow1);
        assertEquals(test2,multiSliceRow2);

    }






    @Test
    public void testInplaceTranspose() {
        INDArray test = Nd4j.rand(34, 484);
        INDArray transposei = test.transposei();

        for (int i = 0; i < test.rows(); i++) {
            for (int j = 0; j < test.columns(); j++) {
                assertEquals(test.getDouble(i, j), transposei.getDouble(j, i), 1e-1);
            }
        }

    }






    @Test
    public void testMmulF() {

        DataBuffer data = Nd4j.linspace(1, 10, 10).data();
        INDArray n = Nd4j.create(data, new int[]{1, 10});
        INDArray transposed = n.transpose();
        assertEquals(true, n.isRowVector());
        assertEquals(true, transposed.isColumnVector());


        INDArray innerProduct = n.mmul(transposed);

        INDArray scalar = Nd4j.scalar(385);
        assertEquals(getFailureMessage(),scalar, innerProduct);


        INDArray ten = Nd4j.linspace(1,100,100).reshape(10,10);
        INDArray square = ten.get(new NDArrayIndex(new int[]{5, 6, 7, 8, 9}), NDArrayIndex.all()).transpose();

        INDArray other = Nd4j.linspace(1, 600, 600).reshape(10, 60);
        INDArray other2 = other.get(NDArrayIndex.interval(5, 10), NDArrayIndex.all());

        INDArray arr1OffsetFor = Nd4j.linspace(1,12,12).reshape(4, 3);
        INDArray arr1Offset = arr1OffsetFor.get(NDArrayIndex.interval(1, 3), NDArrayIndex.all());
        INDArray arr1MmulWith = Nd4j.linspace(1,6,6).reshape(2,3);
        INDArray mmul = arr1Offset.mmul(arr1MmulWith.transpose());
        INDArray assertion2 = Nd4j.create(new double[][]{
                {70,88},{79,100}
        });
        assertEquals(getFailureMessage(),assertion2, mmul);

        INDArray mmulOffsets = square.mmul(other2);
        INDArray assertion = Nd4j.create(new double[][]{{    330.,     730.,    1130.,    1530.,    1930.,    2330.,
                2730.,    3130.,    3530.,    3930.,    4330.,    4730.,
                5130.,    5530.,    5930.,    6330.,    6730.,    7130.,
                7530.,    7930.,    8330.,    8730.,    9130.,    9530.,
                9930.,   10330.,   10730.,   11130.,   11530.,   11930.,
                12330.,   12730.,   13130.,   13530.,   13930.,   14330.,
                14730.,   15130.,   15530.,   15930.,   16330.,   16730.,
                17130.,   17530.,   17930.,   18330.,   18730.,   19130.,
                19530.,   19930.,   20330.,   20730.,   21130.,   21530.,
                21930.,   22330.,   22730.,   23130.,   23530.,   23930.},
                {    730.,    1630.,    2530.,    3430.,    4330.,    5230.,
                        6130.,    7030.,    7930.,    8830.,    9730.,   10630.,
                        11530.,   12430.,   13330.,   14230.,   15130.,   16030.,
                        16930.,   17830.,   18730.,   19630.,   20530.,   21430.,
                        22330.,   23230.,   24130.,   25030.,   25930.,   26830.,
                        27730.,   28630.,   29530.,   30430.,   31330.,   32230.,
                        33130.,   34030.,   34930.,   35830.,   36730.,   37630.,
                        38530.,   39430.,   40330.,   41230.,   42130.,   43030.,
                        43930.,   44830.,   45730.,   46630.,   47530.,   48430.,
                        49330.,   50230.,   51130.,   52030.,   52930.,   53830.},
                {   1130.,    2530.,    3930.,    5330.,    6730.,    8130.,
                        9530.,   10930.,   12330.,   13730.,   15130.,   16530.,
                        17930.,   19330.,   20730.,   22130.,   23530.,   24930.,
                        26330.,   27730.,   29130.,   30530.,   31930.,   33330.,
                        34730.,   36130.,   37530.,   38930.,   40330.,   41730.,
                        43130.,   44530.,   45930.,   47330.,   48730.,   50130.,
                        51530.,   52930.,   54330.,   55730.,   57130.,   58530.,
                        59930.,   61330.,   62730.,   64130.,   65530.,   66930.,
                        68330.,   69730.,   71130.,   72530.,   73930.,   75330.,
                        76730.,   78130.,   79530.,   80930.,   82330.,   83730.},
                {   1530.,    3430.,    5330.,    7230.,    9130.,   11030.,
                        12930.,   14830.,   16730.,   18630.,   20530.,   22430.,
                        24330.,   26230.,   28130.,   30030.,   31930.,   33830.,
                        35730.,   37630.,   39530.,   41430.,   43330.,   45230.,
                        47130.,   49030.,   50930.,   52830.,   54730.,   56630.,
                        58530.,   60430.,   62330.,   64230.,   66130.,   68030.,
                        69930.,   71830.,   73730.,   75630.,   77530.,   79430.,
                        81330.,   83230.,   85130.,   87030.,   88930.,   90830.,
                        92730.,   94630.,   96530.,   98430.,  100330.,  102230.,
                        104130.,  106030.,  107930.,  109830.,  111730.,  113630.},
                {   1930.,    4330.,    6730.,    9130.,   11530.,   13930.,
                        16330.,   18730.,   21130.,   23530.,   25930.,   28330.,
                        30730.,   33130.,   35530.,   37930.,   40330.,   42730.,
                        45130.,   47530.,   49930.,   52330.,   54730.,   57130.,
                        59530.,   61930.,   64330.,   66730.,   69130.,   71530.,
                        73930.,   76330.,   78730.,   81130.,   83530.,   85930.,
                        88330.,   90730.,   93130.,   95530.,   97930.,  100330.,
                        102730.,  105130.,  107530.,  109930.,  112330.,  114730.,
                        117130.,  119530.,  121930.,  124330.,  126730.,  129130.,
                        131530.,  133930.,  136330.,  138730.,  141130.,  143530.},
                {   2330.,    5230.,    8130.,   11030.,   13930.,   16830.,
                        19730.,   22630.,   25530.,   28430.,   31330.,   34230.,
                        37130.,   40030.,   42930.,   45830.,   48730.,   51630.,
                        54530.,   57430.,   60330.,   63230.,   66130.,   69030.,
                        71930.,   74830.,   77730.,   80630.,   83530.,   86430.,
                        89330.,   92230.,   95130.,   98030.,  100930.,  103830.,
                        106730.,  109630.,  112530.,  115430.,  118330.,  121230.,
                        124130.,  127030.,  129930.,  132830.,  135730.,  138630.,
                        141530.,  144430.,  147330.,  150230.,  153130.,  156030.,
                        158930.,  161830.,  164730.,  167630.,  170530.,  173430.},
                {   2730.,    6130.,    9530.,   12930.,   16330.,   19730.,
                        23130.,   26530.,   29930.,   33330.,   36730.,   40130.,
                        43530.,   46930.,   50330.,   53730.,   57130.,   60530.,
                        63930.,   67330.,   70730.,   74130.,   77530.,   80930.,
                        84330.,   87730.,   91130.,   94530.,   97930.,  101330.,
                        104730.,  108130.,  111530.,  114930.,  118330.,  121730.,
                        125130.,  128530.,  131930.,  135330.,  138730.,  142130.,
                        145530.,  148930.,  152330.,  155730.,  159130.,  162530.,
                        165930.,  169330.,  172730.,  176130.,  179530.,  182930.,
                        186330.,  189730.,  193130.,  196530.,  199930.,  203330.},
                {   3130.,    7030.,   10930.,   14830.,   18730.,   22630.,
                        26530.,   30430.,   34330.,   38230.,   42130.,   46030.,
                        49930.,   53830.,   57730.,   61630.,   65530.,   69430.,
                        73330.,   77230.,   81130.,   85030.,   88930.,   92830.,
                        96730.,  100630.,  104530.,  108430.,  112330.,  116230.,
                        120130.,  124030.,  127930.,  131830.,  135730.,  139630.,
                        143530.,  147430.,  151330.,  155230.,  159130.,  163030.,
                        166930.,  170830.,  174730.,  178630.,  182530.,  186430.,
                        190330.,  194230.,  198130.,  202030.,  205930.,  209830.,
                        213730.,  217630.,  221530.,  225430.,  229330.,  233230.},
                {   3530.,    7930.,   12330.,   16730.,   21130.,   25530.,
                        29930.,   34330.,   38730.,   43130.,   47530.,   51930.,
                        56330.,   60730.,   65130.,   69530.,   73930.,   78330.,
                        82730.,   87130.,   91530.,   95930.,  100330.,  104730.,
                        109130.,  113530.,  117930.,  122330.,  126730.,  131130.,
                        135530.,  139930.,  144330.,  148730.,  153130.,  157530.,
                        161930.,  166330.,  170730.,  175130.,  179530.,  183930.,
                        188330.,  192730.,  197130.,  201530.,  205930.,  210330.,
                        214730.,  219130.,  223530.,  227930.,  232330.,  236730.,
                        241130.,  245530.,  249930.,  254330.,  258730.,  263130.},
                {   3930.,    8830.,   13730.,   18630.,   23530.,   28430.,
                        33330.,   38230.,   43130.,   48030.,   52930.,   57830.,
                        62730.,   67630.,   72530.,   77430.,   82330.,   87230.,
                        92130.,   97030.,  101930.,  106830.,  111730.,  116630.,
                        121530.,  126430.,  131330.,  136230., 141130.,  146030.,
                        150930.,  155830.,  160730.,  165630.,  170530., 175430.,
                        180330., 185230., 190130., 195030.,  199930.,  204830.,
                        209730.,  214630.,  219530.,  224430.,  229330.,  234230.,
                        239130.,  244030.,  248930.,  253830.,  258730.,  263630.,
                        268530.,  273430.,  278330.,  283230.,  288130.,  293030.}});
        assertEquals(getFailureMessage(), assertion, mmulOffsets);
    }




    @Test
    public void testRowsColumns() {
        DataBuffer data = Nd4j.linspace(1, 6, 6).data();
        INDArray rows = Nd4j.create(data, new int[]{2, 3});
        assertEquals(2, rows.rows());
        assertEquals(3, rows.columns());

        INDArray columnVector = Nd4j.create(data, new int[]{6, 1});
        assertEquals(6, columnVector.rows());
        assertEquals(1, columnVector.columns());
        INDArray rowVector = Nd4j.create(data, new int[]{6});
        assertEquals(1, rowVector.rows());
        assertEquals(6, rowVector.columns());
    }


    @Test
    public void testTranspose() {
        INDArray n = Nd4j.create(Nd4j.ones(100).data(), new int[]{5, 5, 4});
        INDArray transpose = n.transpose();
        assertEquals(n.length(), transpose.length());
        assertEquals(true, Arrays.equals(new int[]{4, 5, 5}, transpose.shape()));

        INDArray rowVector = Nd4j.linspace(1, 10, 10);
        assertTrue(rowVector.isRowVector());
        INDArray columnVector = rowVector.transpose();
        assertTrue(columnVector.isColumnVector());


        INDArray linspaced = Nd4j.linspace(1, 4, 4).reshape(2, 2);
        INDArray transposed = Nd4j.create(new float[]{1, 3, 2, 4}, new int[]{2, 2});
        assertEquals(transposed, linspaced.transpose());

        linspaced = Nd4j.linspace(1, 4, 4).reshape(2, 2);
        //fortran ordered
        INDArray transposed2 = Nd4j.create(new float[]{1, 3, 2, 4}, new int[]{2, 2});
        transposed = linspaced.transpose();
        assertEquals(transposed, transposed2);


    }




    @Test
    public void testAddMatrix() {
        INDArray five = Nd4j.ones(5);
        five.addi(five.dup());
        INDArray twos = Nd4j.valueArrayOf(5, 2);
        assertEquals(getFailureMessage(),twos, five);

    }


    @Test
    public void testDimensionWiseWithVector() {
        INDArray ret = Nd4j.linspace(1, 2, 2).reshape(1, 2);
        assertTrue(ret.sum(0).isRowVector());
        assertTrue(ret.sum(1).isScalar());
        INDArray retColumn = Nd4j.linspace(1,2,2).reshape(2, 1);
        assertTrue(getFailureMessage(),retColumn.sum(1).isRowVector());
        assertTrue(getFailureMessage(),retColumn.sum(0).isScalar());

        INDArray m2 = Nd4j.rand(1, 2);
        Nd4j.sum(m2, 0);


        Nd4j.sum(m2, 1);

        INDArray m3 = Nd4j.rand(2, 1);

        Nd4j.sum(m3, 0);
        Nd4j.sum(m3, 1).toString();

    }


    @Test
    public void testMMul() {
        INDArray arr = Nd4j.create(new double[][]{
                {1, 2, 3}, {4, 5, 6}
        });

        INDArray assertion = Nd4j.create(new double[][]{
                {14, 32}, {32, 77}
        });

        INDArray test = arr.mmul(arr.transpose());
        assertEquals(getFailureMessage(), assertion, test);

    }

    @Test
    public void testPutSlice() {
        INDArray n = Nd4j.linspace(1,27,27).reshape(3, 3, 3);
        INDArray newSlice = Nd4j.zeros(3, 3);
        n.putSlice(0, newSlice);
        assertEquals(getFailureMessage(), newSlice, n.slice(0));

    }

    @Test
    public void testRowVectorMultipleIndices() {
        INDArray linear = Nd4j.create(1, 4);
        linear.putScalar(new int[]{0, 1}, 1);
        assertEquals(getFailureMessage(), linear.getDouble(0, 1), 1, 1e-1);
    }




    @Test
    public void testDim1() {
        INDArray sum = Nd4j.linspace(1,2, 2).reshape(2, 1);
        INDArray same = sum.dup();
        assertEquals(same.sum(1), sum);
    }


    @Test
    public void testEps() {
        INDArray ones = Nd4j.ones(5);
        double sum = Nd4j.getExecutioner().exec(new Eps(ones, ones, ones, ones.length())).z().sum(Integer.MAX_VALUE).getDouble(0);
        assertEquals(5, sum, 1e-1);
    }


    @Test
    public void testLogDouble() {
        INDArray linspace = Nd4j.linspace(1, 6, 6);
        INDArray log = Transforms.log(linspace);
        INDArray assertion = Nd4j.create(new double[]{0, 0.6931471805599453, 1.0986122886681098, 1.3862943611198906, 1.6094379124341005, 1.791759469228055});
        assertEquals(assertion, log);
    }



    @Test
    public void testSmallSum() {
        INDArray base = Nd4j.create(new double[]{5.843333333333335, 3.0540000000000007});
        base.addi(1e-12);
        INDArray assertion = Nd4j.create(new double[]{5.84333433, 3.054001});
        assertEquals(assertion, base);

    }





    @Test
    public void testPermute() {
        INDArray n = Nd4j.create(Nd4j.linspace(1, 20, 20).data(), new int[]{5, 4});
        INDArray transpose = n.transpose();
        INDArray permute = n.permute(1, 0);
        assertEquals(permute, transpose);
        assertEquals(transpose.length(), permute.length(), 1e-1);


        INDArray toPermute = Nd4j.create(Nd4j.linspace(0, 7, 8).data(), new int[]{2, 2, 2});
        INDArray permuted = toPermute.permute(2, 1, 0);
        assertNotEquals(toPermute,permuted);
        assertEquals('c',permuted.ordering());

        INDArray permuteOther = toPermute.permute(1, 2, 0);
        for(int i = 0; i < permuteOther.slices(); i++) {
            INDArray toPermutesliceI = toPermute.slice(i);
            INDArray permuteOtherSliceI = permuteOther.slice(i);
            permuteOtherSliceI.toString();
            assertNotEquals(toPermutesliceI,permuteOtherSliceI);
        }
        assertArrayEquals(permuteOther.shape(), toPermute.shape());
        assertNotEquals(toPermute, permuteOther);


    }




    @Test
    public void testSliceConstructor() throws Exception {
        List<INDArray> testList = new ArrayList<>();
        for (int i = 0; i < 5; i++)
            testList.add(Nd4j.scalar(i + 1));

        INDArray test = Nd4j.create(testList, new int[]{1, testList.size()});
        INDArray expected = Nd4j.create(new float[]{1, 2, 3, 4, 5}, new int[]{5, 1, 1});
        assertEquals(expected, test);
    }




    @Test
    public void testSlice() {
        INDArray arr = Nd4j.linspace(1, 24, 24).reshape(4, 3, 2);
        INDArray assertion = Nd4j.create(new double[][]{
                {1, 13}
                , {5, 17}
                , {9, 21}
        });

        INDArray firstSlice = arr.slice(0);
        assertEquals(assertion,firstSlice);

    }




    @Test
    public void testAppendBias() {
        INDArray rand = Nd4j.linspace(1, 25, 25).transpose();
        INDArray test = Nd4j.appendBias(rand);
        INDArray assertion = Nd4j.toFlattened(rand, Nd4j.scalar(1));
        assertEquals(assertion, test);
    }

    @Test
    public void testRand() {
        INDArray rand = Nd4j.randn(5, 5);
        Nd4j.getDistributions().createUniform(0.4, 4).sample(5);
        Nd4j.getDistributions().createNormal(1, 5).sample(10);
        //Nd4j.getDistributions().createBinomial(5, 1.0).sample(new int[]{5, 5});
        //Nd4j.getDistributions().createBinomial(1, Nd4j.ones(5, 5)).sample(rand.shape());
        Nd4j.getDistributions().createNormal(rand, 1).sample(rand.shape());
    }




    @Test
    public void testLinearViewAlignment() {
        INDArray twoToFour = Nd4j.create(new double[][]{
                {1, 2},
                {3, 4}
        });
        INDArray linear = twoToFour.linearView();
        assertEquals(Nd4j.create(new double[]{1, 2, 3, 4}), linear);
    }


    @Test
    public void testIdentity() {
        INDArray eye = Nd4j.eye(5);
        assertTrue(Arrays.equals(new int[]{5, 5}, eye.shape()));
        eye = Nd4j.eye(5);
        assertTrue(Arrays.equals(new int[]{5, 5}, eye.shape()));


    }


    @Test
    public void testColumnVectorOpsFortran() {
        INDArray twoByTwo = Nd4j.create(new float[]{1, 2, 3, 4}, new int[]{2, 2});
        INDArray toAdd = Nd4j.create(new float[]{1, 2}, new int[]{2, 1});
        twoByTwo.addiColumnVector(toAdd);
        INDArray assertion = Nd4j.create(new float[]{2, 4, 5, 6}, new int[]{2, 2});
        assertEquals(assertion, twoByTwo);


    }






    @Test
    public void testRSubi() {
        INDArray n2 = Nd4j.ones(2);
        INDArray n2Assertion = Nd4j.zeros(2);
        INDArray nRsubi = n2.rsubi(1);
        assertEquals(n2Assertion, nRsubi);
    }


    @Test
    public void testConcat() {
        INDArray A = Nd4j.linspace(1, 8, 8).reshape(2, 2, 2);
        INDArray B = Nd4j.linspace(1, 12, 12).reshape(3, 2, 2);
        INDArray concat = Nd4j.concat(0, A, B);
        assertTrue(Arrays.equals(new int[]{5, 2, 2}, concat.shape()));

    }

    @Test
    public void testConcatHorizontally() {
        INDArray rowVector = Nd4j.ones(5);
        INDArray other = Nd4j.ones(5);
        INDArray concat = Nd4j.hstack(other, rowVector);
        assertEquals(rowVector.rows(), concat.rows());
        assertEquals(rowVector.columns() * 2, concat.columns());

    }








    @Test
    public void testAssign() {
        INDArray vector = Nd4j.linspace(1, 5, 5);
        vector.assign(1);
        assertEquals(Nd4j.ones(5),vector);
        INDArray twos = Nd4j.ones(2,2);
        INDArray rand = Nd4j.rand(2,2);
        twos.assign(rand);
        assertEquals(rand,twos);

        INDArray tensor = Nd4j.rand((long) 3, 3, 3, 3);
        INDArray ones = Nd4j.ones(3, 3, 3);
        assertTrue(Arrays.equals(tensor.shape(), ones.shape()));
        ones.assign(tensor);
        assertEquals(tensor, ones);
    }

    @Test
    public void testAddScalar() {
        INDArray div = Nd4j.valueArrayOf(new int[]{1, 4}, 4);
        INDArray rdiv = div.add(1);
        INDArray answer = Nd4j.valueArrayOf(new int[]{1, 4}, 5);
        assertEquals(answer, rdiv);
    }

    @Test
    public void testRdivScalar() {
        INDArray div = Nd4j.valueArrayOf(2, 4);
        INDArray rdiv = div.rdiv(1);
        INDArray answer = Nd4j.valueArrayOf(new int[]{1, 4}, 0.25);
        assertEquals(rdiv, answer);
    }

    @Test
    public void testRDivi() {
        INDArray n2 = Nd4j.valueArrayOf(new int[]{1, 2}, 4);
        INDArray n2Assertion = Nd4j.valueArrayOf(new int[]{1, 2}, 0.5);
        INDArray nRsubi = n2.rdivi(2);
        assertEquals(n2Assertion, nRsubi);
    }


    @Test
    public void testVectorAlongDimension() {
        INDArray arr = Nd4j.linspace(1, 24, 24).reshape(4, 3, 2);
        INDArray assertion = Nd4j.create(new float[]{2,14}, new int[]{1,2});
        INDArray vectorDimensionTest = arr.vectorAlongDimension(1, 2);
        assertEquals(assertion,vectorDimensionTest);
        INDArray zeroOne = arr.vectorAlongDimension(0, 1);
        assertEquals(zeroOne, Nd4j.create(new float[]{1, 5,9}));

        INDArray testColumn2Assertion = Nd4j.create(new float[]{2,6,10});
        INDArray testColumn2 = arr.vectorAlongDimension(1, 1);

        assertEquals(testColumn2Assertion, testColumn2);


        INDArray testColumn3Assertion = Nd4j.create(new float[]{3,7,11});
        INDArray testColumn3 = arr.vectorAlongDimension(2, 1);
        assertEquals(testColumn3Assertion, testColumn3);


        INDArray v1 = Nd4j.linspace(1, 4, 4).reshape(new int[]{2, 2});
        INDArray testColumnV1 = v1.vectorAlongDimension(0, 0);
        INDArray testColumnV1Assertion = Nd4j.create(new float[]{1, 2});
        assertEquals(testColumnV1Assertion, testColumnV1);

        INDArray testRowV1 = v1.vectorAlongDimension(1, 0);
        INDArray testRowV1Assertion = Nd4j.create(new float[]{3, 4});
        assertEquals(testRowV1Assertion, testRowV1);

    }

    @Test
    public void testNewLinearView() {
        INDArray arange = Nd4j.arange(1,17).reshape(4, 4);
        NDArrayIndex index = NDArrayIndex.interval(0, 2);
        INDArray get = arange.get(index, index);
        LinearViewNDArray linearViewNDArray = new LinearViewNDArray(get);
        assertEquals(Nd4j.create(new double[]{1,5,2,6}),linearViewNDArray);

    }

    @Test
    public void testArangeMul() {
        INDArray arange = Nd4j.arange(1,17).reshape(4, 4);
        NDArrayIndex index = NDArrayIndex.interval(0, 2);
        INDArray get = arange.get(index, index);
        INDArray zeroPointTwoFive = Nd4j.ones(2,2).mul(0.25);
        INDArray mul = get.mul(zeroPointTwoFive);
        INDArray assertion = Nd4j.create(new double[][]{
                {0.25, 1.25},
                {0.5, 1.5}
        });
        assertEquals(assertion, mul);

    }







    @Test
    public void testNumVectorsAlongDimension() {
        INDArray arr = Nd4j.linspace(1, 24, 24).reshape(4, 3, 2);
        assertEquals(12, arr.vectorsAlongDimension(2));
    }


    @Test
    public void testGetScalar() {
        INDArray n = Nd4j.create(new float[]{1, 2, 3, 4}, new int[]{4});
        assertTrue(n.isVector());
        for (int i = 0; i < n.length(); i++) {
            INDArray scalar = Nd4j.scalar((float) i + 1);
            assertEquals(scalar, n.getScalar(i));
        }


    }

    @Test
    public void testGetScalarFortran() {
        INDArray n = Nd4j.create(new float[]{1, 2, 3, 4}, new int[]{4});
        for (int i = 0; i < n.length(); i++) {
            INDArray scalar = Nd4j.scalar((float) i + 1);
            assertEquals(scalar, n.getScalar(i));
        }


        INDArray twoByTwo = Nd4j.create(new float[][]{{1, 2}, {3, 4}});
        INDArray column = twoByTwo.getColumn(0);
        assertEquals(Nd4j.create(new float[]{1, 3}), column);
        assertEquals(1, column.getFloat(0), 1e-1);
        assertEquals(3, column.getFloat(1), 1e-1);
        assertEquals(Nd4j.scalar(1), column.getScalar(0));
        assertEquals(Nd4j.scalar(3), column.getScalar(1));

    }





    @Test
    public void testBroadCast() {
        INDArray n = Nd4j.linspace(1, 4, 4);
        INDArray broadCasted = n.broadcast(5, 4);
        for (int i = 0; i < broadCasted.rows(); i++) {
            assertEquals(n, broadCasted.getRow(i));
        }

        INDArray broadCast2 = broadCasted.getRow(0).broadcast(5, 4);
        assertEquals(broadCasted, broadCast2);


        INDArray columnBroadcast = n.transpose().broadcast(4, 5);
        for (int i = 0; i < columnBroadcast.columns(); i++) {
            assertEquals(columnBroadcast.getColumn(i), n.transpose());
        }

        INDArray fourD = Nd4j.create(1, 2, 1, 1);
        INDArray broadCasted3 = fourD.broadcast(1, 1, 36, 36);
        assertTrue(Arrays.equals(new int[]{1, 2, 36, 36}, broadCasted3.shape()));
    }

    @Test
    public void testMatrix() {
        INDArray arr = Nd4j.create(new float[]{1, 2, 3, 4}, new int[]{2, 2});
        INDArray brr = Nd4j.create(new float[]{5, 6}, new int[]{1, 2});
        INDArray row = arr.getRow(0);
        row.subi(brr);
        assertEquals(Nd4j.create(new double[]{-4, -3}), arr.getRow(0));

    }

    @Test
    public void testPutRowGetRowOrdering() {
        INDArray row1 = Nd4j.linspace(1, 4, 4).reshape(2, 2);
        INDArray put = Nd4j.create(new double[]{5, 6});
        row1.putRow(1, put);


        INDArray row1Fortran = Nd4j.linspace(1, 4, 4).reshape(2, 2);
        INDArray putFortran = Nd4j.create(new double[]{5, 6});
        row1Fortran.putRow(1, putFortran);
        assertEquals(row1, row1Fortran);
        INDArray row1CTest = row1.getRow(1);
        INDArray row1FortranTest = row1Fortran.getRow(1);
        assertEquals(row1CTest, row1FortranTest);



    }


    @Test
    public void testSumWithRow1(){
        //Works:
        INDArray array2d = Nd4j.ones(1,10);
        array2d.sum(0); //OK
        array2d.sum(1); //OK

        INDArray array3d = Nd4j.ones(1,10,10);
        array3d.sum(0); //OK
        array3d.sum(1); //OK
        array3d.sum(2); //java.lang.IllegalArgumentException: Illegal index 100 derived from 9 with offset of 10 and stride of 10

        INDArray array4d = Nd4j.ones(1,10,10,10);
        array4d.sum(0); //OK
        array4d.sum(1); //OK
        array4d.sum(2); //java.lang.IllegalArgumentException: Illegal index 1000 derived from 9 with offset of 910 and stride of 10
        array4d.sum(3); //java.lang.IllegalArgumentException: Illegal index 1000 derived from 9 with offset of 100 and stride of 100

        INDArray array5d = Nd4j.ones(1, 10, 10, 10, 10);
        array5d.sum(0); //OK
        array5d.sum(1); //OK
        array5d.sum(2); //java.lang.IllegalArgumentException: Illegal index 10000 derived from 9 with offset of 9910 and stride of 10
        array5d.sum(3); //java.lang.IllegalArgumentException: Illegal index 10000 derived from 9 with offset of 9100 and stride of 100
        array5d.sum(4); //java.lang.IllegalArgumentException: Illegal index 10000 derived from 9 with offset of 1000 and stride of 1000
    }

    @Test
    public void testSumWithRow2(){
        //All sums in this method execute without exceptions.
        INDArray array3d = Nd4j.ones(2,10,10);
        array3d.sum(0);
        array3d.sum(1);
        array3d.sum(2);

        INDArray array4d = Nd4j.ones(2,10,10,10);
        array4d.sum(0);
        array4d.sum(1);
        array4d.sum(2);
        array4d.sum(3);

        INDArray array5d = Nd4j.ones(2, 10, 10, 10, 10);
        array5d.sum(0);
        array5d.sum(1);
        array5d.sum(2);
        array5d.sum(3);
        array5d.sum(4);
    }


    @Test
    public void testPutRowFortran() {
        INDArray row1 = Nd4j.linspace(1, 4, 4).reshape(2, 2);
        INDArray put = Nd4j.create(new double[]{5, 6});
        row1.putRow(1, put);


        INDArray row1Fortran = Nd4j.create(new double[][]{{1, 3}, {2, 4}});
        INDArray putFortran = Nd4j.create(new double[]{5, 6});
        row1Fortran.putRow(1, putFortran);
        assertEquals(row1, row1Fortran);



    }


    @Test
    public void testElementWiseOps() {
        INDArray n1 = Nd4j.scalar(1);
        INDArray n2 = Nd4j.scalar(2);
        INDArray nClone = n1.add(n2);
        assertEquals(Nd4j.scalar(3), nClone);
        INDArray n1PlusN2 = n1.add(n2);
        assertFalse(getFailureMessage(), n1PlusN2.equals(n1));

        INDArray n3 = Nd4j.scalar(3);
        INDArray n4 = Nd4j.scalar(4);
        INDArray subbed = n4.sub(n3);
        INDArray mulled = n4.mul(n3);
        INDArray div = n4.div(n3);

        assertFalse(subbed.equals(n4));
        assertFalse(mulled.equals(n4));
        assertEquals(Nd4j.scalar(1), subbed);
        assertEquals(Nd4j.scalar(12), mulled);
        assertEquals(Nd4j.scalar(1.333333333333333333333), div);
    }


    @Test
    public void testRollAxis() {
        INDArray toRoll = Nd4j.ones(3,4,5,6);
        assertArrayEquals(new int[]{3,6,4,5},Nd4j.rollAxis(toRoll,3,1).shape());
        assertArrayEquals(new int[]{5,3,4,6},Nd4j.rollAxis(toRoll,3).shape());
    }

    @Test
    public void testTensorDot() {
        INDArray oneThroughSixty = Nd4j.arange(60).reshape(3,4,5);
        INDArray oneThroughTwentyFour = Nd4j.arange(24).reshape(4, 3, 2);
        INDArray result = Nd4j.tensorMmul(oneThroughSixty,oneThroughTwentyFour,new int[][]{{1,0},{0,1}});
        assertArrayEquals(new int[]{5,2},result.shape());
        INDArray assertion = Nd4j.create(new double[][]{
                {   440. ,  1232.},
                {  1232. ,  3752.},
                {  2024.  , 6272.},
                {  2816. ,  8792.},
                {  3608. , 11312.}
        });
        assertEquals(assertion,result);

    }


    @Test
    public void testNegativeShape() {
        INDArray linspace = Nd4j.linspace(1,4,4);
        INDArray reshaped = linspace.reshape(-1,2);
        assertArrayEquals(new int[]{2,2},reshaped.shape());

        INDArray linspace6 = Nd4j.linspace(1,6,6);
        INDArray reshaped2 = linspace6.reshape(-1,3);
        assertArrayEquals(new int[]{2,3},reshaped2.shape());

    }

    @Test
    public void testGetColumnGetRow(){
        INDArray row = Nd4j.ones(5);
        for( int i = 0; i < 5; i++ ){
            INDArray col = row.getColumn(i);
            assertArrayEquals(col.shape(),new int[]{1,1});
        }

        INDArray col = Nd4j.ones(5,1);
        for( int i = 0; i < 5; i++ ){
            INDArray row2 = col.getRow(i);
            assertArrayEquals(row2.shape(), new int[]{1, 1});
        }
    }

    @Test
    public void testSliceLeadingTrailingOnes(){
        INDArray arr1 = Nd4j.ones(10,10,10);
        testSliceHelper(arr1,0,new int[]{10,10});
        testSliceHelper(arr1,1,new int[]{10,10});
        testSliceHelper(arr1,2,new int[]{10,10});

        INDArray arr2 = Nd4j.ones(1,10,10);
        testSliceHelper(arr2,0,new int[]{10,10});
        testSliceHelper(arr2,1,new int[]{1,10});
        testSliceHelper(arr2,2,new int[]{1,10});

        INDArray arr3 = Nd4j.ones(10,10,1);
        testSliceHelper(arr3,0,new int[]{10,1});
        testSliceHelper(arr3,1,new int[]{10,1});
        testSliceHelper(arr3,2,new int[]{10,10});

        INDArray arr3a = Nd4j.ones(1,10,1);
        testSliceHelper(arr3a,0,new int[]{10,1});
        testSliceHelper(arr3a,1,new int[]{1,1});
        testSliceHelper(arr3a,2,new int[]{1,10});

        INDArray arr4 = Nd4j.ones(10,10,10,10);
        testSliceHelper(arr4,0,new int[]{10,10,10});
        testSliceHelper(arr4,1,new int[]{10,10,10});
        testSliceHelper(arr4,2,new int[]{10,10,10});
        testSliceHelper(arr4,3,new int[]{10,10,10});

        INDArray arr5 = Nd4j.ones(1,10,10,10);
        testSliceHelper(arr5,0,new int[]{10,10,10});
        testSliceHelper(arr5,1,new int[]{1,10,10});
        testSliceHelper(arr5,2,new int[]{1,10,10});
        testSliceHelper(arr5,3,new int[]{1,10,10});

        INDArray arr6 = Nd4j.ones(10,10,10,1);
        testSliceHelper(arr6,0,new int[]{10,10,1});
        testSliceHelper(arr6,1,new int[]{10,10,1});
        testSliceHelper(arr6,2,new int[]{10,10,1});
        testSliceHelper(arr6,3,new int[]{10,10,10});

        INDArray arr7 = Nd4j.ones(1,10,10,1);
        testSliceHelper(arr7,0,new int[]{10,10,1});
        testSliceHelper(arr7,1,new int[]{1,10,1});
        testSliceHelper(arr7,2,new int[]{1,10,1});
        testSliceHelper(arr7,3,new int[]{1,10,10});
    }

    private static void testSliceHelper(INDArray in, int dimension, int[] expectedShape ){
        int[] shape = in.shape();
        for( int i=0; i<shape[dimension]; i++ ){
            INDArray slice = in.slice(i,dimension);
            int[] sliceShape = slice.shape();
            assertArrayEquals(sliceShape,expectedShape);
        }
    }


    @Test
    public void testFlatten() {
        INDArray arr = Nd4j.create(Nd4j.linspace(1, 4, 4).data(), new int[]{2, 2});
        INDArray flattened = arr.ravel();
        assertEquals(arr.length(), flattened.length());
        assertEquals(true, Shape.shapeEquals(new int[]{1, arr.length()}, flattened.shape()));
        double[] comp = new double[] {1,3,2,4};
        for (int i = 0; i < arr.length(); i++) {
            assertEquals(comp[i], flattened.getFloat(i), 1e-1);
        }
        assertTrue(flattened.isVector());


        INDArray n = Nd4j.create(Nd4j.ones(27).data(), new int[]{3, 3, 3});
        INDArray nFlattened = n.ravel();
        assertTrue(nFlattened.isVector());


    }

    @Override
    public char ordering() {
        return 'f';
    }
}
