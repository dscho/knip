import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class SerializationTests {

        public static void main(final String[] args) throws IOException,
                        InterruptedException {

                double s = 0;
                double d = 0;

                for (int k = 0; k < 10; k++) {

                        // Creating the test array....
                        final int[] testArray = new int[(int) Math.pow(2, 27)];

                        for (int i = 0; i < testArray.length; i++) {
                                if (i % 2 == 0) {
                                        testArray[i] = i;
                                }
                        }

                        final DataOutputStream dataOut = new DataOutputStream(
                                        new BufferedOutputStream(
                                                        new FileOutputStream(
                                                                        "d:\\test"
                                                                                        + k
                                                                                        + "_"
                                                                                        + 27
                                                                                        + ".dat")));

                        // BufferedDataOutputStream dataOut = new
                        // BufferedDataOutputStream(
                        // new FileOutputStream("d:\\test" + k + "_" + 27 +
                        // ".dat"));

                        // Serializing...
                        long start = System.nanoTime();

                        for (int i = 0; i < testArray.length; i++) {
                                dataOut.writeInt(testArray[i]);
                        }
                        // dataOut.write(testArray);

                        s += (System.nanoTime() - start) / 1000000000.0;

                        dataOut.flush();
                        dataOut.close();

                        final DataInputStream stream = new DataInputStream(
                                        new BufferedInputStream(
                                                        new FileInputStream(
                                                                        "d:\\test"
                                                                                        + k
                                                                                        + "_"
                                                                                        + 27
                                                                                        + ".dat")));

                        final int[] testArrayIn = new int[(int) Math.pow(2, 27)];

                        start = System.nanoTime();

                        for (int i = 0; i < testArrayIn.length; i++) {
                                testArrayIn[i] = stream.readInt();
                        }

                        // stream.readLArray(testArrayIn);
                        d += (System.nanoTime() - start) / 1000000000.0;

                        stream.close();
                }

                System.out.println((((s / 10.0) + "").replace(".", ",")));
                System.out.println((((d / 10.0) + "").replace(".", ",")));
        }
}

// }
