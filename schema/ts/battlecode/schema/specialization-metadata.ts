// automatically generated by the FlatBuffers compiler, do not modify

import * as flatbuffers from 'flatbuffers';

import { SpecializationType } from '../../battlecode/schema/specialization-type';


export class SpecializationMetadata {
  bb: flatbuffers.ByteBuffer|null = null;
  bb_pos = 0;
  __init(i:number, bb:flatbuffers.ByteBuffer):SpecializationMetadata {
  this.bb_pos = i;
  this.bb = bb;
  return this;
}

static getRootAsSpecializationMetadata(bb:flatbuffers.ByteBuffer, obj?:SpecializationMetadata):SpecializationMetadata {
  return (obj || new SpecializationMetadata()).__init(bb.readInt32(bb.position()) + bb.position(), bb);
}

static getSizePrefixedRootAsSpecializationMetadata(bb:flatbuffers.ByteBuffer, obj?:SpecializationMetadata):SpecializationMetadata {
  bb.setPosition(bb.position() + flatbuffers.SIZE_PREFIX_LENGTH);
  return (obj || new SpecializationMetadata()).__init(bb.readInt32(bb.position()) + bb.position(), bb);
}

type():SpecializationType {
  const offset = this.bb!.__offset(this.bb_pos, 4);
  return offset ? this.bb!.readInt8(this.bb_pos + offset) : SpecializationType.ATTACK;
}

level():number {
  const offset = this.bb!.__offset(this.bb_pos, 6);
  return offset ? this.bb!.readInt32(this.bb_pos + offset) : 0;
}

actionCost():number {
  const offset = this.bb!.__offset(this.bb_pos, 8);
  return offset ? this.bb!.readInt32(this.bb_pos + offset) : 0;
}

actionJailedPenalty():number {
  const offset = this.bb!.__offset(this.bb_pos, 10);
  return offset ? this.bb!.readInt32(this.bb_pos + offset) : 0;
}

cooldownReduction():number {
  const offset = this.bb!.__offset(this.bb_pos, 12);
  return offset ? this.bb!.readInt32(this.bb_pos + offset) : 0;
}

damageIncrease():number {
  const offset = this.bb!.__offset(this.bb_pos, 14);
  return offset ? this.bb!.readInt32(this.bb_pos + offset) : 0;
}

healIncrease():number {
  const offset = this.bb!.__offset(this.bb_pos, 16);
  return offset ? this.bb!.readInt32(this.bb_pos + offset) : 0;
}

static startSpecializationMetadata(builder:flatbuffers.Builder) {
  builder.startObject(7);
}

static addType(builder:flatbuffers.Builder, type:SpecializationType) {
  builder.addFieldInt8(0, type, SpecializationType.ATTACK);
}

static addLevel(builder:flatbuffers.Builder, level:number) {
  builder.addFieldInt32(1, level, 0);
}

static addActionCost(builder:flatbuffers.Builder, actionCost:number) {
  builder.addFieldInt32(2, actionCost, 0);
}

static addActionJailedPenalty(builder:flatbuffers.Builder, actionJailedPenalty:number) {
  builder.addFieldInt32(3, actionJailedPenalty, 0);
}

static addCooldownReduction(builder:flatbuffers.Builder, cooldownReduction:number) {
  builder.addFieldInt32(4, cooldownReduction, 0);
}

static addDamageIncrease(builder:flatbuffers.Builder, damageIncrease:number) {
  builder.addFieldInt32(5, damageIncrease, 0);
}

static addHealIncrease(builder:flatbuffers.Builder, healIncrease:number) {
  builder.addFieldInt32(6, healIncrease, 0);
}

static endSpecializationMetadata(builder:flatbuffers.Builder):flatbuffers.Offset {
  const offset = builder.endObject();
  return offset;
}

static createSpecializationMetadata(builder:flatbuffers.Builder, type:SpecializationType, level:number, actionCost:number, actionJailedPenalty:number, cooldownReduction:number, damageIncrease:number, healIncrease:number):flatbuffers.Offset {
  SpecializationMetadata.startSpecializationMetadata(builder);
  SpecializationMetadata.addType(builder, type);
  SpecializationMetadata.addLevel(builder, level);
  SpecializationMetadata.addActionCost(builder, actionCost);
  SpecializationMetadata.addActionJailedPenalty(builder, actionJailedPenalty);
  SpecializationMetadata.addCooldownReduction(builder, cooldownReduction);
  SpecializationMetadata.addDamageIncrease(builder, damageIncrease);
  SpecializationMetadata.addHealIncrease(builder, healIncrease);
  return SpecializationMetadata.endSpecializationMetadata(builder);
}
}
