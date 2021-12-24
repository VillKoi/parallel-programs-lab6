package Anonimaizer;

import java.util.Random;

public class RandomInt {
    public Integer getInt(Integer maxValue) {
        Random random = new Random();
        return random.nextInt(maxValue);
    }
}
