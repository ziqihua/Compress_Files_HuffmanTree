import org.junit.Test;

import java.io.*;
import java.util.Map;

import static org.junit.Assert.*;

public class HuffTest implements IHuffConstants {

    @Test
    public void testMakeHuffTree() throws IOException {
        Huff newHuffTree = new Huff();
        String filePath = "TestFile1.txt";
        BitInputStream bitInputStream = new BitInputStream(new FileInputStream(filePath));
        HuffTree h = newHuffTree.makeHuffTree(bitInputStream);
        assertEquals(307, h.root().weight());
    }

    @Test
    public void testMakeTable() throws IOException {
        Huff newHuffTree = new Huff();
        String filePath = "TestFile2.txt";
        BitInputStream bitInputStream = new BitInputStream(new FileInputStream(filePath));
        HuffTree h = newHuffTree.makeHuffTree(bitInputStream);
        newHuffTree.makeTable();
        char ch1 = 'b';
        char ch2 = 'c';
        int intValue1 = ch1;
        int intValue2 = ch2;
        Map<Integer, String> huffTable = newHuffTree.makeTable();
        assertEquals("01", huffTable.get(intValue1));
        assertEquals("1", huffTable.get(intValue2));
    }

    @Test
    public void testGetCode() throws IOException {
        Huff newHuffTree = new Huff();
        String filePath = "TestFile2.txt";
        BitInputStream bitInputStream = new BitInputStream(new FileInputStream(filePath));
        HuffTree h = newHuffTree.makeHuffTree(bitInputStream);
        newHuffTree.makeTable();
        char ch1 = 'b';
        char ch2 = 'c';
        assertEquals("01", newHuffTree.getCode(ch1));
        assertEquals("1", newHuffTree.getCode(ch2));
    }

    @Test
    public void testShowCounts() throws IOException {
        Huff newHuffTree = new Huff();
        String filePath = "TestFile1.txt";
        BitInputStream bitInputStream = new BitInputStream(new FileInputStream(filePath));
        HuffTree h = newHuffTree.makeHuffTree(bitInputStream);
        Map<Integer, Integer> frequencyTable = newHuffTree.showCounts();
        char ch1 = 'E';
        char ch2 = 'L';
        Integer intValue1 = (int) ch1;
        Integer intValue2 = (int) ch2;
        assertEquals((Integer)120, frequencyTable.get(intValue1));
        assertEquals((Integer)42, frequencyTable.get(intValue2));
    }

    @Test
    public void testHeaderSize() throws IOException {
        Huff newHuffTree = new Huff();
        String filePath = "TestFile2.txt";
        BitInputStream bitInputStream = new BitInputStream(new FileInputStream(filePath));
        HuffTree h = newHuffTree.makeHuffTree(bitInputStream);
        newHuffTree.makeTable();
        int bitSizeOfMagic = 2 + (int) Math.floor(Math.log(MAGIC_NUMBER) / Math.log(2));
        assertEquals(32, bitSizeOfMagic);
        assertEquals(75, newHuffTree.headerSize());
    }

    @Test
    public void testWriteHeader() throws IOException {
        Huff newHuffTree = new Huff();
        String inputFilePath = "TestFile2.txt";
        BitInputStream bitInputStream = new BitInputStream(new FileInputStream(inputFilePath));
        newHuffTree.makeHuffTree(bitInputStream);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertEquals(75, newHuffTree.writeHeader(new BitOutputStream(out)));
    }


    @Test
    public void testCompressThenUncompress() throws IOException {
        Huff h = new Huff();
        String inputFilePath = "TestFile1.txt";
        String compressedFile = "OutputCompressed1.txt";
        String uncompressedFile = "Uncompressed.txt";
        int compressedSize = h.write(inputFilePath, compressedFile, true);
        assertTrue(compressedSize > 0);
        int uncompressedSize = h.uncompress(compressedFile, uncompressedFile);
        assertTrue(uncompressedSize > 0);
        assertTrue(compareFileBits(inputFilePath, uncompressedFile));
    }

    private boolean compareFileBits(String file1, String file2) throws IOException {
        File firstFile = new File(file1);
        File secFile = new File(file2);
        if (firstFile.length() != secFile.length()) {
            return false;
        }
        // Use buffer to compare
        try (BufferedInputStream bufferOne
                     = new BufferedInputStream(new FileInputStream(firstFile));
            BufferedInputStream bufferTwo
                    = new BufferedInputStream(new FileInputStream(secFile))) {
            int bitsRead1;
            int bitsRead2;
            while ((bitsRead1 = bufferOne.read()) != -1 && (bitsRead2 = bufferTwo.read()) != -1) {
                if (bitsRead1 != bitsRead2) {
                    return false;
                }
            }
            return true;
        }
    }
}