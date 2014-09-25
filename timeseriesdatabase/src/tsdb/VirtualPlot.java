package tsdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import tsdb.aggregated.AggregationType;
import tsdb.aggregated.BaseAggregationTimeUtil;
import tsdb.util.TimestampInterval;
import tsdb.util.Util;

/**
 * plot with data from a collection of data streams changing in time
 * @author woellauer
 *
 */
public class VirtualPlot extends TsDBClient {

	public final String plotID;
	public final GeneralStation generalStation;

	public final int geoPosEasting;
	public final int geoPosNorthing;

	public float elevation;
	public float elevationTemperature;

	public final boolean isFocalPlot;

	public final List<TimestampInterval<StationProperties>> intervalList;


	/**
	 * This list is used for interpolation when similar stations are needed.
	 */
	public List<VirtualPlot> nearestVirtualPlots;

	public VirtualPlot(TsDB tsdb, String plotID, GeneralStation generalStation,int geoPosEasting, int geoPosNorthing, boolean isFocalPlot) {
		super(tsdb);
		this.plotID = plotID;
		this.generalStation = generalStation;
		this.geoPosEasting = geoPosEasting;
		this.geoPosNorthing = geoPosNorthing;
		this.elevation = Float.NaN;
		this.elevationTemperature = Float.NaN;
		this.isFocalPlot = isFocalPlot;
		this.intervalList = new ArrayList<TimestampInterval<StationProperties>>();
		this.nearestVirtualPlots = new ArrayList<VirtualPlot>(0);
	}

	/**
	 * Creates schema of this plot that is union of all attributes of stations that are attached to this plot with some time interval.	 * 
	 * @return null if there are no intervals
	 */
	public String[] getSchema() {
		if(intervalList.isEmpty()) {
			return null;
		}
		return intervalList.stream()
				.map(interval->{
					LoggerType loggerType = tsdb.getLoggerType(interval.value.get_logger_type_name());
					if(loggerType==null) {
						throw new RuntimeException("logger type not found: "+interval.value.get_logger_type_name());
					}
					return loggerType;
				})
				.distinct()
				.flatMap(loggerType->Arrays.stream(loggerType.sensorNames))
				.distinct()
				.toArray(String[]::new);
	}

	public String[] getValidSchemaEntries(String[] querySchema) {
		return Util.getValidEntries(querySchema, getSchema());
	}

	/**
	 * Adds one time interval of one station to this plot
	 * @param station
	 * @param properties
	 */
	public void addStationEntry(Station station, StationProperties properties) {
		try {
			intervalList.add(new TimestampInterval<StationProperties>(properties, properties.get_date_start(), properties.get_date_end()));
		} catch(Exception e) {
			log.warn(e+" with "+station.stationID+"   "+properties.getProperty(StationProperties.PROPERTY_START)+"  "+properties.getProperty(StationProperties.PROPERTY_END));
		}
	}

	/**
	 * checks if the given interval overlaps with query interval
	 * @param queryStart may be null if start time is not specified
	 * @param queryEnd may be null if end time is not specified
	 * @param iStart may be null if start time is not specified
	 * @param iEnd may be null if end time is not specified
	 * @return
	 */
	private static boolean overlaps(Long queryStart, Long queryEnd, Long iStart, Long iEnd) {
		if(queryStart==null) {
			queryStart = Long.MIN_VALUE;
		}
		if(queryEnd==null) {
			queryEnd = Long.MAX_VALUE;
		}
		if(iStart==null) {
			iStart = Long.MIN_VALUE;
		}
		if(iEnd==null) {
			iEnd = Long.MAX_VALUE;
		}		
		return queryStart <= iEnd && iStart <= queryEnd;
	}

	/**
	 * Get list of stations with overlapping entries in time interval start - end
	 * @param queryStart
	 * @param queryEnd
	 * @param schema
	 * @return
	 */
	public List<TimestampInterval<StationProperties>> getStationList(Long queryStart, Long queryEnd, String[] schema) {		
		if(schema==null) {
			schema = getSchema();
		}		
		intervalList.sort( (a,b) -> {
			if(a.start==null) {
				if(b.start==null) {
					return 0;
				} else {
					return -1; // start1==null start2!=null
				}
			} else {
				if(b.start==null) {
					return 1; // start1!=null start2==null
				} else {
					return (a.start < b.start) ? -1 : ((a.start == b.start) ? 0 : 1);
				}
			}
		});

		Iterator<TimestampInterval<StationProperties>> it = intervalList.iterator();


		List<TimestampInterval<StationProperties>> resultIntervalList = new ArrayList<TimestampInterval<StationProperties>>();
		while(it.hasNext()) {
			TimestampInterval<StationProperties> interval = it.next();
			if(schemaOverlaps(tsdb.getLoggerType(interval.value.get_logger_type_name()).sensorNames,schema)) {
				if(overlaps(queryStart, queryEnd, interval.start, interval.end)) {
					resultIntervalList.add(interval);
				}
			}
		}
		return resultIntervalList;
	}

	/**
	 * Checks if there are some attributes that are in both schema
	 * @param schema
	 * @param schema2
	 * @return
	 */
	private boolean schemaOverlaps(String[] schema, String[] schema2) {
		for(String name:schema) {
			for(String name2:schema2) {
				if(name.equals(name2)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return plotID;
	}

	public long[] getTimestampInterval() {
		long[] result = null;
		for(TimestampInterval<StationProperties> entry:intervalList) {
			long[] interval = tsdb.getTimeInterval(entry.value.get_serial());
			if(interval!=null) {
				if(result==null) {
					result = interval;
				} else {
					if(interval[0]<result[0]) {
						result[0] = interval[0];
					}
					if(result[1]<interval[1]) {
						result[1] = interval[1];
					}
				}
			}
		}
		return result;
	}

	public long[] getTimestampBaseInterval() {
		long[] interval = getTimestampInterval();
		if(interval==null) {
			return null;
		}
		return new long[]{BaseAggregationTimeUtil.alignQueryTimestampToBaseAggregationTime(interval[0]),BaseAggregationTimeUtil.alignQueryTimestampToBaseAggregationTime(interval[1])};
	}

	public boolean isValidSchema(String[] querySchema) {
		Util.throwNull((Object)querySchema);
		String[] schema = getSchema();
		if(schema==null) {
			return false;
		}
		return !(querySchema==null||querySchema.length==0||!Util.isContained(querySchema, schema));
	}

	public boolean isValidBaseSchema(String[] querySchema) {
		if(!isValidSchema(querySchema)) {
			return false;
		}
		for(String name:querySchema) {
			if(tsdb.getSensor(name).baseAggregationType==AggregationType.NONE) {
				return false;
			}
		}
		return true;
	}

	public void setElevation(float elevation) {
		if(Float.isNaN(elevation)) {
			log.warn("elevation not set: nan");
			return;
		}
		if(!Float.isNaN(this.elevation)) {
			log.warn("elevation already set, overwriting");
		}

		this.elevation = elevation;

		if(!Float.isNaN(this.elevation)) {
			if(elevation<=2321.501) {
				elevationTemperature = elevation*-0.008443f+31.560182f;
			} else {
				elevationTemperature = elevation*-0.004174f+21.648931f;	
			}
		} else {
			elevationTemperature = Float.NaN;
		}
	}
}