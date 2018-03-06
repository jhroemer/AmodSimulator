package AmodSimulator;

import java.util.*;

/**
 * Own properties-class to make result-files sorted
 */
public class ExpProperties extends Properties {

    public Enumeration<Object> keys() {
        Enumeration<Object> keysEnum = super.keys();
        Vector<Object> keyList = new Vector<Object>();

        while (keysEnum.hasMoreElements()) {
            keyList.add(keysEnum.nextElement());
        }

        keyList.sort(new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });

        return keyList.elements();
    }
}
