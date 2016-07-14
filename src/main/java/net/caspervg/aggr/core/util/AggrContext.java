package net.caspervg.aggr.core.util;

import org.apache.hadoop.fs.FileSystem;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SQLContext;

import java.util.Map;

public class AggrContext {

    public static final AggrContext EMPTY = new AggrContext(null, null);

    private Map<String, String> parameters;
    private JavaSparkContext sparkContext;
    private SQLContext sqlContext;
    private FileSystem fileSystem;

    public AggrContext(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public AggrContext(Map<String, String> parameters, JavaSparkContext sparkContext) {
        this.parameters = parameters;
        this.sparkContext = sparkContext;
    }

    public AggrContext(Map<String, String> parameters, JavaSparkContext sparkContext, SQLContext sqlContext) {
        this.parameters = parameters;
        this.sparkContext = sparkContext;
        this.sqlContext = sqlContext;
    }

    public AggrContext(Map<String, String> parameters, JavaSparkContext sparkContext, SQLContext sqlContext, FileSystem fileSystem) {
        this.parameters = parameters;
        this.sparkContext = sparkContext;
        this.sqlContext = sqlContext;
        this.fileSystem = fileSystem;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public JavaSparkContext getSparkContext() {
        return sparkContext;
    }

    public SQLContext getSqlContext() {
        return sqlContext;
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }
}
