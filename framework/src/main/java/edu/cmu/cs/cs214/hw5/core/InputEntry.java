package edu.cmu.cs.cs214.hw5.core;

import java.util.List;

/**
 * Abstract class of the input entry
 */
 public abstract class InputEntry {
     private String label;
     private String helpMessage;
     private String value;

    /**
     * Constructor
     * @param label label for this entry
     * @param helpMessage help message for this entry
     */
     public InputEntry(String label, String helpMessage) {
         this.label = label;
         this.helpMessage = helpMessage;
         /* avoid unexpected error, set a default */
     }

    /**
     * Get the description label of this entry
     * @return label of this entry
     */
    public String getLabel() {
        return label;
    }

    /**
     * A string to tell user how to input data in this entry
     * @return the help message
     */
    public String getHelpMessage() {
        return helpMessage;
    }


    /**
     * Set input value for this entry
     *
     * @param userInput user input (as string) for this entry
     */
    public void setValue(String userInput) {
      this.value = userInput;
    }

    /**
     * Get value for this entry
     *
     * @return value of this entry, if it is select_many_entry, the selected options would be delimited by ";"
     */
    public String getValue() {
      return value;
    }

    /**
     * @return entry type of this entry
     */
    public abstract InputEntryType getEntryType();


    /**
     * Called by gui, get scope of options for this entry (only valid in select one/may entry)
     *
     * @return scope of this entry
     */
    public abstract List<String> getScope();

    /**
     * Called by plugin, set scope of selection for this entry (only valid in select one/may entry)
     *
     * @param options an array of options
     */
    public abstract void setScope(List<String> options);

    /**
     * Print content
     * @return print content of entry
     */
    @Override
    public String toString() {
        return "InputEntry{" +
            "label='" + label + '\'' +
            ", helpMessage='" + helpMessage + '\'' +
            ", value='" + value + '\'' +
            '}';
    }
}
