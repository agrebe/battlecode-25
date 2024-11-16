// automatically generated by the FlatBuffers compiler, do not modify

/* eslint-disable @typescript-eslint/no-unused-vars, @typescript-eslint/no-explicit-any, @typescript-eslint/no-non-null-assertion */

import * as flatbuffers from 'flatbuffers';

/**
 * The final event sent in the game.
 */
export class GameFooter {
  bb: flatbuffers.ByteBuffer|null = null;
  bb_pos = 0;
  __init(i:number, bb:flatbuffers.ByteBuffer):GameFooter {
  this.bb_pos = i;
  this.bb = bb;
  return this;
}

static getRootAsGameFooter(bb:flatbuffers.ByteBuffer, obj?:GameFooter):GameFooter {
  return (obj || new GameFooter()).__init(bb.readInt32(bb.position()) + bb.position(), bb);
}

static getSizePrefixedRootAsGameFooter(bb:flatbuffers.ByteBuffer, obj?:GameFooter):GameFooter {
  bb.setPosition(bb.position() + flatbuffers.SIZE_PREFIX_LENGTH);
  return (obj || new GameFooter()).__init(bb.readInt32(bb.position()) + bb.position(), bb);
}

/**
 * The ID of the winning team of the game.
 */
winner():number {
  const offset = this.bb!.__offset(this.bb_pos, 4);
  return offset ? this.bb!.readInt8(this.bb_pos + offset) : 0;
}

static startGameFooter(builder:flatbuffers.Builder) {
  builder.startObject(1);
}

static addWinner(builder:flatbuffers.Builder, winner:number) {
  builder.addFieldInt8(0, winner, 0);
}

static endGameFooter(builder:flatbuffers.Builder):flatbuffers.Offset {
  const offset = builder.endObject();
  return offset;
}

static createGameFooter(builder:flatbuffers.Builder, winner:number):flatbuffers.Offset {
  GameFooter.startGameFooter(builder);
  GameFooter.addWinner(builder, winner);
  return GameFooter.endGameFooter(builder);
}
}
