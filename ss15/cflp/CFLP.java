package ss15.cflp;

/**
 * Klasse zum Berechnen der L&ouml;sung mittels Branch-and-Bound.
 * Hier sollen Sie Ihre L&ouml;sung implementieren.
 */

/**
 * Global variables
 */

/* List of Costumers (index) that contains the corresponding bandwith need. */

import java.lang.reflect.Array;
import java.util.*;


public class CFLP extends AbstractCFLP {

	public CFLP(CFLPInstance instance) {
		// TODO: Hier ist der richtige Platz fuer Initialisierungen

        /* List of Customers, sorted by Bandwithneed */
        int[] customers;

        /* List of Facilities that are available for every customer. */
        int[][] facilities;

        /* List of the real distance costs (distance[][] * distanceCosts) */
        int[][] distanceCosts;

        /* Get all the values from instance. */
        int customer_cnt = instance.getNumCustomers();
        int facility_cnt = instance.getNumFacilities();
        int distanceCost_cnt = instance.distances.length;
        /* Step one: Calculate the best faciliy (e.g lowest distance ?!) for every customer */


        facilities = new int[customer_cnt][facility_cnt];
        customers = new int[customer_cnt];
        distanceCosts = new int[distanceCost_cnt][instance.distances[0].length];

        for (int i = 0; i < customer_cnt; i++) {
            int[] facility_index = new int[facility_cnt];

            for(int j = 0; j < facility_cnt; j++) {
               facility_index[j] = j;
            }

            /* Now sort facility_index in correspondence to the distance of the facilities to the current customer. */
            int best_fac_tmp = 0;
            int loop_cnt = 0;

            System.out.println("Customer "+i+": " + Arrays.toString(facility_index));

            /* Bubble Sort */
            for(int j = 0; j < facility_cnt; j++) {
                for(int k = 1; k < facility_cnt - j; k++) {
                    if(instance.distance(facility_index[k-1], i) < instance.distance(facility_index[k],i)) {
                        int swp = facility_index[k-1];
                        facility_index[k-1] = facility_index[k];
                        facility_index[k] = swp;
                    }
                }
            }

            /* Copy the ordered facility_index into the [][] facilities and assign the customer */
            for(int j = 0; j < facility_cnt; j++) {
                facilities[i][j] = facility_index[j];
            }

            System.out.println("Customer "+i+": " + Arrays.toString(facility_index));
        }

        /* Step two: Get the customers with the biggest need of bandwith and sort them descending */

        for(int i = 0; i < customer_cnt; i++) {
            customers[i] = i;
        }

        /* Bubble Sort #2. Too bad that we can't use Collections with a sortfunction in Java6. */
        for(int j = 0; j < customer_cnt; j++) {
                for(int k = 1; k < customer_cnt - j; k++) {
                    if(instance.bandwidthOf(customers[k-1]) < instance.bandwidthOf(customers[k])) {
                        int swp = customers[k-1];
                        customers[k-1] = customers[k];
                        customers[k] = swp;
                    }
                }
        }

        System.out.println("Customer Bandwith: " + Arrays.toString(customers));

        /* Step three: Calculate the real distance costs (distance between customer and facility * distanceCost */

        for(int i = 0; i < distanceCosts.length; i++) {
            for(int j = 0; j < distanceCosts[i].length; j++) {
                distanceCosts[i][j] = instance.distances[i][j] * instance.distanceCosts;
            }
        }
	}


    private void branchAndBound() {

    }

    private int lowerBound() {
        
    }

	/**
	 * Diese Methode bekommt vom Framework maximal 30 Sekunden Zeit zur
	 * Verf&uuml;gung gestellt um eine g&uuml;ltige L&ouml;sung
	 * zu finden.
	 * 
	 * <p>
	 * F&uuml;gen Sie hier Ihre Implementierung des Branch-and-Bound Algorithmus
	 * ein.
	 * </p>
	 */
	@Override
	public void run() {
		// TODO: Diese Methode ist von Ihnen zu implementieren
        Main.printDebug("Hello World!");


	}

}
