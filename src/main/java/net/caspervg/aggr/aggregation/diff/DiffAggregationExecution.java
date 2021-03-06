package net.caspervg.aggr.aggregation.diff;

import net.caspervg.aggr.worker.command.AggrCommand;
import net.caspervg.aggr.worker.command.DiffAggrCommand;
import net.caspervg.aggr.aggregation.AbstractAggregationExecution;
import net.caspervg.aggr.core.bean.Dataset;
import net.caspervg.aggr.core.bean.Measurement;
import net.caspervg.aggr.aggregation.AggregationResult;
import net.caspervg.aggr.worker.read.AbstractAggrReader;
import net.caspervg.aggr.core.util.AggrContext;
import net.caspervg.aggr.worker.write.AggrResultWriter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.Callable;

import static net.caspervg.aggr.worker.write.AbstractAggrWriter.OUTPUT_PARAM_KEY;

public class DiffAggregationExecution extends AbstractAggregationExecution {

    private AggrCommand ac;
    private DiffAggrCommand dac;

    public DiffAggregationExecution(AggrCommand ac, DiffAggrCommand dac) {
        this.ac = ac;
        this.dac = dac;
    }
    
    @Override
    public void execute() throws URISyntaxException, IOException {
        Map<String, String> params = ac.getDynamicParameters();
        params.put(AbstractAggrReader.INPUT_PARAM_KEY, ac.getInput());
        params.put(OUTPUT_PARAM_KEY, ac.getOutput());
        String other = dac.getOthers().get(0);

        params.put(AbstractDiffAggregator.OTHER_PARAM_KEY, other);
        params.put(AbstractDiffAggregator.KEY_PARAM_KEY, dac.getKey());

        DiffAggregator aggregator;
        AggrContext ctx = createContext(params, ac);

        Iterable<Measurement> subtrahends = getReader(other, ac, ctx).read(ctx);

        if (ac.isSpark()) {
            aggregator = new SparkDiffAggregator(subtrahends);
        } else {
            aggregator = new PlainDiffAggregator(subtrahends);
        }

        Dataset dataset = Dataset.Builder.setup().withTitle(ac.getDatasetId()).withUuid(ac.getDatasetId()).build();
        Iterable<Measurement> minuends = getReader(ac, ctx).read(ctx);
        Iterable<AggregationResult<DiffAggregation, Measurement>> results = aggregator.aggregate(dataset, minuends, ctx);

        AggrResultWriter writer = null;
        for (AggregationResult<DiffAggregation, Measurement> res : results) {
            writer = getWriter(res, ac, ctx);

            writer.writeDiffAggregation(res, ctx);
        }

        if (writer != null) {
            writer.writeDataset(dataset, ctx);
        }

        stop(ctx);
    }

    public static <T> T uncheckCall(Callable<T> callable) {
        try { return callable.call(); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
