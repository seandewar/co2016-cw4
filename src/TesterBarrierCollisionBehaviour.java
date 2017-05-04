import java.util.Enumeration;

import javax.media.j3d.Alpha;
import javax.media.j3d.Behavior;
import javax.media.j3d.Bounds;
import javax.media.j3d.Interpolator;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnCollisionEntry;
import javax.media.j3d.WakeupOnCollisionExit;
import javax.media.j3d.WakeupOr;

import com.sun.j3d.utils.geometry.Primitive;

public class TesterBarrierCollisionBehaviour extends Behavior {

  // the interpolators for the open/close anim
  private Interpolator testerOpenInterp;
  private Interpolator testerCloseInterp;

  // the area to check for collisions in
  private Primitive testerCollisionArea;

  // wakeUp criteria for acting upon a collision
  private WakeupCriterion[] wakeupCriterion;
  private WakeupOr wakeupOr;

  public TesterBarrierCollisionBehaviour(Interpolator testerOpenInterp,
      Interpolator testerCloseInterp, Primitive testerCollisionArea, Bounds schedulingBounds) {
    this.testerOpenInterp = testerOpenInterp;
    this.testerCloseInterp = testerCloseInterp;
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
        // collision entry - open barrier
        testerCloseInterp.setEnable(false);

        // reset interpolator so we can replay the animation
        Alpha animAlpha = new Alpha(1, 250);
        animAlpha.setTriggerTime(System.currentTimeMillis() - animAlpha.getStartTime());
        testerOpenInterp.setAlpha(animAlpha);
        testerOpenInterp.setEnable(true);
      } else if (criterion instanceof WakeupOnCollisionExit) {
        // collision exit - close barrier
        testerOpenInterp.setEnable(false);

        // reset interpolator so we can replay the animation
        Alpha animAlpha = new Alpha(1, 250);
        animAlpha.setTriggerTime(System.currentTimeMillis() - animAlpha.getStartTime());
        testerCloseInterp.setAlpha(animAlpha);
        testerCloseInterp.setEnable(true);
      }
    }

    // continue listening for collisions
    wakeupOn(wakeupOr);
  }

}
