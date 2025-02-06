package swan2flowdroid;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Class representing a Security-Relevant Method (SRM).
 */
class Srm {
    
    // SRM classes for SWAN
    protected static final String CLASS_SWAN_SOURCE = "source";
    protected static final String CLASS_SWAN_SINK = "sink";
    // SRM classes for FlowDroid
    protected static final String CLASS_FLOWDROID_SOURCE = "_SOURCE_";
    protected static final String CLASS_FLOWDROID_SINK = "_SINK_";
    protected static final String CLASS_FLOWDROID_BOTH = "_BOTH_";

    // Relevant SWAN JSON fields
    @JsonProperty("name")
    protected String   name;
    @JsonProperty("srm")
    protected String[] classes;
    @JsonProperty("parameters")
    protected String[] parameters;
    @JsonProperty("return")
    protected String   returnType;


    /**
     * Check if this SRM is a source.
     * 
     * @return True if this SRM is a source, false otherwise.
     */
    protected boolean isSource() {
        if (this.classes == null) {
            return false;
        }

        return Arrays.asList(this.classes).contains(Srm.CLASS_SWAN_SOURCE);
    }

    /**
     * Check if this SRM is a sink.
     * 
     * @return True if this SRM is a sink, false otherwise.
     */
    protected boolean isSink() {
        if (this.classes == null) {
            return false;
        }
        
        return Arrays.asList(this.classes).contains(Srm.CLASS_SWAN_SINK);
    }

    /**
     * Convert this SRM to a human-readable string.
     * 
     * @return The SRM as a human-readable string.
     */
    @Override
    public String toString() {
        return String.format("%s %s %s %s",
            this.name,
            Arrays.toString(this.classes),
            Arrays.toString(this.parameters),
            this.returnType
        );
    }

    /**
     * Convert this SRM to a string formatted to be used with FlowDroid.
     * 
     * @return The SRM formatted to be used with FlowDroid.
     */
    protected String toFlowdroid() {
        String strFlowdroid;

        // Package and method name
        String packageName;
        String methodName;
        if (this.name == null) {
            packageName = "unknown";
            methodName = "unknown"; 
        } else {
            int indexLastDot = this.name.lastIndexOf(".");
            if (indexLastDot == -1) {
                packageName = "unknown";
                methodName = this.name;
            } else {
                packageName = this.name.substring(0, indexLastDot);
                methodName = this.name.substring(indexLastDot + 1);
                
                String[] split = this.name.split("\\.");
                String className = split[split.length - 2];

                // If method name is identical to class name, it is the constructor:
                // change name to "<init>"
                if (methodName.equals(className)) {
                    methodName = "<init>";
                }
            }
        }

        // Return class
        String returnTypeLocal = this.returnType == null ? "void" : this.returnType;
        strFlowdroid = String.format("<%s: %s %s(", packageName, returnTypeLocal, methodName);

        // Add parameters
        for (int i = 0; i < this.parameters.length; i++) {
            String param = this.parameters[i];
            if (i > 0) {
                strFlowdroid += ",";
            }
            strFlowdroid += param;
        }

        // SRM class
        List classesList = Arrays.asList(this.classes);
        if (classesList.contains(Srm.CLASS_SWAN_SOURCE) &&
            classesList.contains(Srm.CLASS_SWAN_SINK)) {
            strFlowdroid += String.format(")> -> %s", Srm.CLASS_FLOWDROID_BOTH);
        } else if (classesList.contains(Srm.CLASS_SWAN_SOURCE)) {
            strFlowdroid += String.format(")> -> %s", Srm.CLASS_FLOWDROID_SOURCE);
        } else if (classesList.contains(Srm.CLASS_SWAN_SINK)) {
            strFlowdroid += String.format(")> -> %s", Srm.CLASS_FLOWDROID_SINK);
        }

        return strFlowdroid;
    }

}
