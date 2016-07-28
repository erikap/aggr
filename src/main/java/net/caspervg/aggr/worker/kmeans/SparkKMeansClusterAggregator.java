package net.caspervg.aggr.worker.kmeans;

import com.google.common.collect.Lists;
import net.caspervg.aggr.worker.core.bean.*;
import net.caspervg.aggr.worker.core.bean.aggregation.AggregationResult;
import net.caspervg.aggr.worker.core.bean.aggregation.KMeansAggregation;
import net.caspervg.aggr.worker.core.util.AggrContext;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.clustering.KMeans;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;

import java.io.Serializable;
import java.util.*;

public class SparkKMeansClusterAggregator extends AbstractKMeansAggregator implements Serializable {
    @Override
    public Iterable<AggregationResult<KMeansAggregation, Measurement>> aggregate(Dataset dataset,
                                                                              Iterable<Measurement> measurements,
                                                                              AggrContext context) {
        Objects.requireNonNull(context.getSparkContext());

        int maxIterations = Integer.parseInt(
                context.getParameters().getOrDefault(ITERATIONS_PARAM, DEFAULT_MAX_ITERATIONS)
        );
        int numClusters = Integer.parseInt(
                context.getParameters().getOrDefault(CENTROIDS_PARAM, DEFAULT_NUM_CENTROIDS)
        );

        List<Measurement> measurementList = Lists.newArrayList(measurements);
        JavaRDD<Measurement> measRDD = context.getSparkContext().parallelize(measurementList);

        JavaRDD<Vector> vecRDD = measRDD.map(new Function<Measurement, Vector>() {
            @Override
            public Vector call(Measurement meas) throws Exception {
                return Vectors.dense(ArrayUtils.toPrimitive(meas.getVector()));
            }
        });
        vecRDD.cache();

        KMeansModel clusters = KMeans.train(vecRDD.rdd(), numClusters, maxIterations);

        Vector[] centers = clusters.clusterCenters();
        List<Integer> predictedIndices = clusters.predict(vecRDD).collect();

        List<Set<UniquelyIdentifiable>> centroidParentsList = new ArrayList<>();
        for (Vector ignored : centers) {
            centroidParentsList.add(new HashSet<>());
        }

        for (int i = 0; i < predictedIndices.size(); i++) {
            int predictedIndex = predictedIndices.get(i);
            Set<UniquelyIdentifiable> parents = centroidParentsList.get(predictedIndex);
            parents.add(measurementList.get(i));
        }

        List<Measurement> centroidList = new ArrayList<>();
        for (int i = 0; i < centroidParentsList.size(); i++) {
            Double[] centerVec = ArrayUtils.toObject(centers[i].toArray());
            Measurement centroid = context.newMeasurement();
            centroid.setVector(centerVec);
            centroid.setParents(centroidParentsList.get(i));

            centroidList.add(
                    centroid
            );
        }

        // Return the result of the aggregation
        KMeansAggregation aggr = new KMeansAggregation(
                dataset,
                numClusters,
                maxIterations,
                measurementList
        );
        aggr.setComponents(centroidList);

        return Lists.newArrayList(
                new AggregationResult<>(
                        aggr,
                        centroidList
                )
        );
    }
}
