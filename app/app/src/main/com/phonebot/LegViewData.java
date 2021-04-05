package main.com.phonebot;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class containing the data for construction of Leg views in
 *
 * @<code>InteractiveLegActivity</code> .
 * <p>
 * TODO(Max): Fix JavaDoc string here
 */
public class LegViewData {

    /**
     * An array of leg items.
     */
    public static final List<LegItem> ITEMS = new ArrayList<LegItem>() {{
        add(createLegItem(0, "Front Left A", 90));
        add(createLegItem(1, "Front Left B", 90));
        add(createLegItem(2, "Front Right A", 90));
        add(createLegItem(3, "Front Right B", 90));
        add(createLegItem(4, "Hind Left A", 90));
        add(createLegItem(5, "Hind Left B", 90));
        add(createLegItem(6, "Hind Right A", 90));
        add(createLegItem(7, "Hind Right B", 90));
    }};

    /**
     * A map of leg items, by id
     */
    public static final Map<String, LegItem> ITEM_MAP = new HashMap<String, LegItem>();


    public static LegItem createLegItem(int position, String legName, int legValue) {
        return new LegItem(String.valueOf(position), legName, legValue);
    }

    public static void addItem(LegItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public static class LegItem {
        public final String id;
        public final String legName;
        public int legValue;

        public LegItem(String id, String legName, int legValue) {
            this.id = id;
            this.legName = legName;
            this.legValue = legValue;
        }
    }

}
