package io.skytreasure.kotlingroupchat;

import android.util.Log;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void hash() {
        List<String> values = new ArrayList<>();
        values.add("akash");
        long result = 17;
        for (String v : values) result = 37 * result + v.hashCode();
        Log.e("", String.valueOf(result));
//3540882287
    }
}