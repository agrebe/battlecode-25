// automatically generated by the FlatBuffers compiler, do not modify

/* eslint-disable @typescript-eslint/no-unused-vars, @typescript-eslint/no-explicit-any, @typescript-eslint/no-non-null-assertion */

import * as flatbuffers from 'flatbuffers';

/**
 * Indicates that the robot died due to an uncaught exception
 */
export class DieExceptionAction {
  bb: flatbuffers.ByteBuffer|null = null;
  bb_pos = 0;
  __init(i:number, bb:flatbuffers.ByteBuffer):DieExceptionAction {
  this.bb_pos = i;
  this.bb = bb;
  return this;
}

value():number {
  return this.bb!.readInt8(this.bb_pos);
}

static sizeOf():number {
  return 1;
}

static createDieExceptionAction(builder:flatbuffers.Builder, value: number):flatbuffers.Offset {
  builder.prep(1, 1);
  builder.writeInt8(value);
  return builder.offset();
}

}
