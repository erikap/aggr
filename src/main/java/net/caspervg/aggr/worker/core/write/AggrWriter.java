package net.caspervg.aggr.worker.core.write;

import net.caspervg.aggr.worker.core.bean.Dataset;
import net.caspervg.aggr.worker.core.bean.Measurement;
import net.caspervg.aggr.worker.core.bean.aggregation.*;
import net.caspervg.aggr.worker.core.util.AggrContext;

/**
 * TODO: Could be simplified by using more OOP: let Aggregation objects write themselves
 */
public interface AggrWriter {
    /**
     * Writes a measurement to the channel
     *
     * @param measurement Measurement to write
     * @param context Context of the operation
     */
    void writeMeasurement(Measurement measurement, AggrContext context);

    /**
     * Writes many measurements to the channel
     *
     * @param measurements Measurements to write
     * @param context Context of the operation
     */
    void writeMeasurements(Iterable<Measurement> measurements, AggrContext context);

    /**
     * Writes a time aggregation to the channel
     *
     * @param aggregation Aggregation to write
     * @param context Context of the operation
     */
    void writeAggregation(TimeAggregation aggregation, AggrContext context);

    /**
     * Writes a KMeans aggregation to the channel
     *
     * @param aggregation Aggregation to write
     * @param context Context of the operation
     */
    void writeAggregation(KMeansAggregation aggregation, AggrContext context);

    /**
     * Writes a grid aggregation to the channel
     *
     * @param aggregation Aggregation to write
     * @param context Context of the operation
     */
    void writeAggregation(GridAggregation aggregation, AggrContext context);

    /**
     * Writes a basic aggregation to the channel
     *
     * @param aggregation Aggregation to write
     * @param context Context of the operation
     */
    void writeAggregation(BasicAggregation aggregation, AggrContext context);

    /**
     * Writes a diff aggregation to the channel
     *
     * @param aggregation Aggregation to write
     * @param context Context of the operation
     */
    void writeAggregation(DiffAggregation aggregation, AggrContext context);

    /**
     * Writes a average aggregation to the channel
     *
     * @param aggregation Aggregation to write
     * @param context Context of the operation
     */
    void writeAggregation(AverageAggregation aggregation, AggrContext context);

    /**
     * Writes a dataset to the channel
     *
     * @param dataset Dataset to write
     * @param context Context of the operation
     */
    void writeDataset(Dataset dataset, AggrContext context);

}
