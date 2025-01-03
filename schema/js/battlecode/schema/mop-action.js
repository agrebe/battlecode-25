"use strict";
// automatically generated by the FlatBuffers compiler, do not modify
Object.defineProperty(exports, "__esModule", { value: true });
exports.MopAction = void 0;
/**
 * Visually indicate a mop attack
 */
var MopAction = /** @class */ (function () {
    function MopAction() {
        this.bb = null;
        this.bb_pos = 0;
    }
    MopAction.prototype.__init = function (i, bb) {
        this.bb_pos = i;
        this.bb = bb;
        return this;
    };
    /**
     * Ids of the mopped targets, possibly 0
     */
    MopAction.prototype.id0 = function () {
        return this.bb.readUint16(this.bb_pos);
    };
    MopAction.prototype.id1 = function () {
        return this.bb.readUint16(this.bb_pos + 2);
    };
    MopAction.prototype.id2 = function () {
        return this.bb.readUint16(this.bb_pos + 4);
    };
    MopAction.sizeOf = function () {
        return 6;
    };
    MopAction.createMopAction = function (builder, id0, id1, id2) {
        builder.prep(2, 6);
        builder.writeInt16(id2);
        builder.writeInt16(id1);
        builder.writeInt16(id0);
        return builder.offset();
    };
    return MopAction;
}());
exports.MopAction = MopAction;
