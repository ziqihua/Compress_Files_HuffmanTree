import java.io.*;

public class main {
    public static void main(String[] atgs) throws IOException {
        Huff newHuffTree = new Huff();
        String inputFilePath = "src/TestFile2.txt";
        InputStream inputStream = new FileInputStream(inputFilePath);
        HuffTree root = newHuffTree.makeHuffTree(inputStream);

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        BitOutputStream out = new BitOutputStream(byteOut);
    }
}
