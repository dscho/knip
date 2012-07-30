//package org.knime.knip.core.ops.iterable;
//
//import java.util.Iterator;
//
//import net.imglib2.img.Img;
//import net.imglib2.img.ImgFactory;
//import net.imglib2.img.array.ArrayImgFactory;
//import net.imglib2.ops.UnaryOperation;
//import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
//import net.imglib2.type.numeric.integer.UnsignedByteType;
//
//import org.knime.knip.core.types.NeighborhoodType;
//
//public class SlidingSpeedTests {
//
//        public static void main(String[] args) {
//
//                long[] imgDims = new long[] { 2000, 2000, 5 };
//
//                ImgFactory<UnsignedByteType> fac = new ArrayImgFactory<UnsignedByteType>();
//
//                Img<UnsignedByteType> img = fac.create(imgDims,
//                                new UnsignedByteType());
//                Img<UnsignedByteType> res = fac.create(imgDims,
//                                new UnsignedByteType());
//
//                // int[] span = new int[img.numDimensions()];
//                // Arrays.fill(span, 1);
//
//                UnaryOperation<Iterator<UnsignedByteType>, UnsignedByteType> op = new UnaryOperation<Iterator<UnsignedByteType>, UnsignedByteType>() {
//
//                        @Override
//                        public UnsignedByteType compute(
//                                        Iterator<UnsignedByteType> input,
//                                        UnsignedByteType output) {
//
//                                while (input.hasNext()) {
//                                        input.next();
//                                }
//                                return output;
//                        }
//
//                        @Override
//                        public UnaryOperation<Iterator<UnsignedByteType>, UnsignedByteType> copy() {
//                                return null;
//                        }
//                };
//
//                SlidingNeighborhoodOp<UnsignedByteType, UnsignedByteType, Img<UnsignedByteType>, Img<UnsignedByteType>> slidingOp = new SlidingNeighborhoodOp<UnsignedByteType, UnsignedByteType, Img<UnsignedByteType>, Img<UnsignedByteType>>(
//                                NeighborhoodType.RECTANGULAR,
//                                new OutOfBoundsBorderFactory<UnsignedByteType, Img<UnsignedByteType>>(),
//                                1, op);
//
//                long curr = System.nanoTime();
//                slidingOp.compute(img, res);
//                System.out.println("Speed " + (System.nanoTime() - curr) / 1000
//                                / 1000);
//        }
// }
