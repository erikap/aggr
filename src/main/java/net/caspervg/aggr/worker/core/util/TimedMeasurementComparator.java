package net.caspervg.aggr.worker.core.util;

import net.caspervg.aggr.worker.core.bean.Measurement;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Comparator;

import static net.caspervg.aggr.worker.core.util.Constants.DEFAULT_TIMESTAMP_KEY;

public class TimedMeasurementComparator implements Comparator<Measurement>, Serializable {
    @Override
    public int compare(Measurement o1, Measurement o2) {

        if(o1 == null && o2 == null) {
            return 0;
        } else if(o1 == null || o1.getTimestamp() == null) {
            return 1;
        } else if(o2 == null || o2.getTimestamp() == null) {
            return -1;
        } else {
            LocalDateTime o1t = o1.getTimestamp();
            LocalDateTime o2t = o2.getTimestamp();

            return o1t.compareTo(o2t);
        }
    }
}
