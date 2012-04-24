package org.kniplib.io.serialization.serializers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;

import net.imglib2.img.NativeImg;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.basictypeaccess.DataAccess;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.img.cell.AbstractCell;
import net.imglib2.img.cell.CellCursor;
import net.imglib2.img.cell.CellImg;
import net.imglib2.img.cell.CellImg.CellContainerSampler;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.planar.PlanarImg;
import net.imglib2.img.planar.PlanarImgFactory;
import net.imglib2.img.sparse.Ntree;
import net.imglib2.img.sparse.Ntree.NtreeNode;
import net.imglib2.img.sparse.NtreeAccess;
import net.imglib2.img.sparse.NtreeImg;
import net.imglib2.img.sparse.NtreeImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;

import org.kniplib.io.serialization.BufferedDataInputStream;
import org.kniplib.io.serialization.BufferedDataOutputStream;
import org.kniplib.io.serialization.PlanarImgContainerSamplerImpl;
import org.kniplib.types.ImgFactoryTypes;

public class NativeImgDeSerializer {

        @SuppressWarnings("unchecked")
        public void serialize(
                        BufferedDataOutputStream out,
                        NativeImg<? extends NativeType<?>, ? extends DataAccess> img)
                        throws IOException {
                ImgFactoryTypes factoryType = ImgFactoryTypes
                                .getImgFactoryType(img.factory());

                // write factory type
                out.writeInt(factoryType.ordinal());

                // write dimensions
                out.writeInt(img.numDimensions());
                for (int i = 0; i < img.numDimensions(); i++) {
                        out.writeLong(img.dimension(i));
                }

                switch (factoryType) {
                case ARRAY_IMG_FACTORY:
                        serializeArrayImg(
                                        out,
                                        (ArrayImg<? extends NativeType<?>, ? extends ArrayDataAccess<?>>) img);
                        break;

                case PLANAR_IMG_FACTORY:
                        serializePlanarImg(
                                        out,
                                        (PlanarImg<? extends NativeType<?>, ? extends ArrayDataAccess<?>>) img);
                        break;

                case CELL_IMG_FACTORY:
                        serializeCellImg(
                                        out,
                                        (CellImg<? extends NativeType<?>, ? extends ArrayDataAccess<?>, ? extends AbstractCell<?>>) img);
                        break;

                case NTREE_IMG_FACTORY:
                        serializeNtreeImg(
                                        out,
                                        (NtreeImg<? extends NativeType<?>, ?>) img);
                        break;

                default:
                        throw new UnsupportedOperationException(
                                        "Serializing a native-image with the specified factory("
                                                        + factoryType.toString()
                                                        + ") type isn't supported, yet.");
                }
        }

        public NativeImg<? extends NativeType<?>, ? extends DataAccess> deserialize(
                        BufferedDataInputStream in) throws IOException {

                ImgFactoryTypes factoryType = ImgFactoryTypes.values()[in
                                .readInt()];
                long[] dims = new long[in.readInt()];
                in.read(dims);

                switch (factoryType) {
                case ARRAY_IMG_FACTORY:
                        return deserializeArrayImg(in, dims);

                case PLANAR_IMG_FACTORY:
                        return deserializePlanarImg(in, dims);

                case CELL_IMG_FACTORY:
                        return deserializeCellImg(in, dims);

                case NTREE_IMG_FACTORY:
                        return deserializeNtreeImg(in, dims);

                default:
                        throw new UnsupportedOperationException(
                                        "Deserializing a native-image with the specified factory("
                                                        + factoryType.toString()
                                                        + ") type isn't supported, yet.");
                }

        }

        /*
         * ARRAYIMG
         */
        private void serializeArrayImg(
                        BufferedDataOutputStream out,
                        ArrayImg<? extends NativeType<?>, ? extends ArrayDataAccess<?>> img)
                        throws IOException {
                ImgDeSerializer.writeClass(out, img.firstElement().getClass()
                                .getName());
                out.writeArray(((img).update(null)).getCurrentStorageArray());
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        private ArrayImg<? extends NativeType<?>, ? extends ArrayDataAccess<?>> deserializeArrayImg(
                        BufferedDataInputStream in, long[] dims)
                        throws IOException {
                NativeType<?> type = (NativeType<?>) ImgDeSerializer
                                .readClass(in);

                ArrayImg<? extends NativeType<?>, ? extends ArrayDataAccess<?>> img = (ArrayImg<? extends NativeType<?>, ? extends ArrayDataAccess<?>>) new ArrayImgFactory()
                                .create(dims, type);
                in.readLArray(((ArrayDataAccess<?>) img.update(null))
                                .getCurrentStorageArray());

                return img;
        }

        /*
         * PLANARIMG
         */
        private void serializePlanarImg(
                        final BufferedDataOutputStream out,
                        PlanarImg<? extends NativeType<?>, ? extends ArrayDataAccess<?>> img)
                        throws IOException {
                ImgDeSerializer.writeClass(out, img.firstElement().getClass()
                                .getName());

                PlanarImgContainerSamplerImpl sampler = new PlanarImgContainerSamplerImpl();

                PlanarImg<? extends NativeType<?>, ? extends ArrayDataAccess<?>> planar = (PlanarImg<? extends NativeType<?>, ? extends ArrayDataAccess<?>>) img;
                for (int n = 0; n < planar.numSlices(); n++) {
                        sampler.fwd();
                        out.writeArray(((ArrayDataAccess<?>) planar
                                        .update(sampler))
                                        .getCurrentStorageArray());
                }
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        private PlanarImg<? extends NativeType<?>, ? extends ArrayDataAccess<?>> deserializePlanarImg(
                        final BufferedDataInputStream in, final long[] dims)
                        throws IOException {

                NativeType<?> type = (NativeType<?>) ImgDeSerializer
                                .readClass(in);

                PlanarImgContainerSamplerImpl sampler = new PlanarImgContainerSamplerImpl();

                PlanarImg<? extends NativeType<?>, ? extends ArrayDataAccess<?>> img = (PlanarImg<? extends NativeType<?>, ? extends ArrayDataAccess<?>>) new PlanarImgFactory()
                                .create(dims, type);

                for (int i = 0; i < img.numSlices(); i++) {
                        sampler.fwd();
                        in.readLArray(img.update(sampler)
                                        .getCurrentStorageArray());
                }

                return img;
        }

        /*
         * NTREEIMG
         */

        private void serializeNtreeImg(
                        final BufferedDataOutputStream out,
                        final NtreeImg<? extends NativeType<?>, ? extends NtreeAccess<?, ?>> img)
                        throws IOException {

                int n = img.numDimensions();
                int numChildren = 1 << n;

                ImgDeSerializer.writeClass(out, img.firstElement().getClass()
                                .getName());
                out.writeInt(numChildren);

                Ntree<?> tree = img.update(new NtreeImg.PositionProvider() {

                        @Override
                        public long[] getPosition() {
                                return new long[img.numDimensions()];
                        }

                }).getCurrentStorageNtree();

                serializeNtreeNode(new ObjectOutputStream(out),
                                tree.getRootNode());
        }

        private NtreeImg<? extends NativeType<?>, ? extends NtreeAccess<? extends Comparable<?>, ?>> deserializeNtreeImg(
                        final BufferedDataInputStream in, final long[] dims)
                        throws IOException {

                NativeType<?> type = (NativeType<?>) ImgDeSerializer
                                .readClass(in);
                int numChildren = in.readInt();

                final NtreeImg<? extends Type<?>, ? extends NtreeAccess<? extends Comparable<?>, ?>> img = new NtreeImgFactory()
                                .create(dims, type);
                Ntree<? extends Comparable<?>> tree = img.update(
                                new NtreeImg.PositionProvider() {

                                        @Override
                                        public long[] getPosition() {
                                                return new long[img
                                                                .numDimensions()];
                                        }

                                }).getCurrentStorageNtree();

                NtreeNode<? extends Comparable<?>> root = (NtreeNode<? extends Comparable<?>>) tree
                                .getRootNode();

                deserializeNtreeNode(new ObjectInputStream(in),
                                (NtreeNode<Object>) root, numChildren);

                return img;

        }

        @SuppressWarnings("unchecked")
        private void deserializeNtreeNode(ObjectInputStream in,
                        NtreeNode<Object> current, int numChildren)
                        throws IOException {
                try {
                        current.setValue(in.readObject());
                        if (!in.readBoolean())
                                return;

                        LinkedList<NtreeNode<Object>> queue = new LinkedList<NtreeNode<Object>>();
                        queue.add(current);

                        while (!queue.isEmpty()) {
                                current = queue.getFirst();
                                NtreeNode<Object>[] children = new NtreeNode[numChildren];
                                for (int i = 0; i < numChildren; i++) {
                                        children[i] = new NtreeNode<Object>(
                                                        current,
                                                        in.readObject());
                                        ;
                                        if (((Boolean) in.readObject())
                                                        .booleanValue())
                                                queue.add(children[i]);
                                }
                                queue.removeFirst().setChildren(children);
                        }
                } catch (Exception e) {
                        throw new RuntimeException(e);
                }
        }

        private void serializeNtreeNode(ObjectOutputStream out,
                        NtreeNode<? extends Comparable<?>> current)
                        throws IOException {

                out.writeObject(current.getValue());
                out.writeBoolean(current.getChildren() != null);

                LinkedList<NtreeNode<? extends Comparable<?>>> queue = new LinkedList<NtreeNode<? extends Comparable<?>>>();
                queue.add(current);

                if (current.getChildren() == null)
                        return;

                while (!queue.isEmpty()) {

                        current = queue.removeFirst();
                        for (NtreeNode<? extends Comparable<?>> child : current
                                        .getChildren()) {
                                out.writeObject(child.getValue());
                                out.writeObject(child.getChildren() != null);

                                if (child.getChildren() != null)
                                        queue.add(child);

                        }

                }
        }

        /*
         * CELLIMG
         */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        private synchronized void serializeCellImg(
                        BufferedDataOutputStream out,
                        CellImg<? extends NativeType<?>, ? extends ArrayDataAccess<?>, ? extends AbstractCell<?>> img)
                        throws IOException {
                ImgDeSerializer.writeClass(out, img.firstElement().getClass()
                                .getName());

                DirectCellCursor<? extends NativeType<?>, ? extends ArrayDataAccess<?>, ? extends AbstractCell<?>> cursorOnCells = new DirectCellCursor(
                                ((CellImg<? extends NativeType<?>, ? extends ArrayDataAccess<?>, ? extends AbstractCell<?>>) img)
                                                .cursor());

                boolean indicateStop = false;
                while (true) {
                        out.writeArray(((CellContainerSampler<? extends NativeType<?>, ? extends ArrayDataAccess<?>, ? extends AbstractCell<?>>) cursorOnCells)
                                        .getCell().getData()
                                        .getCurrentStorageArray());

                        if (indicateStop)
                                break;

                        cursorOnCells.moveToNextCell();

                        if (cursorOnCells.isLastCell())
                                indicateStop = true;

                }

        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        private CellImg<? extends NativeType<?>, ? extends ArrayDataAccess<?>, ? extends AbstractCell<?>> deserializeCellImg(
                        BufferedDataInputStream in, long[] dims)
                        throws IOException {
                NativeType<?> type = (NativeType<?>) ImgDeSerializer
                                .readClass(in);

                CellImg<? extends NativeType<?>, ? extends ArrayDataAccess<?>, ? extends AbstractCell<?>> cellImg = (CellImg<? extends NativeType<?>, ? extends ArrayDataAccess<?>, ? extends AbstractCell<?>>) new CellImgFactory()
                                .create(dims, type);

                DirectCellCursor<? extends NativeType<?>, ? extends ArrayDataAccess<?>, ? extends AbstractCell<?>> cursor = new DirectCellCursor(
                                cellImg.cursor());

                boolean indicateStop = false;
                while (true) {

                        in.readLArray(((CellContainerSampler<? extends NativeType<?>, ? extends ArrayDataAccess<?>, ? extends AbstractCell<?>>) cursor)
                                        .getCell().getData()
                                        .getCurrentStorageArray());

                        if (indicateStop)
                                break;

                        cursor.moveToNextCell();

                        if (cursor.isLastCell())
                                indicateStop = true;
                }

                return cellImg;

        }

        private class DirectCellCursor<T extends NativeType<T>, A extends ArrayDataAccess<A>, C extends AbstractCell<A>>
                        extends CellCursor<T, A, C> {

                protected DirectCellCursor(CellCursor<T, A, C> cursor) {
                        super(cursor);
                }

                /**
                 * Move cursor right before the first element of the next cell.
                 * Update type and index variables.
                 */
                public void moveToNextCell() {
                        cursorOnCells.fwd();
                        isNotLastCell = cursorOnCells.hasNext();
                        lastIndexInCell = (int) (getCell().size() - 1);
                        index = -1;
                        type.updateContainer(this);
                }

                public boolean isLastCell() {
                        return !isNotLastCell;
                }
        }

}
