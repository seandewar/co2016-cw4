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
import javax.media.j3d.Light;
import javax.media.j3d.Material;
import javax.media.j3d.PointLight;
import javax.media.j3d.PositionPathInterpolator;
import javax.media.j3d.RenderingAttributes;
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
  private static final double TRACK_RADIUS = 45.0;

  // materials for the collision testing light shape
  public static final Material TESTER_LIGHT_OFF = new Material(new Color3f(0.1f, 0.1f, 0.1f),
      new Color3f(), new Color3f(0.1f, 0.1f, 0.1f), new Color3f(1.0f, 1.0f, 1.0f), 20.0f);
  public static final Material TESTER_LIGHT_ON =
      new Material(new Color3f(0.1f, 0.1f, 0.1f), new Color3f(1.0f, 0.0f, 0.0f),
          new Color3f(0.1f, 0.1f, 0.1f), new Color3f(1.0f, 1.0f, 1.0f), 20.0f);

  public static void main(String[] args) {
    new Example3D();
  }

  private SimpleUniverse universe;

  // loaded textures
  private Texture skyTex = new TextureLoader(getClass().getResource("/sky.jpg"), this).getTexture();
  private Texture metal1Tex =
      new TextureLoader(getClass().getResource("/metal1.jpg"), this).getTexture();
  private Texture metal2Tex =
      new TextureLoader(getClass().getResource("/metal2.jpg"), this).getTexture();
  private Texture woodTex =
      new TextureLoader(getClass().getResource("/wood.jpg"), this).getTexture();
  private Texture glassTex =
      new TextureLoader(getClass().getResource("/glass.jpg"), this).getTexture();
  private Texture grassTex =
      new TextureLoader(getClass().getResource("/grass.jpg"), this).getTexture();

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
      final Appearance appearance) {
    BranchGroup piston = new BranchGroup();

    // set up piston transform
    TransformGroup pistonTg = new TransformGroup();
    pistonTg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

    // piston shape and appearance
    Box pistonShape = new Box(size.getX(), size.getY(), size.getZ(),
        Primitive.GENERATE_TEXTURE_COORDS | Primitive.GENERATE_NORMALS, appearance);

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

    PositionPathInterpolator pistonInterper = new PositionPathInterpolator(new Alpha(-1, 95),
        pistonTg, new Transform3D(), pistonInterpKnots, pistonInterpPositions);
    pistonInterper.setSchedulingBounds(SCENE_BOUNDS);

    // add transform group to piston group
    piston.addChild(pistonTg);
    piston.addChild(pistonInterper);
    return piston;
  }

  private BranchGroup createTrainWheel(final Vector3f pos, float radius,
      final Appearance outerAppearance, final Appearance innerAppearance) {
    BranchGroup wheel = new BranchGroup();

    TransformGroup wheelTg = new TransformGroup();
    wheelTg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

    // create transforms for our wheels
    Transform3D wheelTrans = new Transform3D();
    wheelTrans.rotX(Math.PI * 0.5f);
    wheelTrans.rotZ(Math.PI * 0.5f);
    wheelTrans.setTranslation(pos);

    // create outer wheel section
    TransformGroup wheelOuterTg = new TransformGroup(wheelTrans);
    Cylinder wheelOuter = new Cylinder(radius, 0.25f,
        Primitive.GENERATE_TEXTURE_COORDS | Primitive.GENERATE_NORMALS, outerAppearance);
    wheelOuterTg.addChild(wheelOuter);

    // create inner wheel section
    TransformGroup wheelInnerTg = new TransformGroup(wheelTrans);
    Cylinder wheelInner = new Cylinder(radius * 0.65f, 0.35f,
        Primitive.GENERATE_TEXTURE_COORDS | Primitive.GENERATE_NORMALS, innerAppearance);
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
    final Material bodyPaintMat = new Material(new Color3f(1.0f, 0.5f, 0.2f), new Color3f(),
        new Color3f(1.0f, 0.5f, 0.2f), new Color3f(1.0f, 1.0f, 1.0f), 25.0f);
    final Material bodyMetalMat = new Material(new Color3f(0.3f, 0.3f, 0.3f), new Color3f(),
        new Color3f(0.3f, 0.3f, 0.3f), new Color3f(1.0f, 1.0f, 1.0f), 20.0f);
    final Material windowMat = new Material(new Color3f(0.4f, 1.0f, 1.0f), new Color3f(),
        new Color3f(0.4f, 1.0f, 1.0f), new Color3f(1.0f, 1.0f, 1.0f), 10.0f);
    final Material wheelMetalMat = new Material(new Color3f(0.6f, 0.6f, 0.6f), new Color3f(),
        new Color3f(0.6f, 0.6f, 0.6f), new Color3f(1.0f, 1.0f, 1.0f), 35.0f);

    // train appearances
    Appearance wheelMetalApp = new Appearance();
    wheelMetalApp.setMaterial(wheelMetalMat);
    wheelMetalApp.setTexture(metal1Tex);

    Appearance windowApp = new Appearance();
    windowApp.setMaterial(windowMat);
    windowApp.setTexture(glassTex);

    Appearance bodyPaintApp = new Appearance();
    bodyPaintApp.setMaterial(bodyPaintMat);

    Appearance bodyMetalApp = new Appearance();
    bodyMetalApp.setMaterial(bodyMetalMat);
    bodyMetalApp.setTexture(metal2Tex);

    // train body
    Transform3D body1Trans = new Transform3D();
    body1Trans.rotX(Math.PI * 0.5f);
    body1Trans.setTranslation(new Vector3f(0.0f, 0.0f, -0.5f));
    TransformGroup body1Tg = new TransformGroup(body1Trans);
    Cylinder body1 = new Cylinder(2.5f, 5.2f, bodyPaintApp);
    body1Tg.addChild(body1);

    Transform3D body2Trans = new Transform3D();
    body2Trans.setTranslation(new Vector3f(0.0f, 1.95f, 4.5f));
    TransformGroup body2Tg = new TransformGroup(body2Trans);
    Box body2 = new Box(3.5f, 4.0f, 2.0f, bodyPaintApp);
    body2Tg.addChild(body2);

    Transform3D body3Trans = new Transform3D();
    body3Trans.setTranslation(new Vector3f(0.0f, -3.0f, 1.75f));
    TransformGroup body3Tg = new TransformGroup(body3Trans);
    Box body3 = new Box(3.5f, 1.0f, 4.75f,
        Primitive.GENERATE_TEXTURE_COORDS | Primitive.GENERATE_NORMALS, bodyMetalApp);
    body3Tg.addChild(body3);

    Transform3D body4Trans = new Transform3D();
    body4Trans.rotX(Math.PI * 0.5f);
    body4Trans.setTranslation(new Vector3f(0.0f, 0.0f, 2.5f));
    TransformGroup body4Tg = new TransformGroup(body4Trans);
    Cylinder body4 = new Cylinder(2.75f, 1.0f,
        Primitive.GENERATE_TEXTURE_COORDS | Primitive.GENERATE_NORMALS, bodyMetalApp);
    body4Tg.addChild(body4);

    Transform3D body5Trans = new Transform3D();
    body5Trans.rotX(Math.PI * 0.5f);
    body5Trans.setTranslation(new Vector3f(0.0f, 0.0f, -2.75f));
    TransformGroup body5Tg = new TransformGroup(body5Trans);
    Cylinder body5 = new Cylinder(2.75f, 0.5f,
        Primitive.GENERATE_TEXTURE_COORDS | Primitive.GENERATE_NORMALS, bodyMetalApp);
    body5Tg.addChild(body5);

    Transform3D body6Trans = new Transform3D();
    body6Trans.setTranslation(new Vector3f(0.0f, 6.2f, 4.5f));
    TransformGroup body6Tg = new TransformGroup(body6Trans);
    Box body6 = new Box(3.5f, 0.25f, 2.0f,
        Primitive.GENERATE_TEXTURE_COORDS | Primitive.GENERATE_NORMALS, bodyMetalApp);
    body6Tg.addChild(body6);

    // cow plow
    Transform3D cowPlowTrans = new Transform3D();
    cowPlowTrans.rotY(Math.PI);
    cowPlowTrans.setScale(new Vector3d(7.5, 2.5, 2.0));
    cowPlowTrans.setTranslation(new Vector3f(3.75f, -4.5f, -2.95f));
    TransformGroup cowPlowTg = new TransformGroup(cowPlowTrans);
    Shape3D cowPlow = createTrainCowPlowShape();
    cowPlow.setAppearance(bodyMetalApp);
    cowPlowTg.addChild(cowPlow);

    // chimney
    Transform3D chimneyTrans = new Transform3D();
    chimneyTrans.setTranslation(new Vector3f(0.0f, 3.0f, -1.5f));
    TransformGroup chimneyTg = new TransformGroup(chimneyTrans);
    Cylinder chimney = new Cylinder(0.5f, 3.0f,
        Primitive.GENERATE_TEXTURE_COORDS | Primitive.GENERATE_NORMALS, bodyMetalApp);
    chimneyTg.addChild(chimney);

    // windows
    Transform3D windowTrans = new Transform3D();
    windowTrans.setTranslation(new Vector3f(0.0f, 3.5f, 4.5f));
    TransformGroup windowTg = new TransformGroup(windowTrans);

    Box window = new Box(3.75f, 1.2f, 1.45f, windowApp);
    windowTg.addChild(window);

    Box windowFrame = new Box(3.65f, 1.35f, 1.55f,
        Primitive.GENERATE_TEXTURE_COORDS | Primitive.GENERATE_NORMALS, bodyMetalApp);
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
        createTrainWheel(new Vector3f(3.5f, -2.8f, 4.5f), 2.5f, wheelMetalApp, bodyMetalApp));
    trainTg.addChild(
        createTrainWheel(new Vector3f(-3.5f, -2.8f, 4.5f), 2.5f, wheelMetalApp, bodyMetalApp));
    // smaller wheels
    trainTg.addChild(
        createTrainWheel(new Vector3f(3.5f, -4.0f, 0.8f), 1.25f, wheelMetalApp, bodyMetalApp));
    trainTg.addChild(
        createTrainWheel(new Vector3f(3.5f, -4.0f, -1.75f), 1.25f, wheelMetalApp, bodyMetalApp));
    trainTg.addChild(
        createTrainWheel(new Vector3f(-3.5f, -4.0f, 0.8f), 1.25f, wheelMetalApp, bodyMetalApp));
    trainTg.addChild(
        createTrainWheel(new Vector3f(-3.5f, -4.0f, -1.75f), 1.25f, wheelMetalApp, bodyMetalApp));

    // add pistons
    trainTg.addChild(createTrainPiston(new Vector3f(3.55f, -4.0f, -0.475f),
        new Vector3f(0.2f, 0.1f, 2.0f), wheelMetalApp));
    trainTg.addChild(createTrainPiston(new Vector3f(-3.55f, -4.0f, -0.475f),
        new Vector3f(0.2f, 0.1f, 2.0f), wheelMetalApp));

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

    // rail materials
    final Material railMetalMat = new Material(new Color3f(0.5f, 0.5f, 0.5f), new Color3f(),
        new Color3f(0.6f, 0.6f, 0.6f), new Color3f(1.0f, 1.0f, 1.0f), 30.0f);
    final Material railWoodMat = new Material(new Color3f(0.43f, 0.22f, 0.08f), new Color3f(),
        new Color3f(0.43f, 0.22f, 0.08f), new Color3f(), 0.0f);

    // rail appearances
    Appearance railMetalApp = new Appearance();
    railMetalApp.setMaterial(railMetalMat);
    railMetalApp.setTexture(metal1Tex);

    Appearance railWoodApp = new Appearance();
    railWoodApp.setMaterial(railWoodMat);
    railWoodApp.setTexture(woodTex);

    // generate and add tracks
    final int numRails = 40;
    for (int i = 0; i < numRails; ++i) {
      // create rail transform to position and rotate the rail properly
      final double mul = i / (double) numRails;
      final Vector3d posMetalRail = new Vector3d(TRACK_RADIUS * Math.cos(2.0 * Math.PI * mul), -5.0,
          TRACK_RADIUS * -Math.sin(2.0 * Math.PI * mul));
      final Vector3d posWoodRailOffset = new Vector3d(3.5 * Math.cos(2.0 * Math.PI * mul), 0.0,
          3.5 * -Math.sin(2.0 * Math.PI * mul));

      // metal rails
      TransformGroup railMetal = new TransformGroup();
      Transform3D railMetalTrans = new Transform3D();
      railMetalTrans.rotY(2.0f * Math.PI * mul);
      railMetalTrans.setTranslation(posMetalRail);
      railMetal.setTransform(railMetalTrans);

      Box railMetalShape = new Box(4.0f, 0.25f, 0.35f,
          Primitive.GENERATE_TEXTURE_COORDS | Primitive.GENERATE_NORMALS, railMetalApp);

      // wooden rails
      TransformGroup railWood1 = new TransformGroup();
      Transform3D railWood1Trans = new Transform3D();
      railWood1Trans.rotY(2.0f * Math.PI * mul + (Math.PI * 0.5f));
      railWood1Trans.setTranslation(new Vector3d(posMetalRail.getX() + posWoodRailOffset.getX(),
          posMetalRail.getY(), posMetalRail.getZ() + posWoodRailOffset.getZ()));
      railWood1.setTransform(railWood1Trans);
      Box railWood1Shape = new Box(4.0f, 0.3f, 0.25f,
          Primitive.GENERATE_TEXTURE_COORDS | Primitive.GENERATE_NORMALS, railWoodApp);

      TransformGroup railWood2 = new TransformGroup();
      Transform3D railWood2Trans = new Transform3D();
      railWood2Trans.rotY(2.0f * Math.PI * mul + (Math.PI * 0.5f));
      railWood2Trans.setTranslation(new Vector3d(posMetalRail.getX() - posWoodRailOffset.getX(),
          posMetalRail.getY(), posMetalRail.getZ() - posWoodRailOffset.getZ()));
      railWood2.setTransform(railWood2Trans);
      Box railWood2Shape = new Box(4.0f, 0.3f, 0.25f,
          Primitive.GENERATE_TEXTURE_COORDS | Primitive.GENERATE_NORMALS, railWoodApp);

      // add rail parts to transform group
      railMetal.addChild(railMetalShape);
      railWood1.addChild(railWood1Shape);
      railWood2.addChild(railWood2Shape);

      // add group to tracks
      tracks.addChild(railMetal);
      tracks.addChild(railWood1);
      tracks.addChild(railWood2);
    }

    return tracks;
  }

  private BranchGroup createTrainCollisionTestLight() {
    BranchGroup tester = new BranchGroup();

    // materials & appearances for the tester
    final Material metalMat = new Material(new Color3f(0.3f, 0.3f, 0.3f), new Color3f(),
        new Color3f(0.3f, 0.3f, 0.3f), new Color3f(1.0f, 1.0f, 1.0f), 30.0f);

    Appearance metalApp = new Appearance();
    metalApp.setMaterial(metalMat);
    metalApp.setTexture(metal2Tex);

    Appearance collisionApp = new Appearance();
    RenderingAttributes collisionRenderAttrib = new RenderingAttributes();
    collisionRenderAttrib.setVisible(false);
    collisionApp.setRenderingAttributes(collisionRenderAttrib);

    Appearance poleLightOffApp = new Appearance();
    poleLightOffApp.setMaterial(TESTER_LIGHT_OFF);
    poleLightOffApp.setCapability(Appearance.ALLOW_MATERIAL_WRITE);

    // create the pole of the light
    TransformGroup poleTg = new TransformGroup();
    Transform3D poleTrans = new Transform3D();
    poleTrans.setTranslation(new Vector3d(0.0, -2.0, TRACK_RADIUS - 8.0));
    poleTg.setTransform(poleTrans);
    Cylinder pole = new Cylinder(0.5f, 10.0f,
        Primitive.GENERATE_TEXTURE_COORDS | Primitive.GENERATE_NORMALS, metalApp);
    poleTg.addChild(pole);

    // create the pole top where the light will be
    TransformGroup poleTopTg = new TransformGroup();
    Transform3D poleTopTrans = new Transform3D();
    poleTopTrans.setTranslation(new Vector3d(0.0, 4.5, TRACK_RADIUS - 8.0));
    poleTopTg.setTransform(poleTopTrans);
    Box poleTop = new Box(1.5f, 2.0f, 1.0f,
        Primitive.GENERATE_TEXTURE_COORDS | Primitive.GENERATE_NORMALS, metalApp);
    poleTopTg.addChild(poleTop);

    // create the pole light shape
    TransformGroup poleLightTg = new TransformGroup();
    Transform3D poleLightTrans = new Transform3D();
    poleLightTrans.setTranslation(new Vector3d(0.0, 4.5, TRACK_RADIUS - 8.0));
    poleLightTg.setTransform(poleLightTrans);
    Sphere poleLightShape = new Sphere(1.45f, poleLightOffApp);
    poleLightTg.addChild(poleLightShape);

    // create the pole point light
    PointLight poleLight = new PointLight(false, new Color3f(1.0f, 0.3f, 0.3f),
        new Point3f(0.0f, 4.5f, (float) TRACK_RADIUS - 8.0f), new Point3f(1.5f, 0.0f, 0.0f));
    poleLight.setCapability(Light.ALLOW_STATE_WRITE);
    poleLight.setInfluencingBounds(SCENE_BOUNDS);

    // create collision checking area
    TransformGroup collisionTg = new TransformGroup();
    Transform3D collisionTrans = new Transform3D();
    collisionTrans.setTranslation(new Vector3d(0.0, 1.0, TRACK_RADIUS));
    collisionTg.setTransform(collisionTrans);
    Box collision = new Box(3.0f, 1.0f, 2.0f, collisionApp);
    collisionTg.addChild(collision);

    // create the collision checking behaviour
    TesterLightCollisionBehaviour collisionBehaviour =
        new TesterLightCollisionBehaviour(poleLightShape, poleLight, collision, SCENE_BOUNDS);

    // add tester parts and collision checking behaviour to group
    tester.addChild(poleTg);
    tester.addChild(poleTopTg);
    tester.addChild(poleLightTg);
    tester.addChild(poleLight);
    tester.addChild(collisionTg);
    tester.addChild(collisionBehaviour);
    return tester;
  }

  private BranchGroup createTrainCollisionTestBarrier() {
    BranchGroup tester = new BranchGroup();

    // materials & appearances for the tester
    final Material metalMat = new Material(new Color3f(0.3f, 0.3f, 0.3f), new Color3f(),
        new Color3f(0.3f, 0.3f, 0.3f), new Color3f(1.0f, 1.0f, 1.0f), 30.0f);

    Appearance metalApp = new Appearance();
    metalApp.setMaterial(metalMat);
    metalApp.setTexture(metal2Tex);

    Appearance barrierApp = new Appearance();
    barrierApp.setMaterial(metalMat); // TODO
    barrierApp.setTexture(metal1Tex); // TODO

    Appearance collisionApp = new Appearance();
    RenderingAttributes collisionRenderAttrib = new RenderingAttributes();
    collisionRenderAttrib.setVisible(false);
    collisionApp.setRenderingAttributes(collisionRenderAttrib);

    // create the pole of the barrier
    TransformGroup poleTg = new TransformGroup();
    Transform3D poleTrans = new Transform3D();
    poleTrans.setTranslation(new Vector3d(TRACK_RADIUS - 8.0, -2.0, 0.0));
    poleTg.setTransform(poleTrans);
    Cylinder pole = new Cylinder(0.5f, 8.0f,
        Primitive.GENERATE_TEXTURE_COORDS | Primitive.GENERATE_NORMALS, metalApp);
    poleTg.addChild(pole);

    // create the pole top where the barrier will be
    TransformGroup poleTopTg = new TransformGroup();
    Transform3D poleTopTrans = new Transform3D();
    poleTopTrans.setTranslation(new Vector3d(TRACK_RADIUS - 8.0, 2.5, 0.0));
    poleTopTg.setTransform(poleTopTrans);
    Box poleTop = new Box(0.75f, 0.75f, 0.75f,
        Primitive.GENERATE_TEXTURE_COORDS | Primitive.GENERATE_NORMALS, metalApp);
    poleTopTg.addChild(poleTop);

    // create the pole barrier
    TransformGroup barrierTg = new TransformGroup();
    Transform3D barrierTrans = new Transform3D();
    barrierTrans.setTranslation(new Vector3d(TRACK_RADIUS - 1.5, 2.5, 0.0));
    barrierTg.setTransform(barrierTrans);
    Box barrier = new Box(6.0f, 0.5f, 0.5f,
        Primitive.GENERATE_TEXTURE_COORDS | Primitive.GENERATE_NORMALS, barrierApp);
    barrier.setCollidable(false); // we will want to trigger the barrier using our collision volume
    barrierTg.addChild(barrier);

    // tg for the animation so that the origin of the rotation is correct
    TransformGroup barrierAnimTg = new TransformGroup();
    barrierAnimTg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
    barrierAnimTg.addChild(barrierTg);

    Transform3D barrierRotAxis = new Transform3D();
    barrierRotAxis.rotX(Math.PI * 0.5);
    barrierRotAxis.setTranslation(new Vector3d(TRACK_RADIUS - 7.75, 2.5, 0.0));

    // open & close anim interpolators for the barrier
    // NOTE: these are given dummy Alphas as the TesterBarrierCollisionBehaviour is responsible for
    // playing these
    RotationInterpolator barrierOpenInterp =
        new RotationInterpolator(new Alpha(0, 0), barrierAnimTg);
    barrierOpenInterp.setEnable(false);
    barrierOpenInterp.setTransformAxis(barrierRotAxis);
    barrierOpenInterp.setSchedulingBounds(SCENE_BOUNDS);
    barrierOpenInterp.setMinimumAngle(0.0f);
    barrierOpenInterp.setMaximumAngle((float) Math.PI * 0.5f);

    RotationInterpolator barrierCloseInterp =
        new RotationInterpolator(new Alpha(0, 0), barrierAnimTg);
    barrierCloseInterp.setEnable(false);
    barrierCloseInterp.setTransformAxis(barrierRotAxis);
    barrierCloseInterp.setSchedulingBounds(SCENE_BOUNDS);
    barrierCloseInterp.setMinimumAngle((float) Math.PI * 0.5f);
    barrierCloseInterp.setMaximumAngle(0.0f);

    // create collision checking area
    TransformGroup collisionTg = new TransformGroup();
    Transform3D collisionTrans = new Transform3D();
    collisionTrans.setTranslation(new Vector3d(TRACK_RADIUS, 1.0, 0.0));
    collisionTg.setTransform(collisionTrans);
    Box collision = new Box(3.0f, 1.0f, 2.0f, collisionApp);
    collisionTg.addChild(collision);

    // create the collision checking behaviour
    TesterBarrierCollisionBehaviour collisionBehaviour = new TesterBarrierCollisionBehaviour(
        barrierOpenInterp, barrierCloseInterp, collision, SCENE_BOUNDS);

    // add tester parts and collision checking behaviour to group
    tester.addChild(poleTg);
    tester.addChild(poleTopTg);
    tester.addChild(barrierAnimTg);
    tester.addChild(collisionTg);
    tester.addChild(barrierOpenInterp);
    tester.addChild(barrierCloseInterp);
    tester.addChild(collisionBehaviour);
    return tester;
  }

  private BranchGroup createTerrain() {
    BranchGroup terrain = new BranchGroup();

    TransformGroup terrainBaseTg = new TransformGroup();
    Transform3D terrainBaseTrans = new Transform3D();
    terrainBaseTrans.setTranslation(new Vector3f(0.0f, -5.8f, 0.0f));
    terrainBaseTg.setTransform(terrainBaseTrans);

    // create terrain base to support the track
    final Material grassMat = new Material(new Color3f(0.0f, 0.58f, 0.15f), new Color3f(),
        new Color3f(0.0f, 0.58f, 0.15f), new Color3f(), 1.0f);
    Appearance terrainApp = new Appearance();
    terrainApp.setMaterial(grassMat);
    terrainApp.setTexture(grassTex);
    Cylinder terrainBase = new Cylinder((float) TRACK_RADIUS + 10.0f, 1.0f,
        Primitive.GENERATE_TEXTURE_COORDS | Primitive.GENERATE_NORMALS, terrainApp);
    terrainBaseTg.addChild(terrainBase);

    terrain.addChild(terrainBaseTg);
    return terrain;
  }

  private void addEnvironmentToScene(BranchGroup sceneRoot) {
    // add different objects to scene
    sceneRoot.addChild(createSky());
    sceneRoot.addChild(createTrain());
    sceneRoot.addChild(createTrainTracks());
    sceneRoot.addChild(createTrainCollisionTestLight());
    sceneRoot.addChild(createTrainCollisionTestBarrier());
    sceneRoot.addChild(createTerrain());
  }

  private BranchGroup createScene() {
    // create the scene graph
    BranchGroup sceneRoot = new BranchGroup();

    // get and configure our view's transform a little...
    TransformGroup viewTransform = universe.getViewingPlatform().getViewPlatformTransform();
    Transform3D initialTrans = new Transform3D();
    initialTrans.setTranslation(new Vector3d(0.0, 3.5, TRACK_RADIUS + 50.0));
    initialTrans.setScale(10.0);
    viewTransform.setTransform(initialTrans);

    // add the 3d environment to our scene
    addEnvironmentToScene(sceneRoot);

    // create mouse behaviours so we can interact with the scene a bit
    MouseRotate rotBehaviour = new MouseRotate(viewTransform);
    rotBehaviour.setSchedulingBounds(SCENE_BOUNDS);
    rotBehaviour.setFactor(0.01);
    sceneRoot.addChild(rotBehaviour);

    MouseTranslate transBehaviour = new MouseTranslate(viewTransform);
    transBehaviour.setSchedulingBounds(SCENE_BOUNDS);
    transBehaviour.setFactor(0.1);
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
