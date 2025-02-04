# automatically generated by the FlatBuffers compiler, do not modify

# namespace: schema

import flatbuffers
from flatbuffers.compat import import_numpy
from typing import Any
np = import_numpy()

# Visually indicate a splash attack
class SplashAction(object):
    __slots__ = ['_tab']

    @classmethod
    def SizeOf(cls) -> int:
        return 2

    # SplashAction
    def Init(self, buf: bytes, pos: int):
        self._tab = flatbuffers.table.Table(buf, pos)

    # Location of the splash attack
    # SplashAction
    def Loc(self): return self._tab.Get(flatbuffers.number_types.Uint16Flags, self._tab.Pos + flatbuffers.number_types.UOffsetTFlags.py_type(0))

def CreateSplashAction(builder, loc):
    builder.Prep(2, 2)
    builder.PrependUint16(loc)
    return builder.Offset()
