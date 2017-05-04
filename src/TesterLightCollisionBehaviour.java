import java.util.Enumeration;

import javax.media.j3d.Behavior;
import javax.media.j3d.Bounds;
import javax.media.j3d.Light;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnCollisionEntry;
import javax.media.j3d.WakeupOnCollisionExit;
import javax.media.j3d.WakeupOr;

import com.sun.j3d.utils.geometry.Primitive;

public class TesterLightCollisionBehaviour extends Behavior {

  // the light parts of the tester pole
  private Primitive testerLightShape;
  private Light testerLight;
  
  // the area to check for collisions in
  private Primitive testerCollisionArea;
  
  // wakeUp criteria for acting upon a collision 
  private WakeupCriterion[] wakeupCriterion;
  private WakeupOr wakeupOr;

  public TesterLightCollisionBehaviour(Primitive testerLightShape, Light testerLight,
      Primitive testerCollisionArea, Bounds schedulingBounds) {
    this.testerLightShape = testerLightShape;
    this.testerLight = testerLight;
    this.testerCollisionArea = testerCollisionArea;
    setSchedulingBounds(schedulingBounds);
  }

  @Override
  public void initialize() {
    // init collision callbacks
    wakeupCriterion = new WakeupCriterion[2];
    wakeupCriterion[0] = new WakeupOnCollisionEntry(testerCollisionArea);
    wakeupCriterion[1] = new WakeupOnCollisionExit(testerCollisionArea);
    
    wakeupOr = new WakeupOr(wakeupCriterion);
    wakeupOn(wakeupOr);
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void processStimulus(Enumeration criteria) {
    while (criteria.hasMoreElements()) {
      WakeupCriterion criterion = (WakeupCriterion) criteria.nextElement();
      if (criterion instanceof WakeupOnCollisionEntry) {
        // collision entry - power on the light
        testerLightShape.getAppearance().setMaterial(Example3D.TESTER_LIGHT_ON);
        testerLight.setEnable(true);
      } else if (criterion instanceof WakeupOnCollisionExit) {
        // collision exit - power off the light
        testerLightShape.getAppearance().setMaterial(Example3D.TESTER_LIGHT_OFF);
        testerLight.setEnable(false);
      }
    }
    
    // continue listening for collisions
    wakeupOn(wakeupOr);
  }

}
