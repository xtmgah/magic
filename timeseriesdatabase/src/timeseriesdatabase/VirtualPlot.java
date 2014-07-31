package timeseriesdatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;

import com.sun.xml.internal.messaging.saaj.packaging.mime.util.QEncoderStream;

import util.TimestampInterval;
import util.Util;

/**
 * plot with data from a collection of data streams changing in time
 * @author woellauer
 *
 */
public class VirtualPlot {
	
	private static final Logger log = Util.log;

	public final String plotID;
	public final String generalStationName;

	public final List<TimestampInterval<Station>> intervalList;

	public VirtualPlot(String plotID, String generalStationName) {
		this.plotID = plotID;
		this.generalStationName = generalStationName;
		this.intervalList = new ArrayList<TimestampInterval<Station>>();
	}
	
	public String[] getSchema() {
		/*Set<LoggerType> loggerTypes = new LinkedHashSet<LoggerType>();
		//Stream<TimestampInterval<Station>> stream = intervalList.stream();
		//intervalList.stream().forEach(interval->loggerTypes.add(interval.value.loggerType));*/
		
		//LinkedHashSet<String> schemaSet = new LinkedHashSet<String>();
		
		//loggerTypes.stream().flatMap(loggerType->Arrays.stream(loggerType.sensorNames)).forEach(name->schemaSet.add(name));
		
		
		/*loggerTypes.stream().forEach(loggerType->{
			for(String name:loggerType.sensorNames) {
				schemaSet.add(name);
			}			
		});*/
		
		
		/*intervalList.stream()
					.map(interval->interval.value.loggerType)
					.distinct()
					.flatMap(loggerType->Arrays.stream(loggerType.sensorNames))
					.forEach(name->schemaSet .add(name));
		
		return schemaSet.toArray(new String[0]);*/
		
		//intervalList.iterator().forEachRemaining(action);
		
		return intervalList.stream()
							.map(interval->interval.value.loggerType)
							.distinct()
							.flatMap(loggerType->Arrays.stream(loggerType.sensorNames))
							.distinct()
							.toArray(String[]::new);
	}

	public void addStationEntry(Station station, Long start, Long end) {
		intervalList.add(new TimestampInterval<Station>(station, start, end));
	}
	
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
	public List<TimestampInterval<Station>> getStationList(Long queryStart, Long queryEnd, String[] schema) {
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
		
		Iterator<TimestampInterval<Station>> it = intervalList.iterator();
		
		
		List<TimestampInterval<Station>> resultIntervalList = new ArrayList<TimestampInterval<Station>>();
		while(it.hasNext()) {
			TimestampInterval<Station> interval = it.next();
			if(overlaps(queryStart, queryEnd, interval.start, interval.end)) {
				resultIntervalList.add(interval);
			}
		}
		
		return resultIntervalList;
	}
}
