import java.io.*;
import java.util.Random;

public class Main {

    public static int firstMaxDepth = 0;
    public static int totalDRs = 0;
    public static int optimal = 31;
    public static int[] cO = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
    public static boolean[] eO = new boolean[] {true, true, true, true, true, true, true, true, true, true, true, true};
    public static boolean[] eqO = new boolean[] {false, false, false, false, true, true, true, true, false, false, false, false};
    public static int[] cP = new int[] {0, 1, 2, 3, 4, 5, 6, 7};
    public static int[] eP = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
    public static int[] coDistTable = new int[2187];
    public static int[] eoDistTable = new int[2048];
    public static int[] cpDistTable = new int[40320];
    public static int[] epDistTable = new int[40320];
    public static int[][] coMoveTable = new int[2187][18];
    public static int[][] eoMoveTable = new int[2048][18];
    public static int[][] eqoMoveTable = new int[495][18];
    public static int[][] cpMoveTable = new int[40320][10];
    public static int[][] epMoveTable = new int[40320][10];
    public static int[][] eqpMoveTable = new int[24][10];
    public static int[] solution = new int[31];

    public static void main(String[] args) {

        //Create Tables (if they haven't already been made)
        createTables();

        //Import tables
        coDistTable = importDistanceTable("coDist.txt", 2187);
        eoDistTable = importDistanceTable("eoDist.txt", 2048);
        cpDistTable = importDistanceTable("cpDist.txt", 40320);
        epDistTable = importDistanceTable("epDist.txt", 40320);
        coMoveTable = importMoveTable("coMove.txt", 2187, 18);
        eoMoveTable = importMoveTable("eoMove.txt", 2048, 18);
        eqoMoveTable = importMoveTable("eqoMove.txt", 495, 18);
        cpMoveTable = importMoveTable("cpMove.txt", 40320, 10);
        epMoveTable = importMoveTable("epMove.txt", 40320, 10);
        eqpMoveTable = importMoveTable("eqpMove.txt", 24, 10);

        //generate scramble
        scramble();

        //draw scramble
        drawState();

        for (int i = 0; i < 31; i++) {
            solution[i] = 18;
        }

        //solve
        for (int i = 0; i < 21; i++) {
            firstMaxDepth = i;
            totalDRs = 0;
            reachDR(generateCOKey(cO), generateEOKey(eO), generateEqOKey(eqO), 18, 0, solution);
            System.out.println("done depth " + i);
        }
        System.out.println("done");
    }

    //First Stage
    public static boolean reachDR(int cO, int eO, int eq, int prev, int depth, int[] moves) {
        if (prev < 18) {
            cO = coMoveTable[cO][prev];
            eO = eoMoveTable[eO][prev];
            eq = eqoMoveTable[eq][prev];
        }

        //checking if reached domino
        boolean solved = cO == 0 && eO == 0 && eq == 425;
        if (solved) {
            if (depth == firstMaxDepth) {
                totalDRs++;
                //System.out.println("Domino reached in: " + depth + " moves");
                int[] newCP = cP.clone();
                int[] newEP = eP.clone();
                for (int i = 0; i < firstMaxDepth; i++) {
                    if (moves[i] == 18)
                        break;
                    newCP = moveCP(newCP, moves[i]);
                    newEP = moveEP(newEP, moves[i]);
                }
                int[] newEQP = getEqP(newEP);
                solveDR(generatePKey(newCP), generatePKey(condenseEP(newEP)), generatePKey(newEQP), 18, depth, moves);
                //return true;
            } else {
                return false;
            }
        }

        if (depth + eoDistTable[eO] > firstMaxDepth || depth + coDistTable[cO] > firstMaxDepth) {
            return false;
        }
        if (depth < firstMaxDepth) {
            //i is ud, lr, bf
            for (int i = 0; i < 3; i++) {
                //j is ulb, drf
                for (int j = 0; j < 2; j++) {
                    //k is u, u2, u'
                    for (int k = 0; k < 3; k++) {
                        if (prev < 6 * i || prev > (6 * i) + (3 * j) + 2) {
                            moves[depth] = (6 * i) + (3 * j) + k;
                            moves[depth + 1] = 18;
                            if (reachDR(cO, eO, eq, (6 * i) + (3 * j) + k, depth + 1, moves)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    //Second Stage
    public static boolean solveDR(int cP, int eP, int eqP, int prev, int depth, int[] moves) {
        if (prev < 18) {
            cP = cpMoveTable[cP][prev];
            eP = epMoveTable[eP][prev];
            eqP = eqpMoveTable[eqP][prev];
        }

        //checking if solved
        boolean solved = cP == 0 && eP == 0 && eqP == 0;
        if (solved && depth < optimal) {
            optimal = depth;
            System.out.println("Solution found in: " + depth + " moves");
            System.out.println(convertToNotation(moves));
        }

        if (depth + cpDistTable[cP] > optimal - 1 || depth + epDistTable[eP] > optimal - 1) {
            return false;
        }

        if (depth < optimal - 1) {
            //iterate through moves U U2 U' D D2 D' L2 R2 B2 F2
            //iterate through moves 0 1  2  3 4  5  6  7  8  9
            for (int i = 0; i < 2; i++) {
                for (int k = 0; k < 3; k++) {
                    if (prev > (i * 3) + 2) {
                        moves[depth] = (i * 3) + k;
                        moves[depth + 1] = 18;
                        if (solveDR(cP, eP, eqP, (i * 3) + k, depth + 1, moves)) {
                            return true;
                        }
                    }
                }
                for (int k = 0; k < 2; k++) {
                    if (prev < 6 + (i * 2) || prev > 6 + (i * 2) + k) {
                        moves[depth] = 7 + (i * 6) + (k * 3);
                        moves[depth + 1] = 18;
                        if (solveDR(cP, eP, eqP, 6 + (i * 2) + k, depth + 1, moves)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    //create tables from scratch
    public static void createTables() {

        //Corner Orientation Tables
        for (int i = 0; i < 2187; i++) {
            coDistTable[i] = -1;
        }
        coDistTable[0] = 0;
        int[] corners = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
        iterateCO(corners, 0);

        //Edge Orientation Tables
        for (int i = 0; i < 2048; i++) {
            eoDistTable[i] = -1;
        }
        eoDistTable[0] = 0;
        boolean[] edges = new boolean[] {true, true, true, true, true, true, true, true, true, true, true, true};
        iterateEO(edges, 0);

        //Equator Orientation Move Table
        iterateEqO();

        //Corner & Edge Permutation Tables
        for (int i = 0; i < 40320; i++) {
            cpDistTable[i] = -1;
            epDistTable[i] = -1;
        }
        cpDistTable[0] = 0;
        epDistTable[0] = 0;
        int[] pieces = new int[] {0, 1, 2, 3, 4, 5, 6, 7};
        iterateP(pieces, 7);

        //Equator Permutation Move Table
        int[] eqP = new int[] {0, 1, 2, 3};
        iterateEqP(eqP, 3);

        createDistanceTable("coDist.txt", coDistTable);
        createDistanceTable("eoDist.txt", eoDistTable);
        createDistanceTable("cpDist.txt", cpDistTable);
        createDistanceTable("epDist.txt", epDistTable);
        createMoveTable("coMove.txt", coMoveTable, 2187, 18);
        createMoveTable("eoMove.txt", eoMoveTable, 2048, 18);
        createMoveTable("eqoMove.txt", eqoMoveTable, 495, 18);
        createMoveTable("cpMove.txt", cpMoveTable, 40320, 10);
        createMoveTable("epMove.txt", epMoveTable, 40320, 10);
        createMoveTable("eqpMove.txt", eqpMoveTable, 24, 10);
    }

    //Corner Orientation
    public static void iterateCO(int[] cO, int count) {
        if (count < 7) {
            cO[count] = 0;
            iterateCO(cO, count + 1);
            cO[count] = 1;
            iterateCO(cO, count + 1);
            cO[count] = 2;
            iterateCO(cO, count + 1);
        } else if (count == 7) {
            int tally = 0;
            for (int i = 0; i < 7; i++) {
                tally += cO[i];
            }
            cO[count] = (15 - tally) % 3;
            if (coDistTable[generateCOKey(cO)] == -1) {
                findCOSolution(cO, 18, 0, false, 7);
            }
            for (int i = 0; i < 18; i++) {
                coMoveTable[generateCOKey(cO)][i] = generateCOKey(moveCO(cO, i));
            }
        }
    }

    public static int generateCOKey(int[] cO) {
        int tally = 0;
        for (int i = 0; i < 7; i++) {
            tally += (int) (cO[i] * (Math.pow(3, i)));
        }
        return tally;
    }

    public static int findCOSolution(int[] cO, int prev, int depth, boolean move, int best) {
        if (move) {
            cO = moveCO(cO, prev);
        }

        int key = generateCOKey(cO);
        if(coDistTable[key] != -1) {
            //distance of current state
            return coDistTable[key];
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
                            value = findCOSolution(cO, (6 * i) + (3 * j) + k, depth + 1, true, best) + 1;
                            if (value < best) {
                                best = value;
                                coDistTable[key] = best;
                            }
                        }
                    }
                }
            }
        }
        return best;
    }

    //Edge Orientation
    public static void iterateEO(boolean[] eO, int count) {
        if (count < 11) {
            eO[count] = true;
            iterateEO(eO, count + 1);
            eO[count] = false;
            iterateEO(eO, count + 1);
        } else if (count == 11) {
            boolean parity = true;
            for (int i = 0; i < 11; i++) {
                if (!eO[i]) {
                    parity = !parity;
                }
            }
            eO[count] = parity;
            if (eoDistTable[generateEOKey(eO)] == -1) {
                findEOSolution(eO, 18, 0, false, 8);
            }
            for (int i = 0; i < 18; i++) {
                eoMoveTable[generateEOKey(eO)][i] = generateEOKey(moveEO(eO, i));
            }
        }
    }

    public static int generateEOKey(boolean[] eO) {
        int tally = 0;
        for (int i = 0; i < 11; i++) {
            if (!eO[i]) {
                tally += (int) (Math.pow(2, i));
            }
        }
        return tally;
    }

    public static int findEOSolution(boolean[] eO, int prev, int depth, boolean move, int best) {
        if (move) {
            eO = moveEO(eO, prev);
        }

        int key = generateEOKey(eO);
        if(eoDistTable[key] != -1) {
            //distance of current state
            return eoDistTable[key];
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
                            value = findEOSolution(eO, (6 * i) + (3 * j) + k, depth + 1, true, best) + 1;
                            if (value < best) {
                                best = value;
                                eoDistTable[key] = best;
                            }
                        }
                    }
                }
            }
        }
        return best;
    }

    //Equator Orientation
    public static void iterateEqO() {
        for(int i = 0; i < 9; i++) {
            for(int j = i + 1; j < 10; j++) {
                for(int k = j + 1; k < 11; k++) {
                    for(int l = k + 1; l < 12; l++) {
                        boolean[] eq = new boolean[] {false, false, false, false, false, false, false, false, false, false, false, false};
                        eq[i] = true;
                        eq[j] = true;
                        eq[k] = true;
                        eq[l] = true;
                        for (int m = 0; m < 18; m++) {
                            eqoMoveTable[generateEqOKey(eq)][m] = generateEqOKey(moveEQO(eq, m));
                        }
                    }
                }
            }
        }
    }

    public static int generateEqOKey(boolean[] eq) {
        int seen = 0;
        int tally = 0;
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
                tally += num/den;
            }
        }
        return tally;
    }

    //Corner & Edge Permutation
    public static void iterateP(int[] p, int count) {
        if (count > 0) {
            for (int i = 0; i < count + 1; i++) {
                int[] newP = p.clone();
                int temp = newP[count - i];
                for (int j = 0; j < i; j++) {
                    newP[count - i + j] = newP[count - i + j + 1];
                }
                newP[count] = temp;
                iterateP(newP, count - 1);
            }
        } else {
            if (cpDistTable[generatePKey(p)] == -1) {
                findCPSolution(p, 18, 0, false, 11);
            }
            if (epDistTable[generatePKey(p)] == -1) {
                findEPSolution(p, 18, 0, false, 9);
            }
            for(int i = 0; i < 6; i++) {
                cpMoveTable[generatePKey(p)][i] = generatePKey(moveCP(p, i));
                epMoveTable[generatePKey(p)][i] = generatePKey(condenseEP(moveEP(extendEP(p), i)));
            }
            for(int i = 0; i < 4; i++) {
                cpMoveTable[generatePKey(p)][6 + i] = generatePKey(moveCP(p, (3 * i) + 7));
                epMoveTable[generatePKey(p)][6 + i] = generatePKey(condenseEP(moveEP(extendEP(p), (3 * i) + 7)));
            }
        }
    }

    public static int generatePKey(int[] p) {
        int tally = 0;
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
            tally += seen;
        }
        return tally;
    }

    //Pruning
    public static int findCPSolution(int[] cP, int prev, int depth, boolean move, int best) {
        if (move) {
            cP = moveCP(cP, prev);
        }

        int key = generatePKey(cP);
        if(cpDistTable[key] != -1) {
            //distance of current state
            return cpDistTable[key];
        }

        if (depth < best) {
            //iterate through moves U U2 U' D D2 D' L2 R2 B2 F2
            //iterate through moves 0 1  2  3 4  5  6  7  8  9
            for (int i = 0; i < 2; i++) {
                for (int k = 0; k < 3; k++) {
                    if (prev > (i * 3) + 2) {
                        int value = findCPSolution(cP, (i * 3) + k, depth + 1, true, best) + 1;
                        if (value < best) {
                            best = value;
                            cpDistTable[key] = best;
                        }
                    }
                }
                for (int k = 0; k < 2; k++) {
                    if (prev < 6 + (i * 2) || prev > 6 + (i * 2) + k) {
                        int value = findCPSolution(cP, 6 + (i * 2) + k, depth + 1, true, best) + 1;
                        if (value < best) {
                            best = value;
                            cpDistTable[key] = best;
                        }
                    }
                }
            }
        }
        return best;
    }

    public static int findEPSolution(int[] eP, int prev, int depth, boolean move, int best) {
        if (move) {
            eP = condenseEP(moveEP(extendEP(eP), prev));
        }

        int key = generatePKey(eP);
        if(epDistTable[key] != -1) {
            //distance of current state
            return epDistTable[key];
        }

        if (depth < best) {
            //iterate through moves U U2 U' D D2 D' L2 R2 B2 F2
            //iterate through moves 0 1  2  3 4  5  6  7  8  9
            for (int i = 0; i < 2; i++) {
                for (int k = 0; k < 3; k++) {
                    if (prev > (i * 3) + 2) {
                        int value = findEPSolution(eP, (i * 3) + k, depth + 1, true, best) + 1;
                        if (value < best) {
                            best = value;
                            epDistTable[key] = best;
                        }
                    }
                }
                for (int k = 0; k < 2; k++) {
                    if (prev < 6 + (i * 2) || prev > 6 + (i * 2) + k) {
                        int value = findEPSolution(eP, 6 + (i * 2) + k, depth + 1, true, best) + 1;
                        if (value < best) {
                            best = value;
                            epDistTable[key] = best;
                        }
                    }
                }
            }
        }
        return best;
    }

    //Equator Permutation
    public static void iterateEqP(int[] eqP, int count) {
        if (count > 0) {
            for (int i = 0; i < count + 1; i++) {
                int[] newP = eqP.clone();
                int temp = newP[count - i];
                for (int j = 0; j < i; j++) {
                    newP[count - i + j] = newP[count - i + j + 1];
                }
                newP[count] = temp;
                iterateEqP(newP, count - 1);
            }
        } else {
            for(int i = 0; i < 6; i++) {
                eqpMoveTable[generatePKey(eqP)][i] = generatePKey(moveEQP(eqP, i));
            }
            for(int i = 0; i < 4; i++) {
                eqpMoveTable[generatePKey(eqP)][6 + i] = generatePKey(moveEQP(eqP, (3 * i) + 7));
            }
        }
    }

    //import tables
    public static int[] importDistanceTable(String fileName, int size) {
        int[] distTable = new int[size];
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                distTable[count] = Integer.parseInt(line);
                count++;
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return distTable;
    }

    public static int[][] importMoveTable(String fileName, int size, int moveSet) {
        int[][] moveTable = new int[size][moveSet];
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            reader.readLine();
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < moveSet; j++) {
                    moveTable[i][j] = Integer.parseInt(reader.readLine());
                }
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return moveTable;
    }

    //create tables
    public static void createDistanceTable(String fileName, int[] table) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            writer.write("0");
            for (int i = 1; i < table.length; i++) {
                writer.write("\n" + table[i]);
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createMoveTable(String fileName, int[][] table, int size, int moveSet) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < moveSet; j++) {
                    writer.write("\n" + table[i][j]);
                }
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    //move pieces
    public static int[] moveCO(int[] cO, int move) {
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

    public static boolean[] moveEO(boolean[] eO, int move) {
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

    public static boolean[] moveEQO(boolean[] eqO, int move) {
        boolean[] newEqO = new boolean[12];
        int[] perm = getEdgeCycle(move);
        for (int i = 0; i < 12; i++) {
            newEqO[i] = eqO[perm[i]];
        }
        return newEqO;
    }

    public static int[] moveCP(int[] cP, int move) {
        int[] newCP = new int[8];
        int[] perm = getCornerCycle(move);
        for (int i = 0; i < 8; i++) {
            newCP[i] = cP[perm[i]];
        }
        return newCP;
    }

    public static int[] moveEP(int[] eP, int move) {
        int[] newEP = new int[12];
        int[] perm = getEdgeCycle(move);
        for (int i = 0; i < 12; i++) {
            newEP[i] = eP[perm[i]];
        }
        return newEP;
    }

    public static int[] moveEQP(int[] eqP, int move) {
        int[] newEQP = new int[4];
        int[] perm = getEdgeCycle(move);
        for (int i = 0; i < 4; i++) {
            newEQP[i] = eqP[perm[i + 4] - 4];
        }
        return newEQP;
    }

    public static int[] condenseEP(int[] eP) {
        return new int[] {eP[0], eP[1], eP[2], eP[3], eP[8], eP[9], eP[10], eP[11]};
    }

    public static int[] extendEP(int[] eP) {
        return new int[] {eP[0], eP[1], eP[2], eP[3], 4, 5, 6, 7, eP[4], eP[5], eP[6], eP[7]};
    }

    public static int[] getEqP(int[] eP) {
        return new int[] {eP[4], eP[5], eP[6], eP[7]};
    }

    public static int[] getCornerCycle(int move) {
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

    public static int[] getEdgeCycle(int move) {
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

    public static void scramble() {
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

        //Update eqO
        for (int i = 0; i < 12; i++) {
            eqO[i] = (4 <= eP[i] && eP[i] <= 7);
        }
    }

    public static void drawState() {
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

    //Converts a sequence of moves into notation
    public static String convertToNotation(int[] moves) {
        StringBuilder alg = new StringBuilder();
        for (int move : moves) {
            if (move == 18)
                break;
            int type = move % 3;
            switch (move - type) {
                case 0:
                    alg.append("U");
                    break;
                case 3:
                    alg.append("D");
                    break;
                case 6:
                    alg.append("L");
                    break;
                case 9:
                    alg.append("R");
                    break;
                case 12:
                    alg.append("B");
                    break;
                case 15:
                    alg.append("F");
                    break;
                default:
                    break;
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

}
