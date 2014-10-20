package tsdb.usecase;

import tsdb.DataQuality;
import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.aggregated.AggregationInterval;
import tsdb.graph.Node;
import tsdb.graph.QueryPlan;
import tsdb.util.iterator.TsIterator;

public class TestingInterpolation {

	public static void main(String[] args) {
		TsDB tsdb = TsDBFactory.createDefault();
		String plotID = "cof1";
		//String plotID = "gra1";
		//String plotID = "sun1";
		//String plotID = "cof3";
		String[] columnNames = new String[]{"Ta_200"};
		AggregationInterval aggregationInterval = AggregationInterval.DAY;
		DataQuality dataQuality = DataQuality.EMPIRICAL;
		boolean interpolated = true;
		Node ts = QueryPlan.plot(tsdb, plotID, columnNames, aggregationInterval, dataQuality, interpolated);
		TsIterator it = ts.get(null, null);
		
		while(it.hasNext()) {
			System.out.println(it.next());
		}
		
		
		
		System.out.println(it.getSchema());

	}

}