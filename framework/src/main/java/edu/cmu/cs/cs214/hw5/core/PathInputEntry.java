package edu.cmu.cs.cs214.hw5.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Select one input entry
 */
public class PathInputEntry extends InputEntry {
  private List<String> options;
  private String value;
  /**
   * Constructor
   *
   * @param label       label for this entry
   * @param helpMessage help message for this entry
   */
  public PathInputEntry(String label, String helpMessage) {
    super(label, helpMessage);
  }

  /**
   * @return entry type of this entry
   */
  @Override
  public InputEntryType getEntryType() {
    return InputEntryType.PATH_INPUT_ENTRY;
  }

  /**
   * Called by gui, get scope of options for this entry (only valid in select one/may entry)
   *
   * @return scope of this entry
   */
  @Override
  public List<String> getScope() {
    throw new IllegalStateException("Not Implement");
  }

  /**
   * Called by plugin, set scope of selection for this entry (only valid in select one/may entry)
   *
   * @param options an array of options
   */
  @Override
  public void setScope(List<String> options) {
    throw new IllegalStateException("Not Implement");
  }
}
