package edu.rochester.nbook.monopdroid.board;

/**
 * The possible types of Attributes that can be specified.
 * @author Nate
 */
public enum XmlAttributeType {
    /**
     * The default type. The XML string value is passed to the setter as a String.
     */
    STRING,
    /**
     * The integer type. Converts the XML value to an integer. Null throws an exception.
     */
    INT,
    /**
     * The boolean type. "0" becomes false. Any other non-null strings become true. Null throws an exception.
     */
    BOOLEAN,
    /**
     * This converts the XML string value to a Color integer using Color.parseColor().
     */
    COLOR,
    /**
     * This does the integer conversion, but has special properties.
     * This is specific to the Estate.rent[] values.
     * The methodName specified with this type MUST end with a digit, specifying 
     * the rent index (0-5), the number of houses, for which this rent value
     * is being stored for.
     */
    RENT
}
