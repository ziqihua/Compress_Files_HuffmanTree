import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class CharCounterTest {

    @Test
    public void add() {
        ICharCounter cc = new CharCounter();
        cc.add(200);
        cc.add(200);
        assertEquals(2, cc.getCount(200));
    }

    @Test
    public void testCountAll() throws IOException {
        ICharCounter cc = new CharCounter();
        InputStream ins = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
        int actualSize = cc.countAll(ins);
        assertEquals(10, actualSize);
    }

    @Test
    public void getCount() throws IOException {
        ICharCounter cc = new CharCounter();
        InputStream ins = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
        cc.countAll(ins);
        assertEquals(3, cc.getCount('t'));
    }

    @Test (expected = IllegalArgumentException.class)
    public void testIntOutOfRange() {
        ICharCounter cc = new CharCounter();
        cc.getCount(300);
    }

    @Test
    public void set() {
        ICharCounter cc = new CharCounter();
        cc.set(100, 3);
        assertEquals(3, cc.getCount(100));
    }

    @Test
    public void clear() throws IOException {
        ICharCounter cc = new CharCounter();
        InputStream ins = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
        cc.countAll(ins);
        cc.clear();
        assertEquals(0, cc.getCount('t'));
    }

    @Test
    public void getTable()  throws IOException {
        ICharCounter cc = new CharCounter();
        InputStream ins = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
        cc.countAll(ins);
        assertEquals(3, cc.getCount('t'));
        assertEquals(1, cc.getCount('e'));
        assertEquals(2, cc.getCount('s'));
        assertEquals(1, cc.getCount('r'));
        assertEquals(1, cc.getCount('i'));
        assertEquals(1, cc.getCount('n'));
        assertEquals(1, cc.getCount('g'));
    }
}