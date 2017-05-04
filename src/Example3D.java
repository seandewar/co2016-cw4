import java.awt.BorderLayout;
import java.awt.Container;

import javax.media.j3d.Alpha;
import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Material;
import javax.media.j3d.PositionPathInterpolator;
import javax.media.j3d.RotationInterpolator;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JFrame;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseWheelZoom;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.universe.SimpleUniverse;

public class Example3D extends JFrame {

  private static final long serialVersionUID = 5548656989931406162L;

  // a very large bounding sphere that is intended to contain the whole scene we will be making -
  // largely used for scheduling
  private static final BoundingSphere SCENE_BOUNDS = new BoundingSphere(new Point3d(), 10000.0);

  // radius of the track
  private static final double TRACK_RADIUS = 75.0;

  public static void main(String[] args) {
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

  private BranchGroup createSky() {
    BranchGroup sky = new BranchGroup();

    // create a textured spherical sky (background)
    Background skyBg = new Background(new Color3f(0.7f, 1.0f, 1.0f));
    skyBg.setApplicationBounds(SCENE_BOUNDS);

    BranchGroup skyGeometry = new BranchGroup();
    Appearance skyApp = new Appearance();
    Texture skyTex = new TextureLoader(getClass().getResource("/sky.jpg"), this).getTexture();
    skyApp.setTexture(skyTex);
    Sphere skySphere = new Sphere(1.0f,
        Primitive.GENERATE_TEXTURE_COORDS | Primitive.GENERATE_NORMALS_INWARD, skyApp);
    skyGeometry.addChild(skySphere);

    skyBg.setGeometry(skyGeometry);

    // create the sky light (ambient)
    AmbientLight skyLight = new AmbientLight(new Color3f(0.4f, 0.4f, 0.4f));
    skyLight.setInfluencingBounds(SCENE_BOUNDS);

    // create the sun light (directional)
    DirectionalLight sunLight =
        new DirectionalLight(new Color3f(1.0f, 0.98f, 0.96f), new Vector3f(4.0f, -7.0f, -12.0f));
    sunLight.setInfluencingBounds(SCENE_BOUNDS);

    // add sky parts to sky group
    sky.addChild(skyBg);
    sky.addChild(skyLight);
    sky.addChild(sunLight);
    return sky;
  }

  private Shape3D createTrainCowPlowShape() {
    // geometry for the cow plow
    final Point3f[] cowPlowCoords = {
        // base triangle 1
        new Point3f(0.0f, 0.0f, 0.0f), new Point3f(1.0f, 0.0f, 0.0f), new Point3f(1.0f, 0.0f, 1.0f),

        // base triangle 2
        new Point3f(0.0f, 0.0f, 0.0f), new Point3f(1.0f, 0.0f, 1.0f), new Point3f(0.0f, 0.0f, 1.0f),

        // left side
        new Point3f(0.0f, 0.0f, 0.0f), new Point3f(0.0f, 0.0f, 1.0f), new Point3f(0.0f, 1.0f, 0.0f),

        // right side
        new Point3f(1.0f, 0.0f, 0.0f), new Point3f(1.0f, 1.0f, 0.0f), new Point3f(1.0f, 0.0f, 1.0f),

        // back triangle 1
        new Point3f(0.0f, 0.0f, 0.0f), new Point3f(0.0f, 1.0f, 0.0f), new Point3f(1.0f, 0.0f, 0.0f),

        // back triangle 2
        new Point3f(1.0f, 1.0f, 0.0f), new Point3f(1.0f, 0.0f, 0.0f), new Point3f(0.0f, 1.0f, 0.0f),

        // front triangle 1
        new Point3f(0.0f, 1.0f, 0.0f), new Point3f(0.0f, 0.0f, 1.0f), new Point3f(1.0f, 0.0f, 1.0f),

        // front triangle 2
        new Point3f(1.0f, 1.0f, 0.0f), new Point3f(0.0f, 1.0f, 0.0f),
        new Point3f(1.0f, 0.0f, 1.0f)};

    GeometryInfo geometryInfo = new GeometryInfo(GeometryInfo.TRIANGLE_ARRAY);
    geometryInfo.setCoordinates(cowPlowCoords);

    NormalGenerator normalGen = new NormalGenerator();
    normalGen.generateNormals(geometryInfo);

    // create shape from custom geometry
    Shape3D cowPlowShape = new Shape3D(geometryInfo.getGeometryArray());
    return cowPlowShape;
  }

  private BranchGroup createTrainPiston(final Vector3f pos, final Vector3f size,
      final Material mat) {
    BranchGroup piston = new BranchGroup();

    // set up piston transform
    TransformGroup pistonTg = new TransformGroup();
    pistonTg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

    // piston shape and appearance
    Appearance pistonApp = new Appearance();
    pistonApp.setMaterial(mat);
    Box pistonShape = new Box(size.getX(), size.getY(), size.getZ(), pistonApp);

    // create translation group for piston and then add piston to anim group
    TransformGroup pistonPosTg = new TransformGroup();
    Transform3D pistonTrans = new Transform3D();
    pistonTrans.setTranslation(pos);
    pistonPosTg.setTransform(pistonTrans);

    // add the translation group into the anim group so that the anim doesn't
    // reset the position of the piston while moving it
    pistonPosTg.addChild(pistonShape);
    pistonTg.addChild(pistonPosTg);

    // piston animation
    final float[] pistonInterpKnots = {0.0f, 0.5f, 1.0f};
    final Point3f[] pistonInterpPositions = {new Point3f(0.0f, 0.75f, 0.0f),
        new Point3f(0.0f, -0.75f, 0.0f), new Point3f(0.0f, 0.75f, 0.0f)};

    PositionPathInterpolator pistonInterper = new PositionPathInterpolator(new Alpha(-1, 125),
        pistonTg, new Transform3D(), pistonInterpKnots, pistonInterpPositions);
    pistonInterper.setSchedulingBounds(SCENE_BOUNDS);

    // add transform group to piston group
    piston.addChild(pistonTg);
    piston.addChild(pistonInterper);
    return piston;
  }

  private BranchGroup createTrainWheel(final Vector3f pos, float radius, final Material outerMat,
      final Material innerMat) {
    BranchGroup wheel = new BranchGroup();

    TransformGroup wheelTg = new TransformGroup();
    wheelTg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

    // create transforms for our wheels
    Transform3D wheelTrans = new Transform3D();
    wheelTrans.setTranslation(pos);
    Transform3D wheelRot = new Transform3D();
    wheelRot.rotX(Math.PI * 0.5f);
    wheelRot.rotZ(Math.PI * 0.5f);
    wheelTrans.mul(wheelRot);

    // create outer wheel section
    Appearance wheelOuterApp = new Appearance();
    wheelOuterApp.setMaterial(outerMat);
    TransformGroup wheelOuterTg = new TransformGroup(wheelTrans);
    Cylinder wheelOuter = new Cylinder(radius, 0.25f, wheelOuterApp);
    wheelOuterTg.addChild(wheelOuter);

    // create inner wheel section
    Appearance wheelInnerApp = new Appearance();
    wheelInnerApp.setMaterial(innerMat);
    TransformGroup wheelInnerTg = new TransformGroup(wheelTrans);
    Cylinder wheelInner = new Cylinder(radius * 0.65f, 0.35f, wheelInnerApp);
    wheelInnerTg.addChild(wheelInner);

    // add wheel parts to group
    wheelTg.addChild(wheelOuterTg);
    wheelTg.addChild(wheelInnerTg);

    // rotation animation for wheels
    RotationInterpolator wheelRotator = new RotationInterpolator(new Alpha(-1, 900), wheelTg);
    Transform3D wheelRotatorAxis = new Transform3D();
    wheelRotatorAxis.rotZ(Math.PI * 0.5);
    wheelRotatorAxis.setTranslation(pos);
    wheelRotator.setTransformAxis(wheelRotatorAxis);
    wheelRotator.setSchedulingBounds(SCENE_BOUNDS);

    // add wheel group and rotator to branch group
    wheel.addChild(wheelTg);
    wheel.addChild(wheelRotator);
    return wheel;
  }

  private BranchGroup createTrain() {
    BranchGroup train = new BranchGroup();

    // the group for the train's parts to be animated
    TransformGroup trainTg = new TransformGroup();
    Transform3D trainTrans = new Transform3D();
    trainTrans.rotY(Math.PI * 0.5);
    trainTrans.setTranslation(new Vector3d(0.0, 0.0, -TRACK_RADIUS));
    trainTg.setTransform(trainTrans);

    // the group for the train's animation to act upon; allows offset to be
    // set properly so that the train follows the tracks
    TransformGroup trainAnimTg = new TransformGroup();
    trainAnimTg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

    // train materials
    final Material bodyPaintMat = new Material(new Color3f(1.0f, 0.0f, 0.0f), new Color3f(),
        new Color3f(1.0f, 0.0f, 0.0f), new Color3f(1.0f, 1.0f, 1.0f), 25.0f);
    final Material bodyMetalMat = new Material(new Color3f(0.3f, 0.3f, 0.3f), new Color3f(),
        new Color3f(0.3f, 0.3f, 0.3f), new Color3f(1.0f, 1.0f, 1.0f), 20.0f);
    final Material windowMat = new Material(new Color3f(0.4f, 1.0f, 1.0f), new Color3f(),
        new Color3f(0.4f, 1.0f, 1.0f), new Color3f(1.0f, 1.0f, 1.0f), 15.0f);
    final Material wheelMetalMat = new Material(new Color3f(0.6f, 0.6f, 0.6f), new Color3f(),
        new Color3f(0.6f, 0.6f, 0.6f), new Color3f(1.0f, 1.0f, 1.0f), 35.0f);

    // train body
    Appearance body1App = new Appearance();
    body1App.setMaterial(bodyPaintMat);
    Transform3D body1Trans = new Transform3D();
    body1Trans.setTranslation(new Vector3f(0.0f, 0.0f, -0.5f));
    Transform3D body1Rot = new Transform3D();
    body1Rot.rotX(Math.PI * 0.5f);
    body1Trans.mul(body1Rot);
    TransformGroup body1Tg = new TransformGroup(body1Trans);
    Cylinder body1 = new Cylinder(2.5f, 5.2f, body1App);
    body1Tg.addChild(body1);

    Appearance body2App = new Appearance();
    body2App.setMaterial(bodyPaintMat);
    Transform3D body2Trans = new Transform3D();
    body2Trans.setTranslation(new Vector3f(0.0f, 1.95f, 4.5f));
    TransformGroup body2Tg = new TransformGroup(body2Trans);
    Box body2 = new Box(3.5f, 4.0f, 2.0f, body2App);
    body2Tg.addChild(body2);

    Appearance body3App = new Appearance();
    body3App.setMaterial(bodyMetalMat);
    Transform3D body3Trans = new Transform3D();
    body3Trans.setTranslation(new Vector3f(0.0f, -3.0f, 1.75f));
    TransformGroup body3Tg = new TransformGroup(body3Trans);
    Box body3 = new Box(3.5f, 1.0f, 4.75f, body3App);
    body3Tg.addChild(body3);

    Appearance body4App = new Appearance();
    body4App.setMaterial(bodyMetalMat);
    Transform3D body4Trans = new Transform3D();
    body4Trans.setTranslation(new Vector3f(0.0f, 0.0f, 2.5f));
    Transform3D body4Rot = new Transform3D();
    body4Rot.rotX(Math.PI * 0.5f);
    body4Trans.mul(body4Rot);
    TransformGroup body4Tg = new TransformGroup(body4Trans);
    Cylinder body4 = new Cylinder(2.75f, 1.0f, body4App);
    body4Tg.addChild(body4);

    Appearance body5App = new Appearance();
    body5App.setMaterial(bodyMetalMat);
    Transform3D body5Trans = new Transform3D();
    body5Trans.setTranslation(new Vector3f(0.0f, 0.0f, -2.75f));
    Transform3D body5Rot = new Transform3D();
    body5Rot.rotX(Math.PI * 0.5f);
    body5Trans.mul(body5Rot);
    TransformGroup body5Tg = new TransformGroup(body5Trans);
    Cylinder body5 = new Cylinder(2.75f, 0.5f, body5App);
    body5Tg.addChild(body5);

    Appearance body6App = new Appearance();
    body6App.setMaterial(bodyMetalMat);
    Transform3D body6Trans = new Transform3D();
    body6Trans.setTranslation(new Vector3f(0.0f, 6.2f, 4.5f));
    TransformGroup body6Tg = new TransformGroup(body6Trans);
    Box body6 = new Box(3.5f, 0.25f, 2.0f, body6App);
    body6Tg.addChild(body6);

    // cow plow
    Appearance cowPlowApp = new Appearance();
    cowPlowApp.setMaterial(bodyMetalMat);
    Transform3D cowPlowTrans = new Transform3D();
    cowPlowTrans.setScale(new Vector3d(7.5, 2.5, 2.0));
    cowPlowTrans.setTranslation(new Vector3f(3.75f, -4.5f, -2.95f));
    Transform3D cowPlowRot = new Transform3D();
    cowPlowRot.rotY(Math.PI);
    cowPlowTrans.mul(cowPlowRot);
    TransformGroup cowPlowTg = new TransformGroup(cowPlowTrans);
    Shape3D cowPlow = createTrainCowPlowShape();
    cowPlow.setAppearance(cowPlowApp);
    cowPlowTg.addChild(cowPlow);

    // chimney
    Appearance chimneyApp = new Appearance();
    chimneyApp.setMaterial(bodyMetalMat);
    Transform3D chimneyTrans = new Transform3D();
    chimneyTrans.setTranslation(new Vector3f(0.0f, 3.0f, -1.5f));
    TransformGroup chimneyTg = new TransformGroup(chimneyTrans);
    Cylinder chimney = new Cylinder(0.5f, 3.0f, chimneyApp);
    chimneyTg.addChild(chimney);

    // windows
    Transform3D windowTrans = new Transform3D();
    windowTrans.setTranslation(new Vector3f(0.0f, 3.5f, 4.5f));
    TransformGroup windowTg = new TransformGroup(windowTrans);

    Appearance windowApp = new Appearance();
    windowApp.setMaterial(windowMat);
    Box window = new Box(3.75f, 1.2f, 1.45f, windowApp);
    windowTg.addChild(window);

    Appearance windowFrameApp = new Appearance();
    windowFrameApp.setMaterial(bodyMetalMat);
    Box windowFrame = new Box(3.65f, 1.35f, 1.55f, windowFrameApp);
    windowTg.addChild(windowFrame);

    // add train parts
    trainTg.addChild(body1Tg);
    trainTg.addChild(body2Tg);
    trainTg.addChild(body3Tg);
    trainTg.addChild(body4Tg);
    trainTg.addChild(body5Tg);
    trainTg.addChild(body6Tg);
    trainTg.addChild(cowPlowTg);
    trainTg.addChild(chimneyTg);
    trainTg.addChild(windowTg);

    // add wheels
    // large wheels
    trainTg.addChild(
        createTrainWheel(new Vector3f(3.5f, -2.8f, 4.5f), 2.5f, wheelMetalMat, bodyMetalMat));
    trainTg.addChild(
        createTrainWheel(new Vector3f(-3.5f, -2.8f, 4.5f), 2.5f, wheelMetalMat, bodyMetalMat));
    // smaller wheels
    trainTg.addChild(
        createTrainWheel(new Vector3f(3.5f, -4.0f, 0.8f), 1.25f, wheelMetalMat, bodyMetalMat));
    trainTg.addChild(
        createTrainWheel(new Vector3f(3.5f, -4.0f, -1.75f), 1.25f, wheelMetalMat, bodyMetalMat));
    trainTg.addChild(
        createTrainWheel(new Vector3f(-3.5f, -4.0f, 0.8f), 1.25f, wheelMetalMat, bodyMetalMat));
    trainTg.addChild(
        createTrainWheel(new Vector3f(-3.5f, -4.0f, -1.75f), 1.25f, wheelMetalMat, bodyMetalMat));

    // add pistons
    trainTg.addChild(createTrainPiston(new Vector3f(3.55f, -4.0f, -0.475f),
        new Vector3f(0.2f, 0.1f, 2.0f), wheelMetalMat));
    trainTg.addChild(createTrainPiston(new Vector3f(-3.55f, -4.0f, -0.475f),
        new Vector3f(0.2f, 0.1f, 2.0f), wheelMetalMat));

    // add the train's group to the transform group to be animated so that the animation
    // is offset correctly and follows the tracks
    trainAnimTg.addChild(trainTg);

    // train moving animation
    RotationInterpolator rotator =
        new RotationInterpolator(new Alpha(-1, (long) (7250 * TRACK_RADIUS / 75.0)), trainAnimTg);
    rotator.setSchedulingBounds(SCENE_BOUNDS);

    // add train and movement animator to group
    train.addChild(trainAnimTg);
    train.addChild(rotator);
    return train;
  }

  private BranchGroup createTrainTracks() {
    BranchGroup tracks = new BranchGroup();

    // rail material
    final Material railMat = new Material(new Color3f(0.5f, 0.5f, 0.5f), new Color3f(),
        new Color3f(0.6f, 0.6f, 0.6f), new Color3f(1.0f, 1.0f, 1.0f), 30.0f);

    // generate and add tracks
    final int numRails = 80;
    for (int i = 0; i < numRails; ++i) {
      // create rail transform to position and rotate the rail properly
      final double mul = i / (double) numRails;

      TransformGroup rail = new TransformGroup();
      Transform3D railTrans = new Transform3D();
      railTrans.setTranslation(new Vector3d(TRACK_RADIUS * Math.cos(2.0 * Math.PI * mul), -5.0,
          TRACK_RADIUS * -Math.sin(2.0 * Math.PI * mul)));
      Transform3D railRot = new Transform3D();
      railRot.rotY(2.0f * Math.PI * mul);
      railTrans.mul(railRot);
      rail.setTransform(railTrans);

      // create rail shape
      Appearance railApp = new Appearance();
      railApp.setMaterial(railMat);
      Box railShape = new Box(4.0f, 0.25f, 0.25f, railApp);

      // add to this rail's own group to be put on the tracks
      rail.addChild(railShape);
      tracks.addChild(rail);
    }

    return tracks;
  }

  private void addEnvironmentToScene(BranchGroup sceneRoot) {
    // add different objects to scene
    sceneRoot.addChild(createSky());
    sceneRoot.addChild(createTrain());
    sceneRoot.addChild(createTrainTracks());
  }

  private BranchGroup createScene() {
    // create the scene graph
    BranchGroup sceneRoot = new BranchGroup();

    // get and configure our view's transform a little...
    TransformGroup viewTransform = universe.getViewingPlatform().getViewPlatformTransform();
    Transform3D initialTrans = new Transform3D();
    initialTrans.setScale(10.0);
    initialTrans.setTranslation(new Vector3f(0.0f, 250.0f, 0.0f));
    Transform3D initialRot = new Transform3D();
    initialRot.rotZ(Math.PI);
    initialRot.rotX(-Math.PI * 0.5);
    initialTrans.mul(initialRot);
    viewTransform.setTransform(initialTrans);

    // add the 3d environment to our scene
    addEnvironmentToScene(sceneRoot);

    // create mouse behaviours so we can interact with the scene a bit
    MouseRotate rotBehaviour = new MouseRotate(viewTransform);
    rotBehaviour.setSchedulingBounds(SCENE_BOUNDS);
    rotBehaviour.setFactor(0.005);
    sceneRoot.addChild(rotBehaviour);

    MouseTranslate transBehaviour = new MouseTranslate(viewTransform);
    transBehaviour.setSchedulingBounds(SCENE_BOUNDS);
    transBehaviour.setFactor(0.05);
    sceneRoot.addChild(transBehaviour);

    MouseWheelZoom zoomBehaviour = new MouseWheelZoom(viewTransform);
    zoomBehaviour.setSchedulingBounds(SCENE_BOUNDS);
    zoomBehaviour.setFactor(2.0);
    sceneRoot.addChild(zoomBehaviour);

    // compile the scene and return the root node of the graph
    sceneRoot.compile();
    return sceneRoot;
  }

}
