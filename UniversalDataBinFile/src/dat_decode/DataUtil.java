package dat_decode;

/**
* Helper methods.
* @author W�llauer
*
*/
public class DataUtil {
	
	public static void printArray(double[] data) {
		int n = data.length<10?data.length:10;
		for(int i=0;i<n;i++) {
			//System.out.print(data[i]+"\t");
			System.out.format("%.2f\t",data[i]);
		}
		System.out.println();
	}

}
