# automatically generated by the FlatBuffers compiler, do not modify

# namespace: schema

import flatbuffers
from flatbuffers.compat import import_numpy
from typing import Any
np = import_numpy()

# Generic action representing damage to a robot
class DamageAction(object):
    __slots__ = ['_tab']

    @classmethod
    def SizeOf(cls) -> int:
        return 4

    # DamageAction
    def Init(self, buf: bytes, pos: int):
        self._tab = flatbuffers.table.Table(buf, pos)

    # Id of the damage target
    # DamageAction
    def Id(self): return self._tab.Get(flatbuffers.number_types.Uint16Flags, self._tab.Pos + flatbuffers.number_types.UOffsetTFlags.py_type(0))
    # DamageAction
    def Damage(self): return self._tab.Get(flatbuffers.number_types.Uint16Flags, self._tab.Pos + flatbuffers.number_types.UOffsetTFlags.py_type(2))

def CreateDamageAction(builder, id, damage):
    builder.Prep(2, 4)
    builder.PrependUint16(damage)
    builder.PrependUint16(id)
    return builder.Offset()
