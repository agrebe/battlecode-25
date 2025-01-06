# automatically generated by the FlatBuffers compiler, do not modify

# namespace: schema

import flatbuffers
from flatbuffers.compat import import_numpy
from typing import Any
np = import_numpy()

# Visually indicate that a tower was upgraded
class UpgradeAction(object):
    __slots__ = ['_tab']

    @classmethod
    def SizeOf(cls) -> int:
        return 20

    # UpgradeAction
    def Init(self, buf: bytes, pos: int):
        self._tab = flatbuffers.table.Table(buf, pos)

    # Id of the upgraded tower
    # UpgradeAction
    def Id(self): return self._tab.Get(flatbuffers.number_types.Uint16Flags, self._tab.Pos + flatbuffers.number_types.UOffsetTFlags.py_type(0))
    # UpgradeAction
    def NewHealth(self): return self._tab.Get(flatbuffers.number_types.Int32Flags, self._tab.Pos + flatbuffers.number_types.UOffsetTFlags.py_type(4))
    # UpgradeAction
    def NewMaxHealth(self): return self._tab.Get(flatbuffers.number_types.Int32Flags, self._tab.Pos + flatbuffers.number_types.UOffsetTFlags.py_type(8))
    # UpgradeAction
    def NewPaint(self): return self._tab.Get(flatbuffers.number_types.Int32Flags, self._tab.Pos + flatbuffers.number_types.UOffsetTFlags.py_type(12))
    # UpgradeAction
    def NewMaxPaint(self): return self._tab.Get(flatbuffers.number_types.Int32Flags, self._tab.Pos + flatbuffers.number_types.UOffsetTFlags.py_type(16))

def CreateUpgradeAction(builder, id, newHealth, newMaxHealth, newPaint, newMaxPaint):
    builder.Prep(4, 20)
    builder.PrependInt32(newMaxPaint)
    builder.PrependInt32(newPaint)
    builder.PrependInt32(newMaxHealth)
    builder.PrependInt32(newHealth)
    builder.Pad(2)
    builder.PrependUint16(id)
    return builder.Offset()
