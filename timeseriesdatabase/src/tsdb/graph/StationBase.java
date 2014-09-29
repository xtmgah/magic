package tsdb.graph;

import static tsdb.util.AssumptionCheck.throwNull;
import tsdb.Station;
import tsdb.TsDB;
import tsdb.aggregated.iterator.BaseAggregationIterator;
import tsdb.util.iterator.TsIterator;

/**
 * This node creates base aggregated values from one station source.
 * @author woellauer
 *
 */
public class StationBase extends Base.Abstract {

	private final Node source;	

	protected StationBase(TsDB tsdb, Node source) {
		super(tsdb);
		throwNull(source);
		this.source = source;
	}

	public static StationBase create(TsDB tsdb,Station station, String[] querySchema, NodeGen stationGen) {
		if(querySchema==null) {
			querySchema = tsdb.getBaseSchema(station.loggerType.sensorNames);
		}
		Node source = stationGen.get(station.stationID, querySchema);
		return new StationBase(tsdb, source);
	}

	@Override
	public TsIterator get(Long start, Long end) {
		TsIterator input_iterator = source.get(start, end);
		if(input_iterator==null||!input_iterator.hasNext()) {
			return null;
		}
		BaseAggregationIterator base_iterator = new BaseAggregationIterator(tsdb, input_iterator);
		if(!base_iterator.hasNext()) {
			return null;
		}	

		return base_iterator;				
	}

	@Override
	public Station getSourceStation() {
		return source.getSourceStation();
	}

	@Override
	public boolean isContinuous() {
		return false; // maybe todo
	}

	@Override
	public String[] getSchema() {
		return tsdb.getBaseSchema(source.getSchema());
	}
}
