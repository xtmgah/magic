package usecase;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import timeseriesdatabase.DataQuality;
import timeseriesdatabase.aggregated.AggregationInterval;
import timeseriesdatabase.raw.TimestampSeries;
import timeseriesdatabase.server.TSDServerInterface;
import util.CSV;
import util.iterator.TimeSeriesIterator;

public class RunClient {
	
	private static final String CSV_OUTPUT_PATH = "C:/timeseriesdatabase_output/";
	
	public static void main(String[] args) throws RemoteException, NotBoundException {
			System.out.println("start RunClient...");
            Registry registry = LocateRegistry.getRegistry("localhost");
            TSDServerInterface stub = (TSDServerInterface) registry.lookup(StartServer.SERVER_NAME);
            String plotID = "HEG01";
			String[] querySchema = null;
			Long queryStart = null;
			Long queryEnd = null;
			DataQuality dataQuality = DataQuality.EMPIRICAL;
			AggregationInterval aggregationInterval = AggregationInterval.DAY;
			boolean interpolated = false;
			TimestampSeries response = stub.query(plotID, querySchema, queryStart, queryEnd, dataQuality, aggregationInterval, interpolated);
			CSV.write(response, CSV_OUTPUT_PATH+"response.csv");
            System.out.println(response);
            System.out.println("... end RunClient");
	}

}
