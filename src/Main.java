import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class Main<T extends RealType<T> & NativeType<T>> {

        // public static void main(String[] args) {
        // EllipseRegionOfInterest ellipse = new EllipseRegionOfInterest(2);
        // ellipse.setOrigin(new double[] { 1, 1 });
        // ellipse.setRadius(1);
        //
        // Img<BitType> img = new ArrayImgFactory<BitType>().create(
        // new long[] { 3, 3 }, new BitType());
        //
        // RandomAccess<BitType> randomAccess = img.randomAccess();
        //
        // Cursor<BitType> cursor = ellipse
        // .getIterableIntervalOverROI(img).cursor();
        //
        // long[] pos = new long[2];
        // while (cursor.hasNext()) {
        // cursor.fwd();
        // cursor.localize(pos);
        // randomAccess.setPosition(cursor);
        // randomAccess.get().set(true);
        // System.out.println(Arrays.toString(pos));
        // }
        // }

        // private static ImgOpener m_opener;
        //
        // public static void main(String[] args) throws ImgIOException,
        // IncompatibleTypeException {
        //
        //
        //
        // // m_opener = new ImgOpener();
        //
        // // new Main().startUp(20);
        // }

        // private void startUp(int i) throws ImgIOException,
        // IncompatibleTypeException {

        // Img<BitType> img = new ArrayImgFactory<BitType>().create(new int[] {
        // 1,
        // 1 }, new BitType());
        //
        // SubImg<BitType> subImg = new SubImg<BitType>(img, new FinalInterval(
        // new long[] { 0, 0 }, new long[] { 0, 0 }));
        //
        //
        // List<PluginInfo<?>> plugins = new ArrayList<PluginInfo<?>>();
        // ImageJPluginFinder finder = new ImageJPluginFinder();
        // finder.findPlugins(plugins);
        //
        // for (PluginInfo<?> info : plugins) {
        //
        // if (info instanceof PluginModuleInfo) {
        // System.out.println(info.getTitle());
        //
        // PluginModule<ImageJPlugin> mod = new PluginModule<ImageJPlugin>(
        // (PluginModuleInfo<ImageJPlugin>) info);
        // for (ModuleItem item : ((PluginModuleInfo<ImageJPlugin>) info)
        // .inputs()) {
        // System.out.println(item.getType().getSimpleName());
        // }
        // } else {
        // // System.out.println("THIS IS STRANGE" + info.getTitle());
        // }
        //
        // }

        // ImgPlus<T> imgPlus = m_opener
        //
        // .openImg(
        // "/home/hornm/cell_images/itensitymeasure/wt1_cell1011_L1_Sum.lsm",
        // new PlanarImgFactory());
        //
        // JFrame frame = new JFrame();
        // ImgViewer<BitType, Img<BitType>> viewer = ViewerFactory
        // .createImgViewer(i);
        // viewer.setImg(subImg, new ImgPlus<BitType>(img), new
        // ImgPlus<BitType>(
        // img));
        // // viewer.load();
        // viewer.getEventService().publish(
        // EventType.FILECHOSER_SELECTEDFILES_CHG,
        // new Object[] { new String[] {
        // "D:\\Testpics\\110216_NCP Wld_P1_20x_Ph.tif",
        // "D:\\Testpics\\B06_merged.tif" } });
        //
        // frame.add(viewer);
        // frame.pack();
        // frame.setVisible(true);
        // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // }
}
