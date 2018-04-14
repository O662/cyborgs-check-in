package org.cyborgs3335.checkin.ui;

import java.util.List;

import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;

public class CyclingSpinnerListModel extends SpinnerListModel {

  private static final long serialVersionUID = -3849101049569888305L;

  private SpinnerModel linkedModel = null;

  public CyclingSpinnerListModel(List<?> values) {
    super(values);
  }

  public CyclingSpinnerListModel(Object[] values) {
    super(values);
  }

  public void setLinkedModel(SpinnerModel linkedModel) {
    this.linkedModel = linkedModel;
  }

  public Object getNextValue() {
    Object value = super.getNextValue();
    if (value == null) {
      List<?> list = getList();
      if (list.isEmpty()) {
        return value;
      }
      value = list.get(0);
      if (linkedModel != null) {
        linkedModel.setValue(linkedModel.getNextValue());
      }
    }
    return value;
  }

  public Object getPreviousValue() {
    Object value = super.getPreviousValue();
    if (value == null) {
      List<?> list = getList();
      if (list.isEmpty()) {
        return value;
      }
      value = list.get(list.size() - 1);
      if (linkedModel != null) {
        linkedModel.setValue(linkedModel.getPreviousValue());
      }
    }
    return value;
  }
}
