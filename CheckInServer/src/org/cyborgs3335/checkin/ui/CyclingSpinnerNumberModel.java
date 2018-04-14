package org.cyborgs3335.checkin.ui;

import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

public class CyclingSpinnerNumberModel extends SpinnerNumberModel {

  private static final long serialVersionUID = -3646879424845232795L;

  private SpinnerModel linkedModel = null;

  public CyclingSpinnerNumberModel(Number value, Comparable<?> minimum,
      Comparable<?> maximum, Number stepSize) {
    super(value, minimum, maximum, stepSize);
  }

  public CyclingSpinnerNumberModel(int value, int minimum, int maximum,
      int stepSize) {
    super(value, minimum, maximum, stepSize);
  }

  public CyclingSpinnerNumberModel(double value, double minimum, double maximum,
      double stepSize) {
    super(value, minimum, maximum, stepSize);
  }

  public void setLinkedModel(SpinnerModel linkedModel) {
    this.linkedModel = linkedModel;
  }

  public Object getNextValue() {
    Object value = super.getNextValue();
    if (value == null) {
      if (linkedModel != null) {
        linkedModel.setValue(linkedModel.getNextValue());
      }
      value = getMinimum();
    }
    return value;
  }

  public Object getPreviousValue() {
    Object value = super.getPreviousValue();
    if (value == null) {
      if (linkedModel != null) {
        linkedModel.setValue(linkedModel.getPreviousValue());
      }
      value = getMaximum();
    }
    return value;
  }
}
