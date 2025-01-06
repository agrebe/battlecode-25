// automatically generated by the FlatBuffers compiler, do not modify

/* eslint-disable @typescript-eslint/no-unused-vars, @typescript-eslint/no-explicit-any, @typescript-eslint/no-non-null-assertion */

import * as flatbuffers from 'flatbuffers';

/**
 * Visually indicate a mop attack
 */
export class MopAction {
  bb: flatbuffers.ByteBuffer|null = null;
  bb_pos = 0;
  __init(i:number, bb:flatbuffers.ByteBuffer):MopAction {
  this.bb_pos = i;
  this.bb = bb;
  return this;
}

/**
 * Ids of the mopped targets, possibly 0
 */
id0():number {
  return this.bb!.readUint16(this.bb_pos);
}

id1():number {
  return this.bb!.readUint16(this.bb_pos + 2);
}

id2():number {
  return this.bb!.readUint16(this.bb_pos + 4);
}

static sizeOf():number {
  return 6;
}

static createMopAction(builder:flatbuffers.Builder, id0: number, id1: number, id2: number):flatbuffers.Offset {
  builder.prep(2, 6);
  builder.writeInt16(id2);
  builder.writeInt16(id1);
  builder.writeInt16(id0);
  return builder.offset();
}

}
