# cube-solver

An efficient Rubik's cube solver inspired by [Kociemba's algorithm](https://kociemba.org/cube.htm).

## Overview

The algorithm is made up of two phases: Domino reduction (DR) and then solving using the reduced move set.
The first phase aims to solve the corner orientation (CO), edge orientation (EO) and equator edge locations (EqL).
The aim of the second phase is to solve the corner permutation (CP), edge permutation (EP) and the equator edge 
permutation (EqP).

Splitting the search into two phases allows the program to find solutions in a reasonable time without increasing the 
move count of the solution very much. For example: Searching 11 moves deep in the first phase and then 15 moves deep in 
the second phase (roughly 18<sup>11</sup> + 10<sup>15</sup> states) is much fewer states than searching 26 moves
deep to solve directly from the scramble (roughly 18<sup>26</sup> states). It also increases the effectiveness of 
pruning because you can prune each phase separately.


## Coordinates

### Overview

The program expresses the state of the cube in each phase as coordinates to reduce the number of calculations.
In the first phase the coordinates are (CO, EO, EqL). In the second phase the coordinates are (CP, EP, EqP).
Each coordinate is an integer that refers to a specific state. For example: The CO coordinate refers to the orientation
of the 8 corners. At the 'cubie level' this would look something like (0, 2, 1, 2, 1, 0, 1, 2), where each value denotes
the orientation of a specific corner. This array is converted into a single integer unique to that state.

The get_key functions convert arrays in the cubie level to their unique coordinate. The _moveTable arrays store the
relationship between states. E.g. coMoveTable\[450]\[12] = 208 means applying a B move (move 12) to
the state 450 brings you to the state 208.

### Phase 1 Coordinates

#### Corner Orientation

At the cubie level the corner orientations are stored in an array of 8 integers from 0-2, each one denoting the
orientation of a specific corner e.g. (0, 2, 1, 2, 1, 0, 1, 2). Since the sum of orientations is 0 mod 3, each state
can be uniquely defined by only the first 7 values, giving 3<sup>7</sup> = 2187 unique states. We convert to the 
coordinate by treating the array as a base 3 system:
i.e (0, 2, 1, 2, 1, 0, 1, 2) converts to: 
0\*3<sup>0</sup> + 2\*3<sup>1</sup> + 1\*3<sup>2</sup> + 2\*3<sup>3</sup> + 1\*3<sup>4</sup> + 0\*3<sup>5</sup> +
1*3<sup>6</sup> = 879

Note that the solved state has a CO coordinate of 0.

#### Edge Orientation

At the cubie level the edge orientations are stored in an array of 12 booleans, each one denoting the
orientation of a specific edge e.g. (T, F, T, T, F, F, T, F, F, F, T, T). Since the number of oriented edges is always 
even, each state can be uniquely defined by only the first 11 values, giving 2<sup>11</sup> = 2048 unique states. We 
convert to the coordinate by treating the array as a base 2 system (with true being 0 and false being 1):
i.e (T, F, T, T, F, F, T, F, F, F, T, T) converts to:
0\*2<sup>0</sup> + 1\*2<sup>1</sup> + 0\*2<sup>2</sup> + 0\*2<sup>3</sup> + 1\*2<sup>4</sup> + 1\*2<sup>5</sup> +
0\*2<sup>6</sup> + 1\*2<sup>7</sup> + 1\*2<sup>8</sup> + 1\*2<sup>9</sup> + 0\*2<sup>10</sup> = 946

Note that the solved state has an EO coordinate of 0.

#### Equator Locations

Equator edges are the edges which belong on the equator slice. At the cubie level the equator
locations are stored in an array of 12 booleans, each boolean indicating whether a specific edge is an equator edge.
Since there are 4 equator edges the total number of unique states is C(12,4) = 495. We convert to the coordinate by 
summing C(n,k) where n are the non-equator edge indices and k is one less than the number of equator edges with
indices below n (we ignore when k is -1). i.e (F, F, T, F, T, F, T, F, F, F, T, F) converts to C(3,0) + C(5,1) + 
C(7,2) + C(8,2) + C(9,2) + C(11,3) = 256

We order the edges so that the solved state has an EqL coordinate of 0 (The last 4 indices are the correct positions).

### Phase 2 Coordinates

#### Corner Permutation

#### Edge Permutation

#### Equator Permutation