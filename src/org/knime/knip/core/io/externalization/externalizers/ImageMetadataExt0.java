package org.knime.knip.core.io.externalization.externalizers;

import net.imglib2.display.ColorTable16;
import net.imglib2.display.ColorTable8;
import net.imglib2.meta.ImageMetadata;

import org.knime.knip.core.data.img.ImageMetadataImpl;
import org.knime.knip.core.io.externalization.BufferedDataInputStream;
import org.knime.knip.core.io.externalization.BufferedDataOutputStream;
import org.knime.knip.core.io.externalization.Externalizer;

public class ImageMetadataExt0 implements Externalizer<ImageMetadata> {

    @Override
    public String getId() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Class<ImageMetadata> getType() {
        return ImageMetadata.class;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public ImageMetadata read(final BufferedDataInputStream in) throws Exception {
        final ImageMetadataImpl obj = new ImageMetadataImpl();

        // Valid bits are deserialized
        obj.setValidBits(in.readInt());

        // Channel Min/Max are deserialized
        final int numChannels = in.readInt();

        for (int c = 0; c < numChannels; c++) {
            obj.setChannelMinimum(c, in.readDouble());
            obj.setChannelMaximum(c, in.readDouble());
        }

        // Colortables are deserialized
        final int numColorTables = in.readInt();
        obj.initializeColorTables(numColorTables);

        for (int t = 0; t < numColorTables; t++) {

            if (in.readBoolean()) {
                final int componentCount8 = in.readInt();
                final int length8 = in.readInt();
                final byte[][] ct8 = new byte[componentCount8][length8];

                for (int c = 0; c < componentCount8; c++) {
                    for (int k = 0; k < length8; k++) {
                        ct8[c][k] = in.readByte();
                    }
                }

                obj.setColorTable(new ColorTable8(ct8), t);
            }

            if (in.readBoolean()) {
                final int componentCount16 = in.readInt();
                final int length16 = in.readInt();
                final short[][] ct16 = new short[componentCount16][length16];

                for (int c = 0; c < componentCount16; c++) {
                    for (int k = 0; k < componentCount16; k++) {
                        ct16[c][k] = in.readShort();
                    }
                }

                obj.setColorTable(new ColorTable16(ct16), t);
            }
        }

        return obj;
    }

    // Invalid: As ImageMetadataExt1
    @Deprecated
    @Override
    public void write(final BufferedDataOutputStream out, final ImageMetadata obj)
            throws Exception {

        // // Valid bits
        // out.writeInt(obj.getValidBits());
        //
        // // Channels are serialized
        // int numChannels = obj.getCompositeChannelCount();
        // out.writeInt(numChannels);
        //
        // for (int c = 0; c < numChannels; c++) {
        // out.writeDouble(obj.getChannelMinimum(c));
        // out.writeDouble(obj.getChannelMaximum(c));
        // }
        //
        // // Color Tables are serialized
        // int numTables = obj.getColorTableCount();
        // out.writeInt(numTables);
        //
        // for (int t = 0; t < numTables; t++) {
        //
        // out.writeBoolean(obj.getColorTable8(t) != null);
        // if (obj.getColorTable8(t) != null) {
        // ColorTable8 ct8 = obj.getColorTable8(t);
        // int componentCount8 = ct8.getComponentCount();
        // out.writeInt(componentCount8);
        // out.writeInt(ct8.getLength());
        //
        // for (int c = 0; c < componentCount8; c++)
        // for (int k = 0; k < ct8.getLength(); k++)
        // out.writeByte((byte) ct8.get(c,
        // k));
        //
        // }
        //
        // out.writeBoolean(obj.getColorTable16(t) != null);
        // if (obj.getColorTable16(t) != null) {
        // ColorTable16 ct16 = obj.getColorTable16(t);
        // int componentCount16 = ct16.getComponentCount();
        // out.writeInt(componentCount16);
        // out.writeInt(ct16.getLength());
        //
        // for (int c = 0; c < componentCount16; c++)
        // for (int k = 0; k < ct16.getLength(); k++)
        // out.writeShort((short) ct16
        // .get(c, k));
        //
        // }
        //
        // }

    }
}
