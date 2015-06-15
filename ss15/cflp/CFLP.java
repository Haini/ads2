package ads2.ss15.cflp;
//package ss15.cflp;

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

        this.instance = instance;

        customer_cnt = instance.getNumCustomers();
        facility_cnt = instance.getNumFacilities();
        distanceCost_cnt = instance.distances.length;

        /* Init all the things. */
        facilities = new int[customer_cnt][facility_cnt];   // Contains all facilities corresponding to the customer
        customers = new int[customer_cnt];                  // Just contains the customers
        distanceCosts = new int[distanceCost_cnt][instance.distances[0].length];

        /* Step one: Calculate the best facility (lowest distance) for every customer. */
        for (int i = 0; i < customer_cnt; i++) {
            int[] facility_index = new int[facility_cnt];

            // Generate Indexvalues
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

            /* Copy the ordered facility_index into the [][] facilities and assign the customer.
            *  In reverse order because I wrote the BubbleSort the wrong way round.
            *  The facilities are sorted in an ascending order (distance from facility to customer). */
            for(int j = facility_cnt-1; j >= 0; j--) {
                facilities[i][facility_cnt-j-1] = facility_index[j];
            }

            //System.out.println("Customer "+i+": " + Arrays.toString(facilities[i]));
            //for(int j = 0; j < facility_cnt; j++) {
            //    System.out.println("Distance: " + instance.distance(facilities[i][j],i));
            //}
        }

        /* Step two: Get the customers with the biggest need of bandwidth and sort them descending. */
        for(int i = 0; i < customer_cnt; i++) {
            customers[i] = i;
        }

        /* Bubble Sort #2. */
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

        /* Step three: Calculate the real distance costs (distance between customer and facility * distanceCost. */
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
        *               - Den bisher verwendeten Pfad (sol[])
        *              - Die laenge des Pfads (eigentlich nicht wirklich noetig, aber zum Abbrechen ist es gemuetlich)
        *              - Die aktuellen Kosten des Pfads, ist auch ganz angenehm so.
        * Ausgabe: Beste gueltige Loesung U (und globale obere Schranke)
        *               - Wird nicht wirklich ausgegeben, sondern nur sporadisch (Beim Beenden des Pfads) gesetzt.
        * Variablen: Liste offerne Probleme; lokale untere Schranke; lokale heuristische Loesung
        *
        */

    private void branchAndBound(int[] customersNode, int[] bandwithNode, int[] sol, int solLength, int currentCost) {

        /* Bounding. */

        int tmp = Integer.MAX_VALUE;

        /* Berechne fuer P' lokale untere Schranke L' mit Dualheuristik (e.g FirstFit). */
        int lowerBound = lowerBound(customersNode, bandwithNode, sol, solLength, currentCost);

        /* Fall L' >= U braucht nicht weiter verfolgt werden. */
        if(getBestSolution() != null) {
            if (!(getBestSolution().getUpperBound() > lowerBound)) {
                return;
            }
        }

        /* Falls L' < U dann...
         * Hier muessen wir ankommen wenn unser LowerBound kleiner war als der UpperBound.
         * Wenn ein vollstaendiger Pfad bestimmt wurde wird dieser auf jeden Fall als neue Loesung
         * festgelegt.*/

        solLength++;
        if(solLength == sol.length) {
            if (getBestSolution() == null) {
                setSolution(currentCost, sol);
            }
            if (getBestSolution().getUpperBound() > currentCost) {
                //System.out.println("Normal solution: " + Arrays.toString(sol));
                setSolution(currentCost, sol);
            }
            return;
        }

        /* Berechne fuer P' gueltige heuristische Loesung --> obere Schranke U'. */
        //System.out.println("Heuristic Call: " + Arrays.toString(sol));

        //tmp = heuristic2(customersNode, bandwithNode, sol, solLength, currentCost);

        /* L' > U muss nicht weiter verfolgt werden. */
        // if(getBestSolution() != null && tmp > getBestSolution().getUpperBound())
        //   return;

        int customer = customers[solLength];


        /* Select a (valid) solution for the next customer and branch it.
           Update the values of available slots and bandwidth.
           When we return from the branch we undo the updates.
         */
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
    }


    /* Calculate a lower bound for the current problem.
    *  Strategy: Iterate over all customers that are not set yet. Select the first available facility (which is the one
     *  with the lowest possible distanceCosts) and ignore facts like openingCosts. */
    private int lowerBound(int[] customersNode, int[] bandwithNode, int[] sol, int solLength, int currentCost) {

        int lowerBound = currentCost;

        for(int i = solLength + 1; i < sol.length; i++) {
            /* Select next customer and assign it to a Node. */
            int smallestFacIndex = 0;
            int customer = customers[i];
            int smallestFac = facilities[customer][smallestFacIndex];

            while ((bandwithNode[smallestFac] < instance.bandwidthOf(customer)) && smallestFacIndex < facilities[customer].length - 1) {
                smallestFacIndex++;
                smallestFac = facilities[customer][smallestFacIndex];
                break;
            }

            lowerBound += distanceCosts[smallestFac][customer];
        }
        return lowerBound;
    }

    /* Get an (not optimal) first solution. */
    private int upperBound(int[] customersNode, int[] bandWithNode, int[] sol) {
        int upperBound = Integer.MAX_VALUE;

        for (int i = 0; i < customer_cnt; i++) {
            int customer = customers[i];
            sol[customer] = facilities[customer][facility_cnt - 1];
        }
        int cost = instance.calcObjectiveValue(sol);
        System.out.println(cost);
        setSolution(cost, sol);
        return upperBound;
    }

    private int heuristic2(int[] customersNode, int[] bandWithNode, int[] sol, int solLength, int currentCost) {

        int[] sol2 = sol.clone();
        int cost = Integer.MAX_VALUE;
        int solCnt = solLength;

        int[] bandWithNode2 = bandWithNode.clone();
        int[] customersNode2 = customersNode.clone();

        //System.out.println("HeuristicStart: " + Arrays.toString(sol2) + "solLenght: " + solLength);

        for (int i = solLength; i < sol.length; i++) {
            int customer = customers[i]; // The current customer

           /* Iterate over all facilities. If we find a facility that provides enough bandwith for the customer
            *  we set it and break;
            */

            for (int facility : facilities[customer]) {
                if (customersNode2[facility] > 0 && bandWithNode2[facility] > instance.bandwidthOf(customer)) {
                    sol2[customer] = facility;
                    customersNode2[facility]--;
                    bandWithNode2[facility] -= instance.bandwidthOf(customer);
                    if (bandWithNode2[facility] <= 0)
                        System.out.println(bandWithNode2[facility]);
                    solCnt++;
                    break;
                }
            }
        }
        //System.out.println();

        //System.out.println("cnt: " + solCnt + " solLength: " + sol.length);

        if (solCnt < sol.length - 1) {
            //System.out.println("No solution: " + solCnt+ " Laenge: " + sol.length);
            return -1;
        }

        cost = instance.calcObjectiveValue(sol2);

        if (getBestSolution() == null) {
            setSolution(cost, sol2);
            //System.out.println("Heuristic Solution: "+ Arrays.toString(sol2) + "  "+ cost);
            return cost;
        }

        if (cost <= getBestSolution().getUpperBound()) {
            setSolution(cost, sol2);
            //System.out.println("Heuristic Solution: "+ Arrays.toString(sol2));
            return cost;
        }
        return -1;
    }

    /* Used to calculate a start UpperBound. */
    private int heuristic(int[] customersNode, int[] bandWithNode, int[] sol, int solLength, int currentCost) {

        int[] sol2 = sol.clone();
        int cost = Integer.MAX_VALUE;
        int solCnt = solLength + 1;

        int[] bandWithNode2 = bandWithNode.clone();
        int[] customersNode2 = customersNode.clone();

        //System.out.println("HeuristicStart: " + Arrays.toString(sol2) + "solLenght: " + solLength);

        for (int i = solLength + 1; i < sol.length; i++) {
            int customer = customers[i]; // The current customer

           /* Iterate over all facilities. If we find a facility that provides enough bandwith for the customer
            *  we set it and break;
            */
            //   System.out.print(i + " ");
            for (int facility : facilities[customer]) {
                if (customersNode2[facility] > 0 && bandWithNode2[facility] > instance.bandwidthOf(customer)) {
                    sol2[i] = facility;
                    customersNode2[facility]--;
                    bandWithNode2[facility] -= instance.bandwidthOf(customer);
                    solCnt++;
                    break;
                }
            }
        }
        //System.out.println();

        //  System.out.println("cnt: " + solCnt + " solLength: " + sol.length);
        if (solCnt < sol.length - 1) {
            // System.out.println("No solution: " + solCnt+ " Laenge: " + sol.length);
            return -1;
        }

        cost = instance.calcObjectiveValue(sol2);

        if (getBestSolution() == null) {
            setSolution(cost, sol2);
            //System.out.println("Heuristic Solution: "+ Arrays.toString(sol2) + "  "+ cost);
            return cost;
        }

        if (cost < getBestSolution().getUpperBound()) {
            setSolution(cost, sol2);
            //System.out.println("Heuristic Solution: "+ Arrays.toString(sol2));
            return cost;
        }
        return -1;
    }


	/**
	 * Diese Methode bekommt vom Framework maximal 30 Sekunden Zeit zur
     * Verfuegung gestellt um eine gueltige Loesung
     * zu finden.
	 * 
	 * <p>
     * Fuegen Sie hier Ihre Implementierung des Branch-and-Bound Algorithmus
     * ein.
	 * </p>
	 */
	@Override
	public void run() {
		// TODO: Diese Methode ist von Ihnen zu implementieren

        /* Init all the things. */
        int[] bandwithNode = new int[facility_cnt];
        int[] customersNode = instance.maxCustomers.clone();    /* Returns the maximum amount of customers per facility. */
        int[] sol = new int[customer_cnt];

        Arrays.fill(sol, -1);
        Arrays.fill(bandwithNode, instance.maxBandwidth);

        /* Get a first solution */
        //int upperBound = upperBound(customersNode, bandwithNode, sol);

        /* This function gives a way better (and 100% valid) UpperBound (e.g 20 000 vs 29 000 on instance 0004) */
        heuristic(customersNode, bandwithNode, sol, -1, 0);

        /* Start all the recursions!*/
        branchAndBound(customersNode, bandwithNode, sol, -1, 0); //Start at -1 because I iterate in the wrong place
    }

}
