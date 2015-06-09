package ads2.ss15.cflp;

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

        /* List of Customers, sorted by Bandwithneed */
        int[] customers;

        /* List of Facilities that are available for every customer. */
        int[][] facilities;

        /* List of the real distance costs (distance[][] * distanceCosts) */
        int[][] distanceCosts;

        /* Get all the values from instance. */
        int customer_cnt, facility_cnt, distanceCost_cnt;

        /* Better make a copy of instance obviously */
        CFLPInstance instance;

	public CFLP(CFLPInstance instance) {
		// TODO: Hier ist der richtige Platz fuer Initialisierungen

        this.instance = instance;

        customer_cnt = instance.getNumCustomers();
        facility_cnt = instance.getNumFacilities();
        distanceCost_cnt = instance.distances.length;

        /* Init all the things */
        facilities = new int[customer_cnt][facility_cnt];
        customers = new int[customer_cnt];
        distanceCosts = new int[distanceCost_cnt][instance.distances[0].length];

        /* Step one: Calculate the best faciliy (e.g lowest distance ?!) for every customer */
        for (int i = 0; i < customer_cnt; i++) {
            int[] facility_index = new int[facility_cnt];

            for(int j = 0; j < facility_cnt; j++) {
               facility_index[j] = j;
            }

            /* Now sort facility_index in correspondence to the distance of the facilities to the current customer. */


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
            for(int j = facility_cnt-1; j >= 0; j--) {
                facilities[i][facility_cnt-j-1] = facility_index[j];
            }

            //System.out.println("Customer "+i+": " + Arrays.toString(facilities[i]));
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


        //System.out.println("Customer Bandwith: " + Arrays.toString(customers));
        //System.out.print("Customer Bandwith: [");
       // for(int i = 0; i < customer_cnt; i++) {
        //    System.out.print(instance.bandwidthOf(customers[i])+"," );
        //}
        /* Step three: Calculate the real distance costs (distance between customer and facility * distanceCost */

        for(int i = 0; i < distanceCosts.length; i++) {
            for(int j = 0; j < distanceCosts[i].length; j++) {
                distanceCosts[i][j] = instance.distances[i][j] * instance.distanceCosts;
            }
        }
	}

    /*
        * Eingabe: Problem P
        *           - Wird beschrieben durch die Anzahl der Kunden pro Node, die verfuegbare Bandbreite pro Node.
        *           - Das war es eigentlich schon, da wir aber rekursiv aufrufen wollen gibt es noch:
        *               -
        * Ausgabe: Beste gueltige Loesung U (und globale obere Schranke)
        * Variablen: Liste offerne Probleme; lokale untere Schranke; lokale heuristische Loesung
        *
         */

    private void branchAndBound(int[] customersNode, int[] bandwithNode, int[] sol, int solLength, int currentCost) {

        /* Bounding. */

        /* Berechne fuer P' lokale untere Schranke L' mit Dualheuristik (e.g FirstFit). */
        int lowerBound = lowerBound(customersNode, bandwithNode, sol, solLength, currentCost);

        /* Fall L' >= U braucht nicht weiter verfolgt werden */
        if(getBestSolution() != null) {
            if (!(getBestSolution().getUpperBound() > lowerBound)) {
                return;
            }
        }

        /* Falls L' < U dann */
        solLength++;
        if(solLength == sol.length) {
            //System.out.println("Obere Schranke: " + currentCost);
            setSolution(currentCost, sol);
            return;
        }

        int customer = customers[solLength];

        for(int facility : facilities[customer]) {

            if(customersNode[facility] > 0 && bandwithNode[facility] >= instance.bandwidthOf(customer)) {
                int nextLowerBound = instance.distance(facility, customer) * instance.distanceCosts;
                if(bandwithNode[facility] == instance.maxBandwidth) {
                    nextLowerBound += instance.openingCostsFor(facility);
                }

                sol[customer] = facility;

                bandwithNode[facility] -= instance.bandwidthOf(customer);
                customersNode[facility]--;

                branchAndBound(customersNode, bandwithNode, sol, solLength, nextLowerBound + currentCost);

                bandwithNode[facility] += instance.bandwidthOf(customer);
                customersNode[facility]++;
            }

        }
           /* - berechne fuer P' gueltige heuristische Loesung --> obere Schranke U'
            - falls U' < U dann
                - U = U' --> neue beste Loesung
                - entferne aus PI alle Subprobleme mit lokaler unterer Schranke >= U
         */

        /* Fall L' >= U braucht nicht weiter verfolgt werden */

        /* Falls L' < U dann
            - Branching von P' in P1...Pk
          */

    }


    private int lowerBound(int[] customersNode, int[] bandwithNode, int[] sol, int solLength, int currentCost) {

        int lowerBound = currentCost;

        for(int i = solLength + 1; i < sol.length; i++) {
            /* Select next customer and assign it to a Node. */
            int smallestFacIndex = 0;
            int customer = customers[i];
            int smallestFac = facilities[customer][smallestFacIndex];

            while((bandwithNode[smallestFac] < instance.bandwidthOf(customer) || customersNode[smallestFac] == 0) && smallestFacIndex < facilities[customer].length-1) {
                smallestFacIndex++;
                smallestFac = facilities[customer][smallestFacIndex];
            }
            lowerBound += distanceCosts[smallestFac][customer];
            //lowerBound+=instance.distance(smallestFac, customer) * instance.distanceCosts;

            /* Which customer? Just the next customer, as we already did sort them by bandwith need. */
//            int fIndex = facilities[i][0];
            /* Just pick the closest facility and ignore the constraints. */
 //           lowerBound += instance.distance(fIndex, i) * instance.distanceCosts;


        }
        /* Return this baaaad lowerBound */
        return lowerBound;
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

        /* Init all the things. */
        int[] bandwithNode = new int[facility_cnt];
        int[] customersNode = instance.maxCustomers.clone();
        int[] sol = new int[customer_cnt];
        Arrays.fill(sol, -1);
        Arrays.fill(bandwithNode, instance.maxBandwidth);
        /* Start all the recursions! */
        branchAndBound(customersNode, bandwithNode, sol, -1, 0 );
        Main.printDebug("Hello World!");


	}

}
