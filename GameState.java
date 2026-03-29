import java.util.Collections;
import java.util.ArrayList;

public class GameState {
    Briefcase[] cases = new Briefcase[26];
    int playerCase = -1;
    boolean playerCaseSelected = false;
    int casesOpenedThisRound = 0;
    int round = 1;
    int[] casesPerRound = {6, 5, 4, 3, 2, 1, 1, 1, 1};
    ArrayList<Long> previousOffers = new ArrayList<>();
    boolean peekUsed = false;
    boolean peekMode = false;

    long[] amounts = {1, 100, 1000, 5000, 10000, 25000, 50000, 75000,
            100000, 200000, 300000, 400000, 500000, 750000,
            1000000, 5000000, 10000000, 25000000, 50000000,
            75000000, 100000000, 200000000, 300000000,
            500000000, 750000000, 1000000000};

    public GameState() {
        ArrayList<Long> values = new ArrayList<>();
        for (long amt : amounts) values.add(amt);
        Collections.shuffle(values);
        for (int i = 0; i < 26; i++) {
            cases[i] = new Briefcase(i + 1, values.get(i));
        }
    }

    public long calculateEV() {
        long sum = 0;
        int count = 0;
        for (Briefcase b : cases) {
            if (!b.isOpen && b.id != playerCase) {
                sum += b.value;
                count++;
            }
        }
        return count == 0 ? 0 : sum / count;
    }
}