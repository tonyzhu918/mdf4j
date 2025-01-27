/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Copyright 2023 Richard Liebscher <r1tschy@posteo.de>
 */

package de.richardliebscher.mdf4.blocks;

import static de.richardliebscher.mdf4.blocks.ParseUtils.flagsSet;

import de.richardliebscher.mdf4.LazyIoList;
import de.richardliebscher.mdf4.Link;
import de.richardliebscher.mdf4.io.ByteInput;
import de.richardliebscher.mdf4.io.FromBytesInput;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

/**
 * Header/HD-Block.
 */
@Value
public class Header {

  // Time flags
  private static final byte OFFSET_VALID = 1 << 1;
  // Time quality class
  private static final byte TIME_SRC_PC = 0;

  Link<DataGroupBlock> firstDataGroup;
  long firstFileHistory;
  long firstChannelHierarchy;
  long firstAttachment;
  long firstEventBlock;
  Link<TextBasedBlock> comment;

  /**
   * Absolute start time in nanoseconds since midnight Jan 1st, 1970.
   */
  Instant startTime;
  /**
   * Time zone offset in minutes.
   */
  ZoneOffset timeZoneOffset;
  /**
   * Daylight saving time (DST) offset in minutes.
   */
  ZoneOffset dstOffset;

  byte timeClass;

  byte flags;

  float startAngleRad;

  float startDistanceM;

  public LazyIoList<DataGroupBlock> getDataGroups(ByteInput input) {
    return () -> new DataGroupBlock.Iterator(firstDataGroup, input);
  }

  public Optional<TextBasedBlock> readComment(ByteInput input) throws IOException {
    return comment.resolve(TextBasedBlock.META, input);
  }

  public static Header parse(ByteInput input) throws IOException {
    final var blockHeader = BlockHeader.parseExpecting(BlockType.HD, input, 6, 24);
    final var startTime = ParseUtils.toInstant(input.readI64());
    final var tzOffsetMin = input.readI16();
    final var dstOffsetMin = input.readI16();
    final var timeFlags = input.readU8();
    final var timeClass = input.readU8();
    final var flags = input.readU8();
    input.skip(1);
    final var startAngleRad = input.readF32();
    final var startDistanceM = input.readF32();

    final var links = blockHeader.getLinks();
    return new Header(
        Link.of(links[0]),
        links[1],
        links[2],
        links[3],
        links[4],
        Link.of(links[5]),
        startTime,
        flagsSet(timeFlags, OFFSET_VALID) ? ZoneOffset.ofTotalSeconds(tzOffsetMin * 60) : null,
        flagsSet(timeFlags, OFFSET_VALID) ? ZoneOffset.ofTotalSeconds(dstOffsetMin * 60) : null,
        timeClass,
        flags,
        startAngleRad,
        startDistanceM
    );
  }

  public static final Meta META = new Meta();

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Meta implements FromBytesInput<Header> {

    @Override
    public Header parse(ByteInput input) throws IOException {
      return Header.parse(input);
    }
  }
}
