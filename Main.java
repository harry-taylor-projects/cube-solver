import java.util.HashMap;

public class Main {

    public static int optimal = 13;
    public static HashMap<Integer, Integer> cornerDist = new HashMap<>();
    public static HashMap<Integer, Integer> cornerGraph = new HashMap<>();
    public static HashMap<Integer, Integer> edgeDist = new HashMap<>();
    public static HashMap<Integer, Integer> edgeGraph = new HashMap<>();

    public static void main(String[] args) {

        for (int i = 0; i < 7; i++) {
            cornerGraph.put(i, 0);
        }

        for (int i = 0; i < 8; i++) {
            edgeGraph.put(i, 0);
        }

        cornerDist.put(0, 0);
        int[] corners = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
        createCornerHashMap(corners, 0);

        edgeDist.put(0, 0);
        boolean[] edges = new boolean[] {true, true, true, true, true, true, true, true, true, true, true, true};
        createEdgeHashMap(edges, 0);

        for (int i = 0; i < 7; i++) {
            //System.out.println(cornerGraph.get(i));
        }

        for (int i = 0; i < 8; i++) {
            //System.out.println(edgeGraph.get(i));
        }

        //initialise cube
        Cube cube = new Cube();

        //generate scramble
        cube.scramble();

        //draw scramble
        cube.drawState();

        //solve
        solveDR(cube.getcO(), cube.geteO(), cube.getEq(), 18, 0, "");
        System.out.println("done");
    }

    public static void createEdgeHashMap(boolean[] eO, int count) {
        if (count < 11) {
            eO[count] = true;
            createEdgeHashMap(eO, count + 1);
            eO[count] = false;
            createEdgeHashMap(eO, count + 1);
        } else if (count == 11) {
            boolean parity = true;
            for (int i = 0; i < 11; i++) {
                if (!eO[i]) {
                    parity = !parity;
                }
            }
            eO[count] = parity;
            if (!edgeDist.containsKey(generateEdgeKey(eO))) {
                findEdgeSolution(eO, 18, 0, false, 8);
            }
            edgeGraph.put(edgeDist.get(generateEdgeKey(eO)), edgeGraph.get(edgeDist.get(generateEdgeKey(eO))) + 1);
        }
    }

    public static int generateEdgeKey(boolean[] eO) {
        int tally = 0;
        for (int i = 0; i < 12; i++) {
            if (!eO[i]) {
                tally += (int) (Math.pow(2, i));
            }
        }
        return tally;
    }

    public static int findEdgeSolution(boolean[] eO, int prev, int depth, boolean move, int best) {
        if (move) {
            Cube cube = new Cube(eO);
            cube.move(prev, 0);
            eO = cube.geteO();
        }

        int key = generateEdgeKey(eO);
        if(edgeDist.containsKey(key)) {
            //distance of current state
            return edgeDist.get(key);
        }

        int value;
        if (depth < best) {
            //i is ud, lr, bf
            for (int i = 0; i < 3; i++) {
                //j is ulb, drf
                for (int j = 0; j < 2; j++) {
                    //k is u, u2, u'
                    for (int k = 0; k < 3; k++) {
                        if (prev < 6 * i || prev > (6 * i) + (3 * j) + 2) {
                            value = findEdgeSolution(eO, (6 * i) + (3 * j) + k, depth + 1, true, best) + 1;
                            if (value < best) {
                                best = value;
                                edgeDist.put(key, best);
                            }
                        }
                    }
                }
            }
        }
        return best;
    }

    public static void createCornerHashMap(int[] cO, int count) {
        if (count < 7) {
            cO[count] = 0;
            createCornerHashMap(cO, count + 1);
            cO[count] = 1;
            createCornerHashMap(cO, count + 1);
            cO[count] = 2;
            createCornerHashMap(cO, count + 1);
        } else if (count == 7) {
            int tally = 0;
            for (int i = 0; i < 7; i++) {
                tally += cO[i];
            }
            cO[count] = (15 - tally) % 3;
            if (!cornerDist.containsKey(generateCornerKey(cO))) {
                findCornerSolution(cO, 18, 0, false, 7);
            }
            cornerGraph.put(cornerDist.get(generateCornerKey(cO)), cornerGraph.get(cornerDist.get(generateCornerKey(cO))) + 1);
        }
    }

    public static int generateCornerKey(int[] cO) {
        int tally = 0;
        for (int i = 0; i < 8; i++) {
            tally += (int) (cO[i] * (Math.pow(3, i)));
        }
        return tally;
    }

    public static int findCornerSolution(int[] cO, int prev, int depth, boolean move, int best) {
        if (move) {
            Cube cube = new Cube(cO);
            cube.move(prev, 1);
            cO = cube.getcO();
        }

        int key = generateCornerKey(cO);
        if(cornerDist.containsKey(key)) {
            //distance of current state
            return cornerDist.get(key);
        }

        int value;
        if (depth < best) {
            //i is ud, lr, bf
            for (int i = 0; i < 3; i++) {
                //j is ulb, drf
                for (int j = 0; j < 2; j++) {
                    //k is u, u2, u'
                    for (int k = 0; k < 3; k++) {
                        if (prev < 6 * i || prev > (6 * i) + (3 * j) + 2) {
                            value = findCornerSolution(cO, (6 * i) + (3 * j) + k, depth + 1, true, best) + 1;
                            if (value < best) {
                                best = value;
                                cornerDist.put(key, best);
                            }
                        }
                    }
                }
            }
        }
        return best;
    }

    public static boolean solveDR(int[] cO, boolean[] eO, boolean[] eq, int prev, int depth, String alg) {
        if (prev < 18) {
            Cube cube = new Cube(cO, eO, eq);
            cube.move(prev, 2);
            cO = cube.getcO();
            eO = cube.geteO();
            eq = cube.getEq();
        }

        //checking if reached domino
        boolean solved = true;
        for (int i = 0; i < 12; i++) {
            if (!(eq[4] && eq[5] && eq[6] && eq[7])) {
                solved = false;
                break;
            }
            if (i < 8) {
                if (cO[i] != 0) {
                    solved = false;
                    break;
                }
            }
            if (!eO[i]) {
                solved = false;
                break;
            }
        }
        if (solved && depth < optimal) {
            optimal = depth;
            System.out.println("Domino reached in: " + depth + " moves");
            System.out.println(alg);
            //return true;
        }

        if (depth + cornerDist.get(generateCornerKey(cO)) > optimal) {
            return false;
        }

        if (depth + edgeDist.get(generateEdgeKey(eO)) > optimal) {
            return false;
        }

        if (depth < optimal - 1) {
            if (prev > 2) {
                if (iterateDR(cO, eO, eq, 0, depth, alg, "U"))
                    return true;
                if (prev > 5) {
                    if (iterateDR(cO, eO, eq, 3, depth, alg, "D"))
                        return true;
                }
            }
            if (prev < 6 || prev > 8) {
                if (iterateDR(cO, eO, eq, 6, depth, alg, "L"))
                    return true;
                if (prev < 9 || prev > 11) {
                    if (iterateDR(cO, eO, eq, 9, depth, alg, "R"))
                        return true;
                }
            }
            if (prev < 12 || prev > 14) {
                if (iterateDR(cO, eO, eq, 12, depth, alg, "B"))
                    return true;
                if (prev < 15 || prev > 17) {
                    return iterateDR(cO, eO, eq, 15, depth, alg, "F");
                }
            }
        }
        return false;
    }

    public static boolean iterateDR(int[] cO, boolean[] eO, boolean[] eq, int prev, int depth, String alg, String face) {
        if (solveDR(cO, eO, eq, prev, depth + 1, alg + face))
            return true;
        if (solveDR(cO, eO, eq,prev + 1, depth + 1, alg + face + "2"))
            return true;
        return solveDR(cO, eO, eq,prev + 2, depth + 1, alg + face + "'");
    }
}
