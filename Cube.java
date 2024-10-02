import java.util.Random;

public class Cube {
    private int[] cP = new int[8];
    private int[] eP = new int[12];
    private int[] cO = new int[8];
    private boolean[] eO = new boolean[12];
    private boolean[] eq = new boolean[12];

    public Cube() {
        for (int i = 0; i < 8; i++) {
            cP[i] = i;
            cO[i] = 0;
        }
        for (int i = 0; i < 12; i++) {
            eP[i] = i;
            eO[i] = true;
        }
        updateEq();
    }

    public Cube(int[] cP, int[] eP, int[] cO, boolean[] eO) {
        for (int i = 0; i < 8; i++) {
            this.cP[i] = cP[i];
            this.cO[i] = cO[i];
        }
        for (int i = 0; i < 12; i++) {
            this.eP[i] = eP[i];
            this.eO[i] = eO[i];
        }
    }

    public Cube(int[] cO, boolean[] eO, boolean[] eq) {
        for (int i = 0; i < 8; i++) {
            this.cO[i] = cO[i];
        }
        for (int i = 0; i < 12; i++) {
            this.eO[i] = eO[i];
            this.eq[i] = eq[i];
        }
    }

    public Cube(int[] cO) {
        for (int i = 0; i < 8; i++) {
            this.cO[i] = cO[i];
        }
    }

    public Cube(boolean[] eO) {
        for (int i = 0; i < 12; i++) {
            this.eO[i] = eO[i];
        }
    }

    public int[] getcP() {
        return cP;
    }

    public int[] geteP() {
        return eP;
    }

    public int[] getcO() {
        return cO;
    }

    public boolean[] geteO() {
        return eO;
    }

    public boolean[] getEq() {
        return eq;
    }

    public void updateEq() {
        for (int i = 0; i < 12; i++) {
            eq[i] = (4 <= eP[i] && eP[i] <= 7);
        }
    }

    private void permuteCorners(int[] p, boolean halfTurn) {
        int temp = cP[p[0]];
        cP[p[0]] = cP[p[1]];
        if (halfTurn) {
            cP[p[1]] = temp;
            temp = cP[p[2]];
        } else {
            cP[p[1]] = cP[p[2]];
        }
        cP[p[2]] = cP[p[3]];
        cP[p[3]] = temp;
    }

    private void permuteEdges(int[] p, boolean halfTurn) {
        int temp = eP[p[0]];
        eP[p[0]] = eP[p[1]];
        if (halfTurn) {
            eP[p[1]] = temp;
            temp = eP[p[2]];
        } else {
            eP[p[1]] = eP[p[2]];
        }
        eP[p[2]] = eP[p[3]];
        eP[p[3]] = temp;
    }

    private void orientCorners(int[] p, int twist, boolean halfTurn) {
        int temp = cO[p[0]];
        if (halfTurn) {
            cO[p[0]] = cO[p[1]];
            cO[p[1]] = temp;
            temp = cO[p[2]];
            cO[p[2]] = cO[p[3]];
            cO[p[3]] = temp;
        } else {
            switch (twist) {
                case 0:
                    cO[p[0]] = cO[p[1]];
                    cO[p[1]] = cO[p[2]];
                    cO[p[2]] = cO[p[3]];
                    cO[p[3]] = temp;
                    break;
                case 1:
                    cO[p[0]] = ((cO[p[1]] + 1) % 3);
                    cO[p[1]] = ((cO[p[2]] + 2) % 3);
                    cO[p[2]] = ((cO[p[3]] + 1) % 3);
                    cO[p[3]] = ((temp + 2) % 3);
                    break;
                case 2:
                    cO[p[0]] = ((cO[p[1]] + 2) % 3);
                    cO[p[1]] = ((cO[p[2]] + 1) % 3);
                    cO[p[2]] = ((cO[p[3]] + 2) % 3);
                    cO[p[3]] = ((temp + 1) % 3);
                    break;
            }
        }
    }

    private void orientEdges(int[] p, boolean flip, boolean halfTurn) {
        boolean ori = eO[p[0]];
        if (halfTurn) {
            eO[p[0]] = eO[p[1]];
            eO[p[1]] = ori;
            ori = eO[p[2]];
            eO[p[2]] = eO[p[3]];
            eO[p[3]] = ori;
        } else {
            if (flip) {
                eO[p[0]] = !eO[p[1]];
                eO[p[1]] = !eO[p[2]];
                eO[p[2]] = !eO[p[3]];
                eO[p[3]] = !ori;
            } else {
                eO[p[0]] = eO[p[1]];
                eO[p[1]] = eO[p[2]];
                eO[p[2]] = eO[p[3]];
                eO[p[3]] = ori;
            }
        }
    }

    public void permuteEquators(int[] p, boolean halfTurn) {
        if (eq[p[0]] != eq[p[1]]) {
            eq[p[0]] = !eq[p[0]];
            eq[p[1]] = !eq[p[1]];
        }
        if (!halfTurn && eq[p[1]] != eq[p[2]]) {
            eq[p[1]] = !eq[p[1]];
            eq[p[2]] = !eq[p[2]];
        }
        if (eq[p[2]] != eq[p[3]]) {
            eq[p[2]] = !eq[p[2]];
            eq[p[3]] = !eq[p[3]];
        }
    }

    public void move(int i, int stage) {
        int[] cornerCycle = new int[4];
        int[] edgeCycle = new int[4];
        boolean halfTurn = false;
        int twist = 0;
        boolean flip = false;
        switch (i) {
            case 0: //u
                cornerCycle = new int[] {0, 2, 3, 1};
                edgeCycle = new int[] {0, 1, 3, 2};
                break;
            case 1: //u2
                cornerCycle = new int[] {0, 3, 1, 2};
                edgeCycle = new int[] {0, 3, 1, 2};
                halfTurn = true;
                break;
            case 2: //u'
                cornerCycle = new int[] {1, 3, 2, 0};
                edgeCycle = new int[] {2, 3, 1, 0};
                break;
            case 3: //d
                cornerCycle = new int[] {4, 5, 7, 6};
                edgeCycle = new int[] {8, 10, 11, 9};
                break;
            case 4: //d2
                cornerCycle = new int[] {6, 5, 7, 4};
                edgeCycle = new int[] {9, 10, 8, 11};
                halfTurn = true;
                break;
            case 5: //d'
                cornerCycle = new int[] {6, 7, 5, 4};
                edgeCycle = new int[] {9, 11, 10, 8};
                break;
            case 6: //l
                cornerCycle = new int[] {0, 4, 6, 2};
                edgeCycle = new int[] {1, 4, 9, 6};
                twist = 2;
                break;
            case 7: //l2
                cornerCycle = new int[] {2, 4, 6, 0};
                edgeCycle = new int[] {6, 4, 9, 1};
                halfTurn = true;
                break;
            case 8: //l'
                cornerCycle = new int[] {2, 6, 4, 0};
                edgeCycle = new int[] {6, 9, 4, 1};
                twist = 1;
                break;
            case 9: //r
                cornerCycle = new int[] {1, 3, 7, 5};
                edgeCycle = new int[] {2, 7, 10, 5};
                twist = 1;
                break;
            case 10: //r2
                cornerCycle = new int[] {5, 3, 7, 1};
                edgeCycle = new int[] {5, 7, 10, 2};
                halfTurn = true;
                break;
            case 11: //r'
                cornerCycle = new int[] {5, 7, 3, 1};
                edgeCycle = new int[] {5, 10, 7, 2};
                twist = 2;
                break;
            case 12: //b
                cornerCycle = new int[] {0, 1, 5, 4};
                edgeCycle = new int[] {0, 5, 8, 4};
                twist = 1;
                flip = true;
                break;
            case 13: //b2
                cornerCycle = new int[] {4, 1, 5, 0};
                edgeCycle = new int[] {4, 5, 8, 0};
                halfTurn = true;
                break;
            case 14: //b'
                cornerCycle = new int[] {4, 5, 1, 0};
                edgeCycle = new int[] {4, 8, 5, 0};
                twist = 2;
                flip = true;
                break;
            case 15: //f
                cornerCycle = new int[] {2, 6, 7, 3};
                edgeCycle = new int[] {3, 6, 11, 7};
                twist = 2;
                flip = true;
                break;
            case 16: //f2
                cornerCycle = new int[] {3, 6, 7, 2};
                edgeCycle = new int[] {7, 6, 11, 3};
                halfTurn = true;
                break;
            case 17: //f'
                cornerCycle = new int[] {3, 7, 6, 2};
                edgeCycle = new int[] {7, 11, 6, 3};
                twist = 1;
                flip = true;
                break;
        }
        if (stage == 3) {
            permuteCorners(cornerCycle, halfTurn);
            permuteEdges(edgeCycle, halfTurn);
        }
        if (stage == 2) {
            permuteEquators(edgeCycle, halfTurn);
        }
        if (stage != 1) {
            orientEdges(edgeCycle, flip, halfTurn);
        }
        if (stage != 0) {
            orientCorners(cornerCycle, twist, halfTurn);
        }
    }

    public void scramble() {
        Random rnd = new Random();

        //Corners
        int counter = 0;
        for (int i = 0; i < 8; i++) {
            int swap = rnd.nextInt(i, 8);
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
        boolean parity = true;
        int toggle;
        for (int i = 0; i < 12; i++) {
            int swap = rnd.nextInt(i, 12);
            int temp = eP[i];
            eP[i] = eP[swap];
            eP[swap] = temp;
            if (i < 11) {
                toggle = rnd.nextInt(0, 2);
                if (toggle == 1) {
                    eO[i] = false;
                    parity = !parity;
                }
            } else {
                eO[i] = parity;
            }
        }

        //Fix PLL Parity
        int cornerCounter = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = i + 1; j < 8; j++) {
                if (cP[i] > cP[j]) {
                    cornerCounter++;
                }
            }
        }
        int edgeCounter = 0;
        for (int i = 0; i < 12; i++) {
            for (int j = i + 1; j < 12; j++) {
                if (eP[i] > eP[j]) {
                    edgeCounter++;
                }
            }
        }
        if ((cornerCounter % 2) != (edgeCounter % 2)) {
            int temp = eP[0];
            eP[0] = eP[1];
            eP[1] = temp;
        }
        updateEq();
    }

    public void drawState() {
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
}
