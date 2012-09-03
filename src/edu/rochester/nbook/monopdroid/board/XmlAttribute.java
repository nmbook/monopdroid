package edu.rochester.nbook.monopdroid.board;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.graphics.Color;

/**
 * Allows me to read off and store XML data. Each of these objects represents an attribute encountered in the XML, and associates it with a setter in the specified class. Given the attribute string, the correct setter on the object can be called with the correct argument type.
 * @author Nate
 */
public class XmlAttribute {
    private Method m;
    private XmlAttributeType type;
    private String methodName;
    
    /**
     * Creates a new attribute.
     * @param cls The class (Player, Estate, or EstateGroup) that has a setter with the methodName.
     * @param methodName The name of the method to call when set() is called.
     * @param type The conversion to do before calling the setter, for data types other than String in the object.
     */
    public XmlAttribute(Class<?> cls, String methodName, XmlAttributeType type) {
        this.type = type;
        this.methodName = methodName;
        if (methodName == null) {
            return;
        }
        try {
            switch (type) {
            default:
            case STRING:
                m = cls.getMethod(methodName, String.class);
                break;
            case INT:
            case COLOR:
                m = cls.getMethod(methodName, int.class);
                break;
            case BOOLEAN:
                m = cls.getMethod(methodName, boolean.class);
                break;
            case RENT:
                m = cls.getMethod(methodName.substring(0, methodName.length() - 1), int.class, int.class);
                break;
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Calls the setter on the specified object instance.
     * @param o The object instance of the class type this attribute was constructed with.
     * @param value The value of the attribute, as stored in XML.
     */
    public void set(Object o, String value) {
        if (m == null) {
            return;
        }
        
        try {
            if (type == XmlAttributeType.RENT) {
                m.invoke(o, methodName.charAt(methodName.length() - 1) - 0x30, parseValue(value, type));
            } else {
                m.invoke(o, parseValue(value, type));
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static Object parseValue(String value, XmlAttributeType type) {
        try {
            switch (type) {
            default:
            case STRING:
                return value;
            case INT:
                return Integer.parseInt(value);
            case BOOLEAN:
                return !value.equals("0");
            case COLOR:
                if (value.length() == 0) {
                    return 0;
                } else {
                    return Color.parseColor(value);
                }
            case RENT:
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return null;
    }
}
