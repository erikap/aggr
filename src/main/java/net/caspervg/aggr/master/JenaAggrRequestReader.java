package net.caspervg.aggr.master;

import net.caspervg.aggr.master.bean.AggregationRequest;
import net.caspervg.aggr.master.bean.AggregationRequestEnvironment;
import net.caspervg.aggr.master.bean.AggregationRequestParameters;
import org.apache.jena.jdbc.mem.MemDriver;
import org.apache.jena.jdbc.remote.RemoteEndpointDriver;

import java.sql.*;
import java.util.*;

/**
 * Scans the triple store for new (not yet started) aggregation requested.
 */
@SuppressWarnings("SqlNoDataSourceInspection")
public class JenaAggrRequestReader {

    private static final String REQUEST_QUERY = "PREFIX mu: <http://mu.semte.ch/vocabularies/core/>\n"+
            "PREFIX own: <http://www.caspervg.net/test/property#>\n"+
            "\n"+
            "select *\n"+
            "where {\n"+
            "    ?req mu:uuid ?id ;\n"+
            "         own:dataset ?dataset ;\n"+
            "         own:input ?input ;\n"+
            "         own:output ?output ;\n"+
            "         own:aggregation_type ?aggregation_type ;\n"+
            "         own:provenance ?provenance ;\n"+
            "         own:input_class ?input_class ;\n"+
            "         own:output_class ?output_class ;\n"+
            "         own:status ?status ;\n"+
            "         own:big_data ?big_data .\n"+
            "   \n"+
            "    OPTIONAL { ?req own:hdfs ?hdfs }\n"+
            "    OPTIONAL { ?req own:spark ?spark }\n"+
            "\n"+
            "    OPTIONAL { ?req own:iterations ?iterations }\n"+
            "    OPTIONAL { ?req own:centroids ?centroids }\n"+
            "    OPTIONAL { ?req own:metric ?metric }\n"+
            "    OPTIONAL { ?req own:levels ?levels }\n"+
            "    OPTIONAL { ?req own:grid_size ?grid_size }\n"+
            "    OPTIONAL { ?req own:others ?others }\n" +
            "    OPTIONAL { ?req own:amount ?amount }\n" +
            "    OPTIONAL { ?req own:key ?key }\n"+
            "\n"+
            "    OPTIONAL { ?req own:query ?query }\n"+
            "    OPTIONAL { ?req own:latitude_key ?latitude_key }\n"+
            "    OPTIONAL { ?req own:longitude_key ?longitude_key }\n"+
            "    OPTIONAL { ?req own:id_key ?id_key }\n"+
            "    OPTIONAL { ?req own:time_key ?time_key }\n"+
            "    OPTIONAL { ?req own:source_key ?source_key }\n"+
            "\n"+
            "    FILTER(str(?status) = \"not_started\")\n"+
            "}";

    private String service;

    public JenaAggrRequestReader(String service) {
        this.service = service;
    }

    public Iterable<AggregationRequest> read() throws SQLException {
        Set<AggregationRequest> requests = new HashSet<>();

        try (Connection conn = getConnection()) {

            Statement stmt = conn.createStatement();
            try (ResultSet rs = stmt.executeQuery(REQUEST_QUERY)) {
                while (rs.next()) {
                    AggregationRequestParameters params = AggregationRequestParameters.Builder
                            .setup()
                            .centroids(rs.getInt("centroids"))
                            .iterations(rs.getInt("iterations"))
                            .metric(rs.getString("metric"))
                            .levels(rs.getInt("levels"))
                            .gridSize(parseDouble(rs.getString("grid_size")))
                            .othersString(rs.getString("others"))
                            .amount(rs.getLong("amount"))
                            .key(rs.getString("key"))
                            .dynamic(getDynamicParameters(rs))
                            .build();

                    AggregationRequestEnvironment env = new AggregationRequestEnvironment(
                            rs.getString("hdfs"),
                            rs.getString("spark")
                    );

                    requests.add(
                            AggregationRequest.Builder
                                    .setup()
                                    .id(rs.getString("id"))
                                    .dataset(rs.getString("dataset"))
                                    .input(rs.getString("input"))
                                    .output(rs.getString("output"))
                                    .aggregationType(rs.getString("aggregation_type"))
                                    .writeProvenance(parseBoolean(rs.getString("provenance")))
                                    .bigData(parseBoolean(rs.getString("big_data")))
                                    .inputClassName(rs.getString("input_class"))
                                    .outputClassName(rs.getString("output_class"))
                                    .parameters(params)
                                    .environment(env)
                                    .build()
                    );
                }
            }
        }

        return requests;
    }

    private Map<String, String> getDynamicParameters(ResultSet rs) throws SQLException {
        String[] keys = new String[]{"query", "latitude_key", "longitude_key", "time_key", "id_key", "source_key"};
        Map<String, String> dynParams = new HashMap<>();

        Arrays.stream(keys).forEach(key -> {
            try {
                putIfExists(rs, key, dynParams);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        return dynParams;
    }

    private void putIfExists(ResultSet rs, String key, Map<String, String> params) throws SQLException {
        if (rs.getString(key) != null) {
            params.put(key, rs.getString(key));
        }

    }

    private boolean parseBoolean(String rsBoolean) {
        return "1".equalsIgnoreCase(rsBoolean);
    }

    private double parseDouble(String rsDouble) {
        if (rsDouble == null) return 1.0;
        else return Double.parseDouble(rsDouble);
    }

    private Connection getConnection() throws SQLException {
        if (this.service.startsWith("mem:")) {
            MemDriver.register();

            return DriverManager.getConnection("jdbc:jena:" + this.service);
        } else {
            RemoteEndpointDriver.register();
            return  DriverManager.getConnection("jdbc:jena:remote:query=" + this.service);
        }
    }
}
