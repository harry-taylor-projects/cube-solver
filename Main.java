import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class Main {

    static int[] cO = new int[8];
    static boolean[] eO = new boolean[] {true, true, true, true, true, true, true, true, true, true, true, true};
    static int[] cP = new int[] {0, 1, 2, 3, 4, 5, 6, 7};
    static int[] eP = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
    static int[] coDistTable = new int[2187];
    static int[] eoDistTable = new int[2048];
    static int[] cpDistTable = new int[40320];
    static int[] epDistTable = new int[40320];
    static int[][] coMoveTable = new int[2187][18];
    static int[][] eoMoveTable = new int[2048][18];
    static int[][] eqlMoveTable = new int[495][18];
    static int[][] cpMoveTable = new int[40320][18];
    static int[][] epMoveTable = new int[40320][10];
    static int[][] eqpMoveTable = new int[24][10];
    static int[][] helperTable = new int[11880][18];
    static int[][] transitionTable = new int[11880][24];
    static int optimal = 24;
    static long time;
    static String solution;

    public static void main(String[] args) {

        //Create Tables
        createTables();

        //Get Scramble
        Scanner scan = new Scanner(System.in);  // Create a Scanner object
        System.out.println("Enter scramble, E.g. R U' F2 B' L2 D (typing something invalid will generate a random scramble)");
        String scramble = scan.nextLine();  // Read user input
        if (!doAlgorithm(scramble)) {
            scramble();
        }

        //Draw Scramble
        drawState();

        //Initialise moves
        int[] moves = new int[optimal];
        Arrays.fill(moves, 18);

        //Solve
        for (int maxDepth = 9; maxDepth < 13; maxDepth++) {
            System.out.println("Searching depth " + maxDepth + " DRs...");
            time = System.currentTimeMillis();
            boolean[] eqL = new boolean[12];
            for (int axis = 0; axis < 3; axis++) {

                //Get equator and helper locations from ep
                for (int i = 0; i < 12; i++) {
                    eqL[i] = (4 <= eP[i] && eP[i] <= 7);
                }
                int[][] helpers = getHelpers();

                //Search
                reachDR(getCOKey(cO), getEOKey(eO), getEqLKey(eqL), getPKey(cP), getHelperKey(helpers[0]), getHelperKey(helpers[1]), getHelperKey(helpers[2]), 18, 0, maxDepth, moves, axis);
                nextAxis();
            }
            System.out.println("Searched all " + maxDepth + " move DRs");
        }
        System.out.println("Finished. Best solution found: " + solution + "(" + optimal + " moves)");
    }

    //Creates distance and move tables
    static void createTables() {

        //Corner Orientation Move Table
        createCOMoveTable(new int[8], 0);

        //Corner Orientation Distance Table
        Arrays.fill(coDistTable, 6);
        createCODistTable(0, 0);

        //Edge Orientation Move Table
        boolean[] edges = new boolean[12];
        Arrays.fill(edges, true);
        createEOMoveTable(edges, 0);

        //Edge Orientation Distance Table
        Arrays.fill(eoDistTable, 7);
        createEODistTable(0, 0);

        //Equator Location Move Table
        createEqLMoveTable();

        //Corner & Edge Permutation Tables (includes Transition Table)
        createPMoveTables(new int[] {0, 1, 2, 3, 4, 5, 6, 7}, 7);
        Arrays.fill(cpDistTable, 13);
        Arrays.fill(epDistTable, 8);
        createCPDistTable(0,0);
        createEPDistTable(0,0);

        //Equator Permutation Move Table
        createEqPMoveTable(new int[] {4, 5, 6, 7}, 3);

        //Helper Move Table
        createHelperTable();
    }

    //Applies notation at the cubie level
    static boolean doAlgorithm(String alg) {
        String[] notation = alg.split(" ");
        int[] moves = new int[notation.length];
        int val;
        for (int i = 0; i < notation.length; i++) {
            val = switch (notation[i]) {
                case "U" -> 0;
                case "U2" -> 1;
                case "U'" -> 2;
                case "D" -> 3;
                case "D2" -> 4;
                case "D'" -> 5;
                case "L" -> 6;
                case "L2" -> 7;
                case "L'" -> 8;
                case "R" -> 9;
                case "R2" -> 10;
                case "R'" -> 11;
                case "B" -> 12;
                case "B2" -> 13;
                case "B'" -> 14;
                case "F" -> 15;
                case "F2" -> 16;
                case "F'" -> 17;
                default -> 18;
            };
            if (val == 18) {
                return false;
            } else {
                moves[i] = val;
            }
        }
        for (int move : moves) {
            cO = moveCO(cO, move);
            eO = moveEO(eO, move);
            cP = moveCP(cP, move);
            eP = moveEP(eP, move);
        }
        return true;
    }

    //Scrambles the cube at the cubie level
    static void scramble() {
        Random rnd = new Random();

        //Corners
        int counter = 0;
        boolean parity = true;
        for (int i = 0; i < 8; i++) {
            int swap = rnd.nextInt(i, 8);
            parity = (swap == i) == parity;
            int temp = cP[i];
            cP[i] = cP[swap];
            cP[swap] = temp;
            if (i < 7) {
                cO[i] = rnd.nextInt(0, 3);
                counter += cO[i];
            } else {
                cO[i] = (15 - counter) % 3;
            }
        }

        //Edges
        boolean flip = true;
        int toggle;
        for (int i = 0; i < 12; i++) {
            int swap = rnd.nextInt(i, 12);
            parity = (swap == i) == parity;
            int temp = eP[i];
            eP[i] = eP[swap];
            eP[swap] = temp;
            if (i < 11) {
                toggle = rnd.nextInt(0, 2);
                eO[i] = toggle == 0;
                flip = (toggle == 0) == flip;
            } else {
                eO[i] = flip;
            }
        }

        //Fix PLL Parity
        if (!parity) {
            int temp = eP[0];
            eP[0] = eP[1];
            eP[1] = temp;
        }
    }

    //Draws the state of the cube
    static void drawState() {
        String[] state = new String[48];
        String[] cornerColours = new String[3];
        for (int i = 0; i < 8; i++) {
            cornerColours = switch (cP[i]) {
                case 0 -> new String[]{"w", "o", "b"};
                case 1 -> new String[]{"w", "b", "r"};
                case 2 -> new String[]{"w", "g", "o"};
                case 3 -> new String[]{"w", "r", "g"};
                case 4 -> new String[]{"y", "b", "o"};
                case 5 -> new String[]{"y", "r", "b"};
                case 6 -> new String[]{"y", "o", "g"};
                case 7 -> new String[]{"y", "g", "r"};
                default -> cornerColours;
            };
            state[(3 * i) + cO[i]] = cornerColours[0];
            state[(3 * i) + ((cO[i] + 1) % 3)] = cornerColours[1];
            state[(3 * i) + ((cO[i] + 2) % 3)] = cornerColours[2];
        }
        String[] edgeColours = new String[2];
        for (int i = 0; i < 12; i++) {
            edgeColours = switch (eP[i]) {
                case 0 -> new String[]{"w", "b"};
                case 1 -> new String[]{"w", "o"};
                case 2 -> new String[]{"w", "r"};
                case 3 -> new String[]{"w", "g"};
                case 4 -> new String[]{"b", "o"};
                case 5 -> new String[]{"b", "r"};
                case 6 -> new String[]{"g", "o"};
                case 7 -> new String[]{"g", "r"};
                case 8 -> new String[]{"y", "b"};
                case 9 -> new String[]{"y", "o"};
                case 10 -> new String[]{"y", "r"};
                case 11 -> new String[]{"y", "g"};
                default -> edgeColours;
            };
            if (eO[i]) {
                state[24 + (2 * i)] = edgeColours[0];
                state [25 + (2 * i)] = edgeColours[1];
            } else {
                state[25 + (2 * i)] = edgeColours[0];
                state [24 + (2 * i)] = edgeColours[1];
            }
        }
        System.out.println("     " + state[0] + state[24] + state[3]);
        System.out.println("     " + state[26] + "w" + state[28]);
        System.out.println("     " + state[6] + state[30] + state[9]);
        System.out.println(" " + state[1] + state[27] + state[8] + " " + state[7] + state[31] + state[11] + " " + state[10] + state[29] + state[5] + " " + state[4] + state[25] + state[2]);
        System.out.println(" " + state[33] + "o" + state[37] + " " + state[36] + "g" + state[38] + " " + state[39] + "r" + state[35] + " " + state[34] + "b" + state[32]);
        System.out.println(" " + state[14] + state[43] + state[19] + " " + state[20] + state[47] + state[22] + " " + state[23] + state[45] + state[16] + " " + state[17] + state[41] + state[13]);
        System.out.println("     " + state[18] + state[46] + state[21]);
        System.out.println("     " + state[42] + "y" + state[44]);
        System.out.println("     " + state[12] + state[40] + state[15]);
    }

    //Creates helpers array from eP
    static int[][] getHelpers() {
        int[][] helpers = new int[3][4];
        for (int i = 0; i < 12; i++) {
            switch (eP[i]) {
                case 0: helpers[1][0] = i;
                    break;
                case 1: helpers[2][0] = i;
                    break;
                case 2: helpers[2][1] = i;
                    break;
                case 3: helpers[1][1] = i;
                    break;
                case 4: helpers[0][0] = i;
                    break;
                case 5: helpers[0][1] = i;
                    break;
                case 6: helpers[0][2] = i;
                    break;
                case 7: helpers[0][3] = i;
                    break;
                case 8: helpers[1][2] = i;
                    break;
                case 9: helpers[2][2] = i;
                    break;
                case 10: helpers[2][3] = i;
                    break;
                case 11: helpers[1][3] = i;
                    break;
            }
        }
        return helpers;
    }

    //Switches the DR axis
    static void nextAxis() {

        //Translate Corner Pieces
        int[] tempCO = new int[8];
        int[] tempCP = new int[8];
        for (int i = 0; i < 2; i++) {
            int[] perm = getAxisCornerCycle(i);
            for (int j = 0; j < 8; j++) {
                tempCO[j] = cO[perm[j]];
                tempCP[j] = cP[perm[j]];
            }
        }

        //Translate Edge Pieces
        boolean[] tempEO = new boolean[12];
        int[] tempEP = new int[12];
        for (int i = 0; i < 4; i++) {
            int[] perm = getAxisEdgeCycle(i);
            for (int j = 0; j < 12; j++) {
                tempEO[j] = eO[perm[j]];
                tempEP[j] = eP[perm[j]];
            }
        }

        cO = tempCO.clone();
        cP = tempCP.clone();
        eO = tempEO.clone();
        eP = tempEP.clone();

        //Correct Corner Targets
        for (int i = 0; i < 8; i++) {
            cP[i] = switch (cP[i]) {
                case 0 -> 5;
                case 1 -> 7;
                case 2 -> 1;
                case 3 -> 3;
                case 4 -> 4;
                case 5 -> 6;
                case 6 -> 0;
                default -> 2;
            };
        }

        //Correct Edge Targets
        for (int i = 0; i < 12; i++) {
            eP[i] = switch (eP[i]) {
                case 0 -> 10;
                case 1 -> 5;
                case 2 -> 7;
                case 3 -> 2;
                case 4 -> 8;
                case 5 -> 11;
                case 6 -> 0;
                case 7 -> 3;
                case 8 -> 9;
                case 9 -> 4;
                case 10 -> 6;
                default -> 1;
            };
        }

        //Correct Corner Orientation
        for (int i = 0; i < 8; i++) {
            if ((i == 0 || i == 3 || i == 5 || i == 6) & (cP[i] == 1 || cP[i] == 2 || cP[i] == 4 || cP[i] == 7)) {
                cO[i] = ((cO[i] + 2) % 3);
            } else if ((i == 1 || i == 2 || i == 4 || i == 7) & (cP[i] == 0 || cP[i] == 3 || cP[i] == 5 || cP[i] == 6)) {
                cO[i] = ((cO[i] + 1) % 3);
            }
        }

        //Correct Edge Orientation
        for (int i = 0; i < 12; i++) {
            if ((i == 10 || i == 5 || i == 7 || i == 2 || i == 9 || i == 4 || i == 6 || i == 1) & (eP[i] == 8 || eP[i] == 11 || eP[i] == 0 || eP[i] == 3)) {
                eO[i] = !eO[i];
            } else if ((i == 8 || i == 11 || i == 0 || i == 3) & (eP[i] == 10 || eP[i] == 5 || eP[i] == 7 || eP[i] == 2 || eP[i] == 9 || eP[i] == 4 || eP[i] == 6 || eP[i] == 1)) {
                eO[i] = !eO[i];
            }
        }
    }

    //Reach DR
    static void reachDR(int cO, int eO, int eqL, int cP, int UD, int RL, int FB, int lastMove, int depth, int maxDepth, int[] moves, int axis) {

        //Updates user on completion percentage if enough time has passed
        if (System.currentTimeMillis() - time > 10000) {
            System.out.println( (int) (100 * ((axis + ((moves[0] + ((float) moves[1] / 18)) / 18)) / 3)) + "%");
            time = System.currentTimeMillis();
        }

        //Checks if domino has been reached
        if (cO == 0 && eO == 0 && eqL == 425) {
            if (depth == maxDepth || maxDepth == 9) {
                solveDR(cP, transitionTable[RL][(FB % 24)], (UD % 24), 18, depth, moves.clone(), axis);
            } else {
                return;
            }
        }

        //Prune
        if (depth >= maxDepth || depth + coDistTable[cO] > maxDepth || depth + eoDistTable[eO] > maxDepth || (maxDepth >= optimal && maxDepth != 9)) {
            return;
        }

        //Increase depth
        for (int i = 0; i < 18; i++) {
            if (doMove(lastMove, i, false)) {
                moves[depth] = i;
                reachDR(coMoveTable[cO][i],
                        eoMoveTable[eO][i],
                        eqlMoveTable[eqL][i],
                        cpMoveTable[cP][i],
                        helperTable[UD][i],
                        helperTable[RL][i],
                        helperTable[FB][i],
                        i, depth + 1, maxDepth, moves.clone(), axis);
            }
        }
    }

    //Solve DR
    static void solveDR(int cP, int eP, int eqP, int lastMove, int depth, int[] moves, int axis) {

        //Checks if solved
        boolean solved = cP == 0 && eP == 0 && eqP == 0;
        if (solved && depth < optimal) {
            optimal = depth;
            solution = toNotation(moves, axis);
            System.out.println("Solution found in " + depth + " moves: " + solution);
            return;
        }

        //Prune
        if (depth >= optimal - 1 || depth + cpDistTable[cP] >= optimal || depth + epDistTable[eP] >= optimal) {
            return;
        }

        //Increase depth
        for (int i = 0; i < 10; i++) {
            if (doMove(lastMove, i, true)) {
                moves[depth] = convertMoveIndex(i);
                solveDR(cpMoveTable[cP][convertMoveIndex(i)],
                        epMoveTable[eP][i],
                        eqpMoveTable[eqP][i],
                        i, depth + 1, moves.clone(), axis);
            }
        }
    }

    //Corner Orientation Tables
    static int getCOKey(int[] cO) {
        int key = 0;
        for (int i = 0; i < 7; i++) {
            key += (int) (cO[i] * (Math.pow(3, i)));
        }
        return key;
    }

    static void createCOMoveTable(int[] cO, int count) {
        if (count < 7) {
            cO[count] = 0;
            createCOMoveTable(cO, count + 1);
            cO[count] = 1;
            createCOMoveTable(cO, count + 1);
            cO[count] = 2;
            createCOMoveTable(cO, count + 1);
        } else if (count == 7) {
            int tally = 0;
            for (int i = 0; i < 7; i++) {
                tally += cO[i];
            }
            cO[count] = (15 - tally) % 3;
            for (int i = 0; i < 18; i++) {
                coMoveTable[getCOKey(cO)][i] = getCOKey(moveCO(cO, i));
            }
        }
    }

    static void createCODistTable(int cO, int depth) {
        if (depth < coDistTable[cO]) {
            coDistTable[cO] = depth;
            for (int i = 0; i < 18; i++) {
                createCODistTable(coMoveTable[cO][i],depth + 1);
            }
        }
    }

    //Edge Orientation Tables
    static int getEOKey(boolean[] eO) {
        int tally = 0;
        for (int i = 0; i < 11; i++) {
            if (!eO[i]) {
                tally += (int) (Math.pow(2, i));
            }
        }
        return tally;
    }

    static void createEOMoveTable(boolean[] eO, int count) {
        if (count < 11) {
            eO[count] = true;
            createEOMoveTable(eO, count + 1);
            eO[count] = false;
            createEOMoveTable(eO, count + 1);
        } else if (count == 11) {
            boolean parity = true;
            for (int i = 0; i < 11; i++) {
                if (!eO[i]) {
                    parity = !parity;
                }
            }
            eO[count] = parity;
            for (int i = 0; i < 18; i++) {
                eoMoveTable[getEOKey(eO)][i] = getEOKey(moveEO(eO, i));
            }
        }
    }

    static void createEODistTable(int eO, int depth) {
        if (depth < eoDistTable[eO]) {
            eoDistTable[eO] = depth;
            for (int i = 0; i < 18; i++) {
                createEODistTable(eoMoveTable[eO][i],depth + 1);
            }
        }
    }

    //Equator Locations Table
    static int getEqLKey(boolean[] eq) {
        int seen = 0;
        int key = 0;
        for(int i = 0; i < 12; i++) {
            if (eq[i]) {
                seen++;
            } else if (seen > 0) {
                int num = 1;
                int den = 1;
                for(int r = 0; r < seen - 1; r++) {
                    num *= i - r;
                    den *= r + 1;
                }
                key += num/den;
            }
        }
        return key;
    }

    static void createEqLMoveTable() {
        for(int i = 0; i < 9; i++) {
            for(int j = i + 1; j < 10; j++) {
                for(int k = j + 1; k < 11; k++) {
                    for(int l = k + 1; l < 12; l++) {
                        boolean[] eq = new boolean[12];
                        eq[i] = true;
                        eq[j] = true;
                        eq[k] = true;
                        eq[l] = true;
                        for (int m = 0; m < 18; m++) {
                            eqlMoveTable[getEqLKey(eq)][m] = getEqLKey(moveEqL(eq, m));
                        }
                    }
                }
            }
        }
    }

    //Corner & Edge Permutation Tables
    static int getPKey(int[] p) {
        int key = 0;
        for (int i = 0; i < p.length; i++) {
            int seen = 0;
            for (int j = 0; j < i; j++) {
                if (p[j] > p[i]) {
                    seen++;
                }
            }
            for (int j = 0; j < i; j++) {
                seen *= j + 1;
            }
            key += seen;
        }
        return key;
    }

    static void createPMoveTables(int[] p, int count) {
        if (count > 0) {
            for (int i = 0; i < count + 1; i++) {
                int[] newP = p.clone();
                int temp = newP[count - i];
                for (int j = 0; j < i; j++) {
                    newP[count - i + j] = newP[count - i + j + 1];
                }
                newP[count] = temp;
                createPMoveTables(newP, count - 1);
            }
        } else {
            for(int i = 0; i < 18; i++) {
                cpMoveTable[getPKey(p)][i] = getPKey(moveCP(p, i));
            }
            for(int i = 0; i < 10; i++) {
                int[] movedP = moveEP(new int[] {p[0], p[1], p[2], p[3], 4, 5, 6, 7, p[4], p[5], p[6], p[7]}, convertMoveIndex(i));
                int[] condensedP = new int[] {movedP[0], movedP[1], movedP[2], movedP[3], movedP[8], movedP[9], movedP[10], movedP[11]};
                epMoveTable[getPKey(p)][i] = getPKey(condensedP);
            }
            createTransitionTable(p);
        }
    }

    static void createCPDistTable(int cP, int depth) {
        if (depth < cpDistTable[cP]) {
            cpDistTable[cP] = depth;
            for (int i = 0; i < 10; i++) {
                createCPDistTable(cpMoveTable[cP][convertMoveIndex(i)],depth + 1);
            }
        }
    }

    static void createEPDistTable(int eP, int depth) {
        if (depth < epDistTable[eP]) {
            epDistTable[eP] = depth;
            for (int i = 0; i < 10; i++) {
                createEPDistTable(epMoveTable[eP][i],depth + 1);
            }
        }
    }

    //Equator Permutation Table
    static void createEqPMoveTable(int[] eqP, int count) {
        if (count > 0) {
            for (int i = 0; i < count + 1; i++) {
                int[] newP = eqP.clone();
                int temp = newP[count - i];
                for (int j = 0; j < i; j++) {
                    newP[count - i + j] = newP[count - i + j + 1];
                }
                newP[count] = temp;
                createEqPMoveTable(newP, count - 1);
            }
        } else {
            for(int i = 0; i < 10; i++) {
                eqpMoveTable[getPKey(eqP)][i] = getPKey(moveHelper(convertMoveIndex(i), eqP));
            }
        }
    }

    //Helper Table
    static int getHelperKey(int[] helper) {
        boolean[] locations = new boolean[12];
        for (int i = 0; i < 4; i++) {
            locations[helper[i]] = true;
        }
        return (getEqLKey(locations) * 24) + getPKey(helper);
    }

    static int[] moveHelper(int move, int[] helper) {
        int[] ep = moveEP(new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}, move);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 12; j++) {
                if (helper[i] == ep[j]) {
                    helper[i] = j;
                    break;
                }
            }
        }
        return helper;
    }

    static void createHelperTable() {
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 12; j++) {
                if (j == i)
                    continue;
                for (int k = 0; k < 12; k++) {
                    if (k == i || k == j)
                        continue;
                    for (int l = 0; l < 12; l++) {
                        if (l == i || l == j || l == k)
                            continue;
                        int[] helper = new int[] {i, j, k, l};
                        for (int move = 0; move < 18; move++) {
                            helperTable[getHelperKey(helper)][move] = getHelperKey(moveHelper(move, helper));
                        }
                    }
                }
            }
        }
    }

    //Transition table
    static void createTransitionTable(int[] p) {
        int[] RL = new int[4];
        int[] FB = new int[4];
        for (int i = 0; i < 8; i++) {
            int val = i;
            if (i > 3)
                val += 4;
            switch (p[i]) {
                case 0: RL[0] = val;
                    break;
                case 1: FB[0] = val;
                    break;
                case 2: FB[1] = val;
                    break;
                case 3: RL[1] = val;
                    break;
                case 4: RL[2] = val;
                    break;
                case 5: FB[2] = val;
                    break;
                case 6: FB[3] = val;
                    break;
                case 7: RL[3] = val;
                    break;
            }
        }
        transitionTable[getHelperKey(RL)][getPKey(FB)] = getPKey(p);
    }

    //Move pieces at the cubie level
    static int[] getCornerCycle(int move) {
        return switch (move) {
            case 0 -> new int[] {2, 0, 3, 1, 4, 5, 6, 7}; //u
            case 1 -> new int[] {3, 2, 1, 0, 4, 5, 6, 7}; //u2
            case 2 -> new int[] {1, 3, 0, 2, 4, 5, 6, 7}; //u'
            case 3 -> new int[] {0, 1, 2, 3, 5, 7, 4, 6}; //d
            case 4 -> new int[] {0, 1, 2, 3, 7, 6, 5, 4}; //d2
            case 5 -> new int[] {0, 1, 2, 3, 6, 4, 7, 5}; //d'
            case 6 -> new int[] {4, 1, 0, 3, 6, 5, 2, 7}; //l
            case 7 -> new int[] {6, 1, 4, 3, 2, 5, 0, 7}; //l2
            case 8 -> new int[] {2, 1, 6, 3, 0, 5, 4, 7}; //l'
            case 9 -> new int[] {0, 3, 2, 7, 4, 1, 6, 5}; //r
            case 10 -> new int[] {0, 7, 2, 5, 4, 3, 6, 1}; //r2
            case 11 -> new int[] {0, 5, 2, 1, 4, 7, 6, 3}; //r'
            case 12 -> new int[] {1, 5, 2, 3, 0, 4, 6, 7}; //b
            case 13 -> new int[] {5, 4, 2, 3, 1, 0, 6, 7}; //b2
            case 14 -> new int[] {4, 0, 2, 3, 5, 1, 6, 7}; //b'
            case 15 -> new int[] {0, 1, 6, 2, 4, 5, 7, 3}; //f
            case 16 -> new int[] {0, 1, 7, 6, 4, 5, 3, 2}; //f2
            case 17 -> new int[] {0, 1, 3, 7, 4, 5, 2, 6}; //f'
            default -> new int[] {0, 1, 2, 3, 4, 5, 6, 7};
        };
    }

    static int[] getEdgeCycle(int move) {
        return switch (move) {
            case 0 -> new int[] {1, 3, 0, 2, 4, 5, 6, 7, 8, 9, 10, 11}; //u
            case 1 -> new int[] {3, 2, 1, 0, 4, 5, 6, 7, 8, 9, 10, 11}; //u2
            case 2 -> new int[] {2, 0, 3, 1, 4, 5, 6, 7, 8, 9, 10, 11}; //u'
            case 3 -> new int[] {0, 1, 2, 3, 4, 5, 6, 7, 10, 8, 11, 9}; //d
            case 4 -> new int[] {0, 1, 2, 3, 4, 5, 6, 7, 11, 10, 9, 8}; //d2
            case 5 -> new int[] {0, 1, 2, 3, 4, 5, 6, 7, 9, 11, 8, 10}; //d'
            case 6 -> new int[] {0, 4, 2, 3, 9, 5, 1, 7, 8, 6, 10, 11}; //l
            case 7 -> new int[] {0, 9, 2, 3, 6, 5, 4, 7, 8, 1, 10, 11}; //l2
            case 8 -> new int[] {0, 6, 2, 3, 1, 5, 9, 7, 8, 4, 10, 11}; //l'
            case 9 -> new int[] {0, 1, 7, 3, 4, 2, 6, 10, 8, 9, 5, 11}; //r
            case 10 -> new int[] {0, 1, 10, 3, 4, 7, 6, 5, 8, 9, 2, 11}; //r2
            case 11 -> new int[] {0, 1, 5, 3, 4, 10, 6, 2, 8, 9, 7, 11}; //r'
            case 12 -> new int[] {5, 1, 2, 3, 0, 8, 6, 7, 4, 9, 10, 11}; //b
            case 13 -> new int[] {8, 1, 2, 3, 5, 4, 6, 7, 0, 9, 10, 11}; //b2
            case 14 -> new int[] {4, 1, 2, 3, 8, 0, 6, 7, 5, 9, 10, 11}; //b'
            case 15 -> new int[] {0, 1, 2, 6, 4, 5, 11, 3, 8, 9, 10, 7}; //f
            case 16 -> new int[] {0, 1, 2, 11, 4, 5, 7, 6, 8, 9, 10, 3}; //f2
            case 17 -> new int[] {0, 1, 2, 7, 4, 5, 3, 11, 8, 9, 10, 6}; //f'
            default -> new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        };
    }

    static int[] moveCO(int[] cO, int move) {
        int[] newCO = new int[8];
        int[] perm = getCornerCycle(move);
        int[] twist = switch (move) {
            case 6, 8 -> new int[] {2, 0, 1, 0, 1, 0, 2, 0}; //l & l'
            case 9, 11 -> new int[] {0, 1, 0, 2, 0, 2, 0, 1}; //r & r'
            case 12, 14 -> new int[] {1, 2, 0, 0, 2, 1, 0, 0}; //b & b'
            case 15, 17 -> new int[] {0, 0, 2, 1, 0, 0, 1, 2}; //f & f'
            default -> new int[] {0, 0, 0, 0, 0, 0, 0, 0};
        };
        for (int i = 0; i < 8; i++) {
            newCO[i] = ((cO[perm[i]] + twist[i]) % 3);
        }
        return newCO;
    }

    static boolean[] moveEO(boolean[] eO, int move) {
        boolean[] newEO = new boolean[12];
        int[] perm = getEdgeCycle(move);
        boolean[] flip = switch (move) {
            case 12, 14 -> new boolean[] {false, true, true, true, false, false, true, true, false, true, true, true}; //b & b'
            case 15, 17 -> new boolean[] {true, true, true, false, true, true, false, false, true, true, true, false}; //f & f'
            default -> new boolean[] {true, true, true, true, true, true, true, true, true, true, true, true};
        };
        for (int i = 0; i < 12; i++) {
            newEO[i] = eO[perm[i]] == flip[i];
        }
        return newEO;
    }

    static boolean[] moveEqL(boolean[] eqL, int move) {
        boolean[] newEqL = new boolean[12];
        int[] perm = getEdgeCycle(move);
        for (int i = 0; i < 12; i++) {
            newEqL[i] = eqL[perm[i]];
        }
        return newEqL;
    }

    static int[] moveCP(int[] cP, int move) {
        int[] newCP = new int[8];
        int[] perm = getCornerCycle(move);
        for (int i = 0; i < 8; i++) {
            newCP[i] = cP[perm[i]];
        }
        return newCP;
    }

    static int[] moveEP(int[] eP, int move) {
        int[] newEP = new int[12];
        int[] perm = getEdgeCycle(move);
        for (int i = 0; i < 12; i++) {
            newEP[i] = eP[perm[i]];
        }
        return newEP;
    }

    //Converts DR move index (0-9) to normal move index (0 - 17)
    static int convertMoveIndex(int move) {
        return switch (move) {
            case 6 -> 7;
            case 7 -> 10;
            case 8 -> 13;
            case 9 -> 16;
            default -> move;
        };
    }

    static int[] getAxisCornerCycle(int index) {
        return switch (index) {
            case 0 -> new int[] {6, 1, 2, 3, 4, 0, 5, 7};
            case 1 -> new int[] {0, 2, 7, 3, 4, 5, 6, 1}; //u2
            default -> new int[] {0, 1, 2, 3, 4, 5, 6, 7};
        };

    }

    static int[] getAxisEdgeCycle(int index) {
        return switch (index) {
            case 0 -> new int[] {6, 1, 2, 3, 4, 5, 10, 7, 8, 9, 0, 11};
            case 1 -> new int[] {0, 11, 2, 3, 4, 1, 6, 7, 8, 9, 10, 5};
            case 2 -> new int[] {0, 1, 3, 7, 4, 5, 6, 2, 8, 9, 10, 11};
            case 3 -> new int[] {0, 1, 2, 3, 9, 5, 6, 7, 4, 8, 10, 11};
            default -> new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        };
    }

    //Converts a sequence of moves into notation
    static String toNotation(int[] moves, int axis) {
        StringBuilder alg = new StringBuilder();
        for (int move : moves) {
            if (move == 18)
                continue;
            int type = move % 3;
            if (axis == 0) {
                alg.append(switch (move - type) {
                    case 0 -> "U";
                    case 3 -> "D";
                    case 6 -> "L";
                    case 9 -> "R";
                    case 12 -> "B";
                    case 15 -> "F";
                    default -> "";
                });
            } else if (axis == 1) {
                alg.append(switch (move - type) {
                    case 0 -> "F";
                    case 3 -> "B";
                    case 6 -> "D";
                    case 9 -> "U";
                    case 12 -> "L";
                    case 15 -> "R";
                    default -> "";
                });
            } else {
                alg.append(switch (move - type) {
                    case 0 -> "R";
                    case 3 -> "L";
                    case 6 -> "B";
                    case 9 -> "F";
                    case 12 -> "D";
                    case 15 -> "U";
                    default -> "";
                });
            }
            if (type == 1) {
                alg.append("2");
            } else if (type == 2) {
                alg.append("'");
            }
            alg.append(" ");
        }
        return alg.toString();
    }

    //Skips moves which result in duplicate states
    static boolean doMove(int lastMove, int currentMove, boolean DR) {
        if (DR) {
            lastMove = convertMoveIndex(lastMove);
            currentMove = convertMoveIndex(currentMove);
        }
        return lastMove / 3 != currentMove / 3 && (((currentMove / 3) % 2) != 1 || lastMove / 6 != currentMove / 6);
    }
}
