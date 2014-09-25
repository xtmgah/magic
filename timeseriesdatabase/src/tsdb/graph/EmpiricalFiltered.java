package tsdb.graph;

import tsdb.Station;
import tsdb.TsDB;
import tsdb.aggregated.iterator.EmpiricalIterator;
import tsdb.aggregated.iterator.ProjectionIterator;
import tsdb.util.Util;
import tsdb.util.iterator.TsIterator;

/**
 * This node filters values based on the difference to a reference source.
 * @author woellauer
 *
 */
public class EmpiricalFiltered extends Continuous.Abstract {

	private final Continuous source; //not null
	private final Continuous compareSource; //not null	
	private final String stationName; //not null
	

	public EmpiricalFiltered(TsDB tsdb, Continuous source, Continuous compareSource, String stationName) {
		super(tsdb);
		Util.throwNull(source,compareSource,stationName);
		if(!source.isContinuous()) {
			throw new RuntimeException("QualityChecked needs continuous source");
		}
		if(!compareSource.isContinuous()) {
			throw new RuntimeException("QualityChecked needs continuous compare source");
		}
		this.source = source;
		this.compareSource = compareSource;
		this.stationName = stationName;
	}

	public static Continuous create(TsDB tsdb, Continuous continuous, String plotID) {
		
		
		Continuous compareSource = GroupAverageSource.createFromPlot(tsdb, plotID, continuous.getSchema());
		return new EmpiricalFiltered(tsdb,continuous,compareSource, plotID);
	}

	@Override
	public TsIterator get(Long start, Long end) {		
		if(start==null||end==null) {
			long[] interval = tsdb.getBaseTimeInterval(stationName);
			if(interval==null) {
				return null;
			}
			if(start==null) {
				start = interval[0];
			}
			if(end==null) {
				end = interval[1];
			}
		}
		TsIterator input_iterator = source.get(start, end);
		if(input_iterator==null||!input_iterator.hasNext()) {
			return null;
		}
		TsIterator compare_iterator = new ProjectionIterator(compareSource.get(start, end),source.getSchema());
		if(compare_iterator==null||!compare_iterator.hasNext()) {
			log.warn("no compare iterator");
			return input_iterator;
		}		
		Float[] maxDiff = tsdb.getEmpiricalDiff(source.getSchema());
		float[] refValues = tsdb.getReferenceValues(stationName,source.getSchema());
		EmpiricalIterator empirical_iterator = new EmpiricalIterator(input_iterator, compare_iterator, maxDiff, refValues);
		return empirical_iterator;
	}

	

	@Override
	public Station getSourceStation() {
		return source.getSourceStation();
	}

	@Override
	public String[] getSchema() {
		return source.getSchema();
	}

	@Override
	public TsIterator getExactly(long start, long end) {
		return get(start,end);
	}
	
	@Override
	public boolean isContinuous() {
		return source.isContinuous();
	}

	@Override
	public boolean isConstantTimestep() {
		return source.isContinuous();
	}
}