package de.richardliebscher.mdf4.blocks;

import de.richardliebscher.mdf4.exceptions.FormatException;
import de.richardliebscher.mdf4.io.ByteInput;
import lombok.Value;

import java.io.IOException;

@Value
public class BlockHeader {
    long length;
    long[] links;

    public long getDataLength() {
        return length - 24L - links.length * 8L;
    }

    public static BlockHeader parse(BlockId id, ByteInput input) throws IOException {
        final var typeId = input.readI32LE();
        if (id.asInt() != typeId) {
            throw new FormatException("Got unexpected block " + BlockId.of(typeId) + ", but expected " + id);
        }

        input.readI32LE(); // padding
        final var length = input.readI64LE();
        final var linkCount = input.readI64LE();

        final var links = new long[Math.toIntExact(linkCount)];
        for (int i = 0; i < linkCount; i++) {
            links[i] = input.readI64LE();
        }
        return new BlockHeader(length, links);
    }

    public static BlockHeader parseExpecting(BlockId id, ByteInput input, int links, int miniumSize) throws IOException {
        final var blockHeader = parse(id, input);
        if (blockHeader.links.length < links) {
            throw new FormatException(
                    "Expecting a minium of " + links + " in " + id + " block, but got "
                            + blockHeader.links.length + " links");
        }
        if (blockHeader.getDataLength() < miniumSize) {
            throw new FormatException(
                    "Expecting a minium of " + miniumSize + " data bytes in " + id + " block, but got "
                            + blockHeader.getDataLength() + " data bytes");
        }
        return blockHeader;
    }
}