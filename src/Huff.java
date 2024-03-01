import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class Huff implements ITreeMaker, IHuffEncoder, IHuffModel, IHuffHeader, IHuffConstants {
    private HuffTree huffTree;
    private static Map<Integer, Integer> frequencyTable = new HashMap<>();
    @Override
    public HuffTree makeHuffTree(InputStream stream) throws IOException {
        PriorityQueue<HuffTree> treeNodes = new PriorityQueue<>();
        CharCounter frequencyCounter = new CharCounter();
        frequencyCounter.countAll(stream);
        frequencyTable = frequencyCounter.getTable();
        for (Map.Entry<Integer, Integer> entry : frequencyTable.entrySet()) {
            HuffTree charNode = new HuffTree(entry.getKey(), entry.getValue());
            treeNodes.add(charNode);
        }
        treeNodes.offer(new HuffTree(PSEUDO_EOF, 1));

        HuffTree tmp1, tmp2, newNode;

        while (treeNodes.size() > 1) {
            tmp1 = treeNodes.poll();
            tmp2 = treeNodes.poll();
            newNode = new HuffTree(tmp1.root(), tmp2.root(),
                    tmp1.weight() + tmp2.weight());
            treeNodes.add(newNode);
        }

        huffTree = treeNodes.poll();
        return huffTree;
    }

    @Override
    public Map<Integer, String> makeTable() {
        Map<Integer, String> huffCodeMap = new HashMap<>();
        assignCodeHelper(huffTree.root(), "", huffCodeMap);
        return huffCodeMap;
    }

    private void assignCodeHelper(IHuffBaseNode node, String code,
                                  Map<Integer, String> huffCodeMap) {
        if (node.isLeaf()) {
            HuffLeafNode leaf = (HuffLeafNode) node;
            huffCodeMap.put(leaf.element(), code);
            return;
        } else {
            HuffInternalNode internal = (HuffInternalNode) node; // Then use a preorder traversal
            assignCodeHelper(internal.left(), code + "0", huffCodeMap); // Traverse left
            assignCodeHelper(internal.right(), code + "1", huffCodeMap); // Traverse right
        }
    }

    @Override
    public String getCode(int i) {
        Map<Integer, String> encodingTable = this.makeTable();
        if (encodingTable == null) {
            throw new IllegalArgumentException("Must call makeTabke() first");
        }
        return encodingTable.getOrDefault(i, null);
    }

    @Override
    public Map<Integer, Integer> showCounts() {
        return frequencyTable;
    }

    @Override
    public int headerSize() {
        return BITS_PER_INT + huffTree.size();
    }

    @Override
    public int writeHeader(BitOutputStream out) {
        out.write(BITS_PER_INT, MAGIC_NUMBER);
        return BITS_PER_INT + writeHeaderHelper(huffTree.root(), out);
    }

    private int writeHeaderHelper(IHuffBaseNode root, BitOutputStream out) {
        int bitNum = 0;
        if (root.isLeaf()) {
            out.write(1, 1); // Write 1 for leaf
            int character = ((HuffLeafNode)root).element();
            out.write(9, character); // Write the character as 9 bits
            bitNum += 10;
        } else {
            out.write(1, 0); // Write 0 for non-leaf
            bitNum += 1;
            bitNum += writeHeaderHelper(((HuffInternalNode)root).left(), out);
            bitNum += writeHeaderHelper(((HuffInternalNode)root).right(), out);
        }
        return bitNum;
    }

    @Override
    public HuffTree readHeader(BitInputStream in) throws IOException {
        int magic = in.read(BITS_PER_INT);
        if (magic != MAGIC_NUMBER) {
            throw new IOException("magic number not right");
        } else {
            return readHeaderHelper(in);
        }
    }

    private HuffTree readHeaderHelper(BitInputStream in) throws IOException {
        int bit = in.read(1); // Read a single bit
        if (bit == -1) {
            throw  new IOException("End of File Error");
        }
        if (bit == 0) { // Internal node
            HuffTree leftSubTree = readHeaderHelper(in);
            HuffTree rightSubTree = readHeaderHelper(in);
            return new HuffTree(leftSubTree.root(), rightSubTree.root(), 0);
        } else { // Leaf node
            int character = in.read(9); // Read the character represented by 9 bits
            return new HuffTree(character, 0);
        }
    }

    @Override
    public int write(String inFile, String outFile, boolean force) {
        BitInputStream inputBits;
        try {
            inputBits = new BitInputStream(new FileInputStream(inFile));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            this.makeHuffTree(inputBits);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Map<Integer, String> huffCodeTable = this.makeTable();
        // Compute the originalSize and expected compressedSize
        int originalCharNum = 0;
        int compressedSize = headerSize();
        for (Map.Entry<Integer, Integer> entry : frequencyTable.entrySet()) {
            int character = entry.getKey();
            int frequency = entry.getValue();
            originalCharNum += BITS_PER_WORD * frequency;
            String charHuffCode = huffCodeTable.get(character);
            if (charHuffCode != null) {
                compressedSize += charHuffCode.length() * frequency;
            }
        }
        // Add EOF to the end of compressedSize
        compressedSize += huffCodeTable.get(PSEUDO_EOF).length();
        // Compare two sizes
        if (originalCharNum <= compressedSize && !force) {
            return compressedSize;
        }

        // Else, initialize the inputStream and write out the compressed-file bits
        BitInputStream inBits;
        try {
            inBits = new BitInputStream(new FileInputStream(inFile));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        BitOutputStream outBits;
        try {
            outBits = new BitOutputStream(new FileOutputStream(outFile));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        this.writeHeader(outBits);
        try {
            int bitRead = inBits.read(BITS_PER_WORD);
            while (bitRead != -1) {
                String huffCode = huffCodeTable.get(bitRead);
                outBits.write(huffCode.length(), Integer.parseInt(huffCode, 2));
                bitRead = inBits.read(BITS_PER_WORD);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Add EOF to the end of outBits
        outBits.write(huffCodeTable.get(PSEUDO_EOF).length(),
                Integer.parseInt(huffCodeTable.get(PSEUDO_EOF), 2));
        outBits.close();
        inputBits.close();
        return compressedSize;
    }

    @Override
    public int uncompress(String inFile, String outFile) {
        BitInputStream inputCompressedBits;
        try {
            inputCompressedBits = new BitInputStream(new FileInputStream(inFile));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        BitOutputStream uncompressedChars;
        try {
            uncompressedChars = new BitOutputStream(new FileOutputStream(outFile));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        HuffTree uncompressedHuffTree;
        IHuffBaseNode root;
        int uncompressedBits = 0;
        boolean endOfFile = false;
        IHuffBaseNode currentNode;
        try {
            uncompressedHuffTree = this.readHeader(inputCompressedBits);
            root = uncompressedHuffTree.root();
            while (!endOfFile) {
                currentNode = root;
                while (!currentNode.isLeaf()) {
                    int bit = inputCompressedBits.read(1);
                    if (bit == -1) {
                        System.err.println("should not happen! trouble reading bits");
                    }
                    if ((bit & 1) == 0) { // read a 0, go left in tree
                        currentNode = ((HuffInternalNode) currentNode).left();
                    } else { // read a 1, go right in tree
                        currentNode = ((HuffInternalNode) currentNode).right();
                    }
                }
                //When we meet a leaf
                int character = ((HuffLeafNode)currentNode).element();
                if (character != PSEUDO_EOF) { // Check for pseudo-EOF
                    // Decode character and write to output
                    uncompressedChars.write(BITS_PER_WORD, character);
                    uncompressedBits += BITS_PER_WORD;
                } else {
                    endOfFile = true;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        uncompressedChars.close();
        inputCompressedBits.close();
        return uncompressedBits;
    }
}
