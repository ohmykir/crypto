package prng;

public class XorShift {
    private long state;

    public XorShift(long seed) {
        this.state = seed != 0 ? seed : 1;
    }

    public long next() {
        state ^= state << 13;
        state ^= state >>> 17;
        state ^= state << 7;
        return state;
    }

    public byte nextByte() {
        return (byte) (next() & 0xFF);
    }
}
