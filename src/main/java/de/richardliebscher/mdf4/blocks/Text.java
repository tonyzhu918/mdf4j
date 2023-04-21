package de.richardliebscher.mdf4.blocks;

import de.richardliebscher.mdf4.io.ByteInput;
import de.richardliebscher.mdf4.io.FromBytesInput;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.IOException;

import static de.richardliebscher.mdf4.blocks.ParseUtils.parseText;

@Value
public class Text implements TextBased {
    String data;

    public static final Meta META = new Meta();
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Meta implements FromBytesInput<Text> {
        @Override
        public Text parse(ByteInput input) throws IOException {
            return Text.parse(input);
        }
    }

    public static Text parse(ByteInput input) throws IOException {
        final var blockHeader = BlockHeader.parse(BlockId.TX, input);
        return new Text(parseText(input, blockHeader.getDataLength()));
    }
}