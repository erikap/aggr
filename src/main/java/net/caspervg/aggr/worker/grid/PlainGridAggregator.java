package net.caspervg.aggr.worker.grid;

import com.google.common.collect.Lists;
import net.caspervg.aggr.worker.core.bean.*;
import net.caspervg.aggr.worker.core.bean.aggregation.AggregationResult;
import net.caspervg.aggr.worker.core.bean.aggregation.GridAggregation;
import net.caspervg.aggr.worker.core.util.AggrContext;

import java.util.HashSet;
import java.util.Set;

public class PlainGridAggregator extends AbstractGridAggregator {


    @Override
    public Iterable<AggregationResult<GridAggregation, Measurement>> aggregate(Dataset dataset,
                                                                               Iterable<Measurement> measurements,
                                                                               AggrContext context) {
        double gridSize = Double.parseDouble(
                context.getParameters().getOrDefault(GRID_SIZE_PARAM, DEFAULT_GRID_SIZE)
        );

        Set<Measurement> roundedMeasurements = new HashSet<>();

        for (Measurement parent : measurements) {
            double latitude = parent.getPoint().getVector()[0];
            double longitude = parent.getPoint().getVector()[1];

            double roundedLatitude = (double) Math.round(latitude / gridSize) * gridSize;
            double roundedLongitude = (double) Math.round(longitude / gridSize) * gridSize;

            Point roundedPoint = new Point(new Double[]{roundedLatitude, roundedLongitude});
            if (parent instanceof TimedMeasurement) {
                roundedMeasurements.add(
                        TimedMeasurement.Builder
                            .setup()
                            .withPoint(roundedPoint)
                            .withParent(parent)
                            .withTimestamp(((TimedMeasurement) parent).getTimestamp())
                            .build()
                );
            } else {
                roundedMeasurements.add(
                        Measurement.Builder
                            .setup()
                            .withPoint(roundedPoint)
                            .withParent(parent)
                            .build()
                );
            }
        }

        return Lists.newArrayList(
                new AggregationResult<>(
                        new GridAggregation(dataset,
                                gridSize,
                                roundedMeasurements
                        ),
                        roundedMeasurements
                )
        );
    }
}