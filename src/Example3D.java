import java.awt.BorderLayout;
import java.awt.Container;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JFrame;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseWheelZoom;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.universe.SimpleUniverse;

public class Example3D extends JFrame {

  private static final long serialVersionUID = 5548656989931406162L;

  public static void main(String[] args) {
    // create frame and init scene
    new Example3D();
  }

  private SimpleUniverse universe;

  public Example3D() {
    // config frame
    setTitle("Sean's Coursework 4");
    setSize(800, 600);
    setDefaultCloseOperation(EXIT_ON_CLOSE);

    // init the UI
    Container pane = getContentPane();
    pane.setLayout(new BorderLayout());

    // create 3d canvas and universe
    Canvas3D canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
    universe = new SimpleUniverse(canvas);
    universe.getViewingPlatform().setNominalViewingTransform();
    pane.add(canvas);

    // create the scene and add it to our universe that draws to our canvas
    BranchGroup scene = createScene();
    universe.addBranchGraph(scene);

    // make frame visible
    setVisible(true);
  }

  private BranchGroup createScene() {
    // create the scene graph
    BranchGroup sceneRoot = new BranchGroup();

    // get and configure our view's transform a little...
    TransformGroup viewTransform = universe.getViewingPlatform().getViewPlatformTransform();
    final Transform3D initialTrans = new Transform3D();
    initialTrans.setScale(5.0);
    initialTrans.setTranslation(new Vector3d(0.0, 0.0, -10.0));
    viewTransform.setTransform(initialTrans);

    // create a test box
    Appearance boxAppearance = new Appearance();
    boxAppearance.setColoringAttributes(
        new ColoringAttributes(0.0f, 0.0f, 1.0f, ColoringAttributes.SHADE_FLAT));
    Box box = new Box(1.0f, 1.0f, 1.0f, boxAppearance);
    sceneRoot.addChild(box);

    // create mouse behaviours so we can interact with the scene a bit
    MouseRotate rotBehaviour = new MouseRotate(viewTransform);
    rotBehaviour.setSchedulingBounds(new BoundingSphere());
    sceneRoot.addChild(rotBehaviour);

    MouseTranslate transBehaviour = new MouseTranslate(viewTransform);
    transBehaviour.setSchedulingBounds(new BoundingSphere());
    sceneRoot.addChild(transBehaviour);

    MouseWheelZoom zoomBehaviour = new MouseWheelZoom(viewTransform);
    zoomBehaviour.setSchedulingBounds(new BoundingSphere());
    sceneRoot.addChild(zoomBehaviour);

    // compile the scene and return the root node of the graph
    sceneRoot.compile();
    return sceneRoot;
  }

}
