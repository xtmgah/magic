package tsdb.graph;

import tsdb.Station;
import tsdb.TsDB;
import tsdb.VirtualPlot;
import tsdb.util.Util;

/**
 * This node provides base aggregated data.
 * @author woellauer
 *
 */
public interface Base extends Node {
	
	@Override
	public default boolean isConstantTimestep() {
		return true;
	}
	
	public static Base create(TsDB tsdb, String plotID, String[] querySchema, NodeGen stationGen) {
		VirtualPlot virtualPlot = tsdb.getVirtualPlot(plotID);
		if(virtualPlot!=null) {
			return VirtualBase.create(tsdb, virtualPlot, querySchema, stationGen);
		}
		Station station = tsdb.getStation(plotID);
		if(station!=null) {
			return StationBase.create(tsdb, station, querySchema, stationGen);
		}else {
			throw new RuntimeException("plotID / stationID not found: "+plotID);
		}	
	}
	
	public abstract class Abstract implements Base {		
		protected TsDB tsdb; // not null		
		Abstract(TsDB tsdb) {
			Util.throwNull(tsdb);
			this.tsdb = tsdb;
		}		
	}
}