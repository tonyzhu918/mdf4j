/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Copyright 2023 Richard Liebscher <r1tschy@posteo.de>
 */

package de.richardliebscher.mdf4.datatypes;

import java.util.Optional;

/**
 * Byte array type.
 */
public class ByteArrayType implements DataType {

  private final Integer maximumLength;

  public ByteArrayType(Integer maximumLength) {
    this.maximumLength = maximumLength;
  }

  /**
   * Return possible maximal length of string.
   *
   * @return Optional with maximal length if one exists
   */
  public Optional<Integer> getMaximumLength() {
    return Optional.of(maximumLength);
  }

  @Override
  public <R, E extends Throwable> R accept(Visitor<R, E> visitor) throws E {
    return visitor.visit(this);
  }
}
