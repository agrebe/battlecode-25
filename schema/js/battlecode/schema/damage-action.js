"use strict";
// automatically generated by the FlatBuffers compiler, do not modify
Object.defineProperty(exports, "__esModule", { value: true });
exports.DamageAction = void 0;
/**
 * Generic action representing damage to a robot
 */
var DamageAction = /** @class */ (function () {
    function DamageAction() {
        this.bb = null;
        this.bb_pos = 0;
    }
    DamageAction.prototype.__init = function (i, bb) {
        this.bb_pos = i;
        this.bb = bb;
        return this;
    };
    /**
     * Id of the damage target
     */
    DamageAction.prototype.id = function () {
        return this.bb.readUint16(this.bb_pos);
    };
    DamageAction.prototype.damage = function () {
        return this.bb.readUint16(this.bb_pos + 2);
    };
    DamageAction.sizeOf = function () {
        return 4;
    };
    DamageAction.createDamageAction = function (builder, id, damage) {
        builder.prep(2, 4);
        builder.writeInt16(damage);
        builder.writeInt16(id);
        return builder.offset();
    };
    return DamageAction;
}());
exports.DamageAction = DamageAction;
